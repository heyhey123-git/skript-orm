# Cookbook

[简体中文](cookbook.zh-CN.md) | **English**

These examples show common database tasks. Adapt table names and columns to your script. A companion script is available at [`docs/examples/cookbook.sk`](examples/cookbook.sk).

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

These two `players` recipes assume the table is registered with `uuid` as its primary key and
with `name`, `last_seen`, and `age` columns. Use `upsert` to maintain one row per player:

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
        if last database error is set:
            send "Could not read your data: %last database error%" to sender
            stop
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

`by id` uses the column declared as the primary key when `players` was registered: `uuid` in both
player recipes. If a table has both `id` and `uuid` columns, `by id` still uses its declared primary key.

Only columns listed in the `values` block are written; the others keep their existing values.

The read and update are separate operations. If another script changes the age between them, this write can overwrite that change, causing a lost update. For concurrent use, add a version column and use a conditional update that matches both the primary key and the old version, writing the new version in the same update. Check `store affected rows` to confirm that one row was updated. If the count is 0, read again before retrying or reporting a conflict.

## Copy rows between tables

```sk
select many entities from table "users" and store the results in {_rows::*}:
    where all:
        active = false
if last database error is set:
    send "Could not read rows to archive: %last database error%" to console
    stop
if {_rows::1::id} is not set:
    stop

insert many {_rows::*} into table "archived_users" and wait
if last database error is set:
    send "Archive failed: %last database error%" to console
    stop

delete entities from table "users" and wait:
    where all:
        active = false
```

A `select many` result can be passed directly to `insert many`. Check for an empty result first: `insert many` rejects an unset variable. The insert finishes before the delete runs.

The select, insert, and delete are separate statements, not a transaction; failure does not roll
them back together. Avoid concurrent changes to these rows: the delete matches `active = false`
again, so it can remove rows that became eligible after the read and were never archived. Changes
made after the read are not automatically included in the archive either. Use this example during
a maintenance window with writes to `users` paused.

## Build rows in a script and insert them

```sk
set {_rows::1::name} to "Alice"
set {_rows::1::age} to 25
set {_rows::2::name} to "Bob"
set {_rows::2::age} to 30

insert many {_rows::*} into table "users" and wait
```

Row indices start at 1. A `select many` result uses the same paths, such as `{_rows::1::name}`.

## Walk every page

There is no count query. Read pages until one is empty, with a limit of 100 pages in this example:

```sk
set {_page} to 1
while {_page} <= 100:
    select page {_page} with size 50 from table "users" and store the results in {_page-rows::*}:
        where all:
            active = true
    if last database error is set:
        send "Could not read page %{_page}%: %last database error%" to console
        exit loop
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

Each page sorts by primary key and skips the rows covered by earlier pages; it does not preserve the data as it was on the first read. Inserts or deletes between reads can shift later rows, causing duplicates or omissions, so use this recipe when nothing is writing to the table. Another approach is to remember the last primary key read and query `id > {_last}`, but that query must also sort consistently by primary key. `select many` does not offer `ORDER BY`, so the condition alone cannot reliably page through the table. See [Reading rows](reading.md).

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

The row stores the NBT contents at the time the command ran; later changes to the item do not affect it. The NBT compound (a collection of named NBT values) you read back works directly with SkBee syntax. You can also inspect its contents as SNBT, the text form of NBT. See [Types](types.md).

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
