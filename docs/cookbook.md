# Cookbook

[简体中文](cookbook.zh-CN.md) | **English**

These recipes cover common tasks and are ready to copy and adapt. They use only syntax introduced elsewhere in the documentation.

Companion examples live in [`docs/examples/cookbook.sk`](examples/cookbook.sk). They are maintained manually, not extracted from this page, and may differ in wording.

CI checks that they parse on a real server; it does not run the commands or verify their results. The examples use English messages and are wrapped in `command /example-…` blocks. Command names must be unique across all example files because they load on the same test server.

## Know the id of a row you just created

The plugin does not return generated ids. Assign the id in your script and save the row with `upsert`:

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    register a database table "users":
        id: bigint, primary key, not null
        name: string(64), not null

command /adduser <text>:
    trigger:
        # Assign ids with a script counter. This counter is not shared between servers.
        # For multiple servers, let the database assign ids, then find the row by another column.
        if {users::next-id} is not set:
            set {users::next-id} to 0
        add 1 to {users::next-id}
        upsert one entity in table "users" by id {users::next-id} and wait:
            values:
                name: arg-1
        if last database error is set:
            send "Could not store %arg-1%: %last database error%" to sender
            stop
        send "Stored %arg-1% as id {users::next-id}." to sender
```

## One row per player

Use the player's UUID as the primary key, then use `upsert` to maintain one row per player:

```sk
on join:
    upsert one entity in table "players" by id uuid of player and wait:
        values:
            name: name of player
            last_seen: now
    if last database error is set:
        send "Could not save your data: %last database error%" to console
```

## Read, change, store back

```sk
command /addage <integer>:
    trigger:
        select one entity from table "players" and store the result in {_row::*}:
            where all:
                uuid = uuid of player
        if {_row::uuid} is not set:
            send "No row for you yet." to sender
            stop
        set {_age} to {_row::age}
        if {_age} is not set:
            set {_age} to 0
        set {_new-age} to {_age} + arg-1
        update one entity in table "players" by id uuid of player and wait:
            values:
                age: {_new-age}
        if last database error is set:
            send "Could not update: %last database error%" to sender
            stop
        send "Your age is now %{_new-age}%." to sender
```

`by id` uses the primary key declared when the table was registered: `uuid` here, as in the recipe above. If a table has both `id` and `uuid` columns, `by id` uses whichever is the primary key.

Only columns listed in the `values` block are written; the others keep their existing values.

The read and update are separate operations. If another script changes the age between them, this write can overwrite that change, causing a lost update. For concurrent use, add a version column and use a conditional update that matches both the primary key and the old version, writing the new version in the same update. Check `store affected rows` to confirm that one row was updated. If the count is 0, read again before retrying or reporting a conflict.

## Copy rows between tables

```sk
select many entities from table "users" and store the results in {_rows::*}:
    where all:
        active = false

insert many {_rows::*} into table "archived_users" and wait
if last database error is set:
    send "Archive failed: %last database error%" to console
    stop

delete entities from table "users" and wait:
    where all:
        active = false
```

A `select many` result can be passed directly to `insert many`, with no restructuring. The insert finishes before the delete runs. In 1.2, all database statements wait for completion; the `and wait` retained in this example has no effect.

These three steps are not a transaction and do not roll back together on failure. Avoid concurrent changes to the affected data: the delete matches `active = false` again, so it can remove rows that became eligible after the read and were never archived. Changes made after the read are not automatically included in the archive either. Use this example during a maintenance window with the relevant writes paused.

## Build rows in a script and insert them

```sk
set {_rows::1::name} to "Alice"
set {_rows::1::age} to 25
set {_rows::2::name} to "Bob"
set {_rows::2::age} to 30

insert many {_rows::*} into table "users" and wait
```

The row index is one-based, exactly like the keys a read produces.

## Walk every page

There is no count query. Read pages until one is empty, with a limit of 100 pages in this example:

```sk
set {_page} to 1
while {_page} <= 100:
    select page {_page} with size 50 from table "users" and store the results in {_page-rows::*}:
        where all:
            active = true
    # Walk the row indices and check the primary key to see whether another row exists.
    # Each row is a sub-list. Other columns may be NULL, leaving an unset key that would stop the loop early.
    # If {_row} is still 1 after the loop, the page was empty.
    set {_row} to 1
    while {_page-rows::%{_row}%::id} is set:
        send "%{_page-rows::%{_row}%::name}%" to console
        add 1 to {_row}
    if {_row} is 1:
        exit loop
    add 1 to {_page}
```

`size of {_page-rows::*}` counts first-layer values, but each row is a sub-list. The example instead walks row indices until the next row's primary key is unset.

The loop limit prevents endless pagination. If page 100 still contains data, this run stops there and leaves the remaining rows unread.

A page is an offset into primary-key order, not a snapshot. Inserts or deletes between reads can shift later rows, causing duplicates or omissions, so use this recipe when nothing is writing to the table. A keyset filter (`where all: id > {_last}`) requires a stable primary-key sort order. `select many` does not offer `ORDER BY`, so adding the filter alone is not a complete replacement for pagination. See [Reading rows](reading.md).

## Store an item's NBT

This requires SkBee and an `nbtcompound` column:

```sk
register a database table "tools":
    id: bigint, primary key, auto increment, not null
    data: nbtcompound, nullable

command /savetool:
    trigger:
        insert one entity into table "tools" and wait:
            values:
                data: nbt of player's tool
        if last database error is set:
            send "Could not save the tool: %last database error%" to sender
            stop
        send "Saved." to sender
```

The saved compound is a snapshot from when the command ran; later changes to the item do not affect the row. The compound you read back works directly with SkBee syntax. You can also inspect its contents as SNBT text. See [Types](types.md).

## Delete old rows in batches

```sk
command /prune:
    trigger:
        set {_cutoff} to now - 30 days
        set {_pruned} to 0
        loop 10 times:
            delete entities from table "logs" with limit 500 and store affected rows in {_deleted} and wait:
                where all:
                    created < {_cutoff}
            if last database error is set:
                send "Prune failed: %last database error%" to console
                exit loop
            add {_deleted} to {_pruned}
            if {_deleted} is less than 500:
                exit loop
        send "Pruned %{_pruned}% old rows." to sender
```

Batching helps reduce the time each statement holds the table. Each delete returns its affected-row count, so a batch of fewer than 500 rows stops the loop early. The script also reports the total deleted. If all ten batches are full, this run deletes at most 5,000 rows and leaves the rest for a later run.

## Use the table from a second script

Scripts on the server share connections. Choose one script to create the connection and register its tables:

```sk
# database.sk
on load:
    set {database::ready} to false    # Clear the ready flag left by the previous run.
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
    if last database error is set:
        send "Database is not ready: %last database error%" to console
        stop
    set {database::ready} to true
```

```sk
# users.sk
command /whois <text>:
    trigger:
        if {database::ready} is not true:
            send "The database is not ready yet." to sender
            stop
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        # ...
```

A second script can create a connection, but doing so replaces the current one, and the new connection has no registered tables. Keeping this responsibility in one script avoids accidental replacements. See [Connections](connections.md).

`{database::ready}` is a global variable and survives restarts. Resetting it to false on load prevents a failed connection attempt from leaving the previous run's `true` in place. The second script can then report that the database is not ready instead of running statements that fail with `No database connected.`.
