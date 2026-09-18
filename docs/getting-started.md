# Getting started

[简体中文](getting-started.zh-CN.md) | **English**

This page ends with a script that stores a row and reads it back. Everything else in the documentation
is reference for the parts you do not need yet.

Assumes the plugin is installed: see [Requirements](../README.md#requirements) and
[Install](../README.md#install).

## 1. Connect

A connection is made by a script, not by a config file:

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    if last database error is set:
        send "Database connection failed: %last database error%" to console
        stop
```

- `"MySQL"` is the type name of an implementation. The jar registers four, `"MySQL"`, `"PostgreSQL"`,
  `"MongoDB"` and `"JDBC"`, and the name is matched exactly, so a name that is only a database *product*
  is not one of them; see [Connections](connections.md#the-implementation-name).
- `url` is required. `username` and `password` may be empty strings, for a server that does not ask for
  them.
- The section always waits, so anything after it in the same trigger runs against the live connection.
  A different script that ran earlier would have seen `No database connected.`, which is why connecting
  on load is the usual place.
- Credentials live in the script file, so keep that file as private as the database account is.

## 2. Describe a table

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

Registering creates the table when it is missing and waits for it to exist. It does **not** change a
table that is already there, so adding a column here has no effect on a database that has the table
already; see [Tables](tables.md) for that trap and the rest of the column syntax.

## 3. Write a row

```sk
command /adduser <text> <integer>:
    trigger:
        insert one entity into table "users" and wait:
            values:
                name: arg-1
                age: arg-2
        if last database error is set:
            send "Could not store %arg-1%: %last database error%" to sender
            stop
        send "Stored %arg-1%." to sender
```

`and wait` is what makes the failure readable here. Without it the section hands the work to the
background and the next line runs immediately, so `last database error` is still unset; see
[Errors and waiting](errors-and-waiting.md).

`id` is not in the values because the database assigns it. If you need it afterwards, write your own
value for it and use `upsert` instead; see [Cookbook](cookbook.md).

## 4. Read it back

```sk
command /whois <text>:
    trigger:
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        if last database error is set:
            send "Lookup failed: %last database error%" to sender
            stop
        if {_user::id} is not set:
            send "No user named %arg-1%." to sender
            stop
        send "name: %{_user::name}%, age: %{_user::age}%" to sender
```

A select always waits, so the lines after it already have the row. Each column of the row is one key of
the variable, named after the column. `{_user::id} is not set` is how "no row matched" looks, and it is
also how a NULL column looks: a column whose value is NULL leaves its key unset. Both cases are covered
in [Reading rows](reading.md).

## 5. Change and remove

```sk
update one entity in table "users" by id {_user::id} and wait:
    values:
        age: 26

delete one entity from table "users" by id {_user::id} and wait
```

Both are in [Updating and deleting](updating-and-deleting.md), including the versions that work on
whatever a `where` block matches. The delete has no body, so it goes without a colon, while the update
above has `values` to indent; see [Writing rows](writing.md).

## The whole thing

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    if last database error is set:
        send "Database connection failed: %last database error%" to console
        stop

    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
        age: int, nullable
    if last database error is set:
        send "Table registration failed: %last database error%" to console

command /adduser <text> <integer>:
    trigger:
        insert one entity into table "users" and wait:
            values:
                name: arg-1
                age: arg-2
        if last database error is set:
            send "Could not store %arg-1%: %last database error%" to sender
            stop
        send "Stored %arg-1%." to sender

command /whois <text>:
    trigger:
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        if last database error is set:
            send "Lookup failed: %last database error%" to sender
            stop
        if {_user::id} is not set:
            send "No user named %arg-1%." to sender
            stop
        send "name: %{_user::name}%, age: %{_user::age}%" to sender
```

A second script may use the same table, because the connection belongs to the server rather than to the
script that made it; see [Connections](connections.md).

## Where to go next

| If you want to | Read |
| --- | --- |
| Know what a column may say, and what registering does not do | [Tables](tables.md) |
| Store many rows at once, or update-or-insert | [Writing rows](writing.md) |
| Filter, page, or read by id | [Reading rows](reading.md) |
| Understand exactly when `last database error` is set | [Errors and waiting](errors-and-waiting.md) |
| Store an item stack, a location, a date, an NBT compound | [Types](types.md) |
| Find out why something silently did nothing | [Troubleshooting](troubleshooting.md) |
