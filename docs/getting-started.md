# Getting started

[简体中文](getting-started.zh-CN.md) | **English**

Build a working script that connects to a database, stores a row, and reads it back.
The other pages provide reference material when you need more detail.

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

- `"MySQL"` is an implementation type name. The jar registers four: `"MySQL"`, `"PostgreSQL"`,
  `"MongoDB"` and `"JDBC"`. Names must match exactly; an arbitrary database *product* name will not work.
  See [Connections](connections.md#the-implementation-name).
- `url` is required. `username` and `password` may be empty strings if the database does not require them.
- The section always waits. After a successful connection, later statements in the same trigger can
  use it. A script that accesses the database before the connection is ready gets `No database connected.`.
  Connecting in `on load` is the usual approach, but script load order still matters.
- Credentials are stored in the script file, so restrict access to that file.

## 2. Describe a table

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

Registration creates the table if it is missing and waits for completion. It does **not** alter an
existing table: adding a column to the script does not add it to the database.
See [Tables](tables.md) for this limitation and the full column syntax.

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

The write waits for completion, so `last database error` reflects this operation before the next line
runs. Database errors are available there, not just in the console. Every statement waits, with or
without `and wait`; see [Errors and waiting](errors-and-waiting.md).

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

A select always waits, so subsequent lines can read its result. Each column becomes a variable key
with the same name. In this example, `{_user::id} is not set` means no row matched. In general, a NULL
column also leaves its key unset, so checks on other columns must account for both cases.
See [Reading rows](reading.md).

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

Other scripts can use the same table because the connection is shared across the server.

**One script should manage the shared default connection.** Another `create a connection` replaces it
with a new connection that has no registered tables, so subsequent operations report `Table 'users' not found.`.
The database tables are not deleted; their registration is simply absent from the new connection.

Other scripts should reuse the connection or create their own with `named`. See
[Connections](connections.md).

## Where to go next

| If you want to | Read |
| --- | --- |
| Know what a column may say, and what registering does not do | [Tables](tables.md) |
| Store many rows at once, or update-or-insert | [Writing rows](writing.md) |
| Filter, page, or read by id | [Reading rows](reading.md) |
| Understand exactly when `last database error` is set | [Errors and waiting](errors-and-waiting.md) |
| Store an item stack, a location, a date, an NBT compound | [Types](types.md) |
| Find out why something silently did nothing | [Troubleshooting](troubleshooting.md) |
