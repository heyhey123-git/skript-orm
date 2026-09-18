# Tables

[简体中文](tables.zh-CN.md) | **English**

A table is described once, in the script that connects, and the description is what the plugin uses to
build statements and to name the keys of a result.

## Registering

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

The body is one column per line, in the exact form

```
name: type[(size)][, primary key][, auto increment][, not null]
```

- **The name** may contain letters, digits, marks and underscores, and may not start with a digit. The
  same rule holds for the table name after `register a database table`, and it is checked when the
  section runs: `"my table"` or `"2fa_codes"` is refused with `Invalid table name '…'` in
  `last database error`, quoted or not. A column name is checked while the script is parsed, so it fails
  earlier. Names are used as written, so keep one spelling: on a MySQL server running on Linux, table
  names are case-sensitive.
- **The type** is one of the names in [Types](types.md). An unknown one is refused when the section runs,
  with a message saying the connected database does not support it; it lands in `last database error`
  right after the section.
- **The size** in brackets is for `string`, and must be greater than zero. Leaving it off gives that type
  its default of 255. Writing one for another type is refused by PostgreSQL, where `uuid` and `location`
  are `BYTEA` and take no size, and accepted by MySQL and `"JDBC"`, where it becomes the width of the
  column: `location(16)` is smaller than a serialized location, so every write to it fails with a
  data-too-long error. `uuid` is 16 bytes and `location` 2048 whatever the brackets say, so leave them to
  `string`.
- **The modifiers** are separated from the type and from each other by commas. They are
  `primary key`, `auto increment`, `not null` and `nullable`. A column is nullable unless it says
  `not null`, and saying both is an error.

Columns are direct lines. A nested block is refused, and so are duplicate column names, more than one
`primary key`, and `auto increment` on a column that is not one.

## What a primary key is for

`primary key` marks the column the `by id` operations use, and pagination needs one:

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
update one entity in table "users" by id {_id} and wait
delete one entity from table "users" by id {_id} and wait
select page 2 with size 20 from table "users" and store the results in {_page::*}
```

`auto increment` lets the database assign the value. The plugin does not hand the generated id back to
the script, so a script that needs to know it should write its own value and use `upsert`, or find the
row again by another column; see [Cookbook](cookbook.md).

## What registering does, and what it does not

Registering runs `CREATE TABLE IF NOT EXISTS`, then waits for the table to exist. It never drops,
alters or inspects anything.

**A table that already exists is left exactly as it is.** Adding a column to the script and reloading it
changes nothing in the database: the plugin stores the new column in its own description, the database
does not grow one, and the operations that mention it fail at runtime while the ones that do not keep
working. There is no warning, because from the plugin's point of view the registration succeeded. To
change a table, run the `ALTER TABLE` yourself, or drop the table in a development database and let the
plugin create it again.

**Registering is remembered per connection.** A second `register a database table "users"` on the same
connection is refused with `Table 'users' is already registered.` — a second script registering the same
name on the connection the first script made, or a reloaded script that registers without connecting
again. The pair below avoids it by building a fresh connection every time it runs:

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"

    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
```

`create a connection` builds a new connection each time it runs, and a connection starts with nothing
registered, so reloading a script that connects and then registers works: the reload replaces the
connection rather than registering into the old one. What is refused is a registration that meets a
connection something else is still holding. See [Connections](connections.md).

## Failures

`register a database table` has no `and wait`: it always waits, and a failure is exposed as
`last database error` right after it:

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
if last database error is set:
    send "Table registration failed: %last database error%" to console
```

An NBT column is refused here on a server without SkBee, rather than failing later on the first row
that touches it; see [Types](types.md).
