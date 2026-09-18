# Cookbook

[简体中文](cookbook.zh-CN.md) | **English**

Whole recipes, using only the syntax in the rest of the documentation. Each one is written as a script
you can paste and adapt.

## Know the id of a row you just created

The plugin does not return a generated id, so let the script own the value and use `upsert`:

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
        # A counter keeps the id in the script. It is not shared between servers, so a setup with
        # more than one server should let the database assign ids instead and look the row up again.
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

A `uuid` column as the primary key makes the player the identity, and `upsert` then keeps one row per
player:

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
        if {_row::id} is not set:
            send "No row for you yet." to sender
            stop
        set {_age} to {_row::age}
        if {_age} is not set:
            set {_age} to 0
        set {_new-age} to {_age} + arg-1
        update one entity in table "players" by id {_row::id} and wait:
            values:
                age: {_new-age}
        if last database error is set:
            send "Could not update: %last database error%" to sender
            stop
        send "Your age is now %{_new-age}%." to sender
```

Only the columns in the `values` block are written, so the rest of the row is left alone.

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

The variable a `select many` fills is already shaped the way `insert many` reads, so nothing has to be
reshaped. The insert waits, so the delete cannot run before it.

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

There is no count query, so a script walks pages until one comes back empty:

```sk
set {_page} to 1
while {_page} <= 100:
    select page {_page} with size 50 from table "users" and store the results in {_page-rows::*}:
        where all:
            active = true
    # Walk the row indices: every row is a sub-list, so a row index is done when a column of the next
    # row is unset. `{_row} is 1` after the walk means the page itself came back empty.
    set {_row} to 1
    while {_page-rows::%{_row}%::name} is set:
        send "%{_page-rows::%{_row}%::name}%" to console
        add 1 to {_row}
    if {_row} is 1:
        exit loop
    add 1 to {_page}
```

A row is a sub-list of the result variable, so `size of {_page-rows::*}` counts first-layer values
rather than rows, which is why the recipe counts the row indices itself.

The loop bound is a safety net: without a count query, an upper bound is what keeps a script from paging
forever when a condition keeps matching.

## Store an item's NBT

Needs SkBee, and an `nbtcompound` column:

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

What is stored is the compound as it was when the command ran: changing the item afterwards does not
change the row. Reading it back gives a compound SkBee's syntax can use, and its SNBT text is the easy
way to check what is inside. See [Types](types.md).

## Delete old rows in batches

```sk
command /prune:
    trigger:
        set {_cutoff} to now - 30 days
        loop 10 times:
            delete entities from table "logs" with limit 500 and wait:
                where all:
                    created < {_cutoff}
            if last database error is set:
                send "Prune failed: %last database error%" to console
                exit loop
        send "Pruned up to 5000 old rows." to sender
```

A bounded batch keeps one statement from holding the table for long. A delete does not report how many
rows it removed, so the loop bound is the practical stop condition: the recipe deletes at most
`10 × 500` rows per run, and running it again a moment later continues where it left off.

## Use the table from a second script

The connection belongs to the server, so exactly one script should own it and register the tables:

```sk
# database.sk
on load:
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

A second script that connects as well is not an error, but it replaces the connection and starts with no
tables registered, so the owning script is the one that should do it. See [Connections](connections.md).
