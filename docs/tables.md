# Tables

[简体中文](tables.zh-CN.md) | **English**

Register a table after creating the connection that will use it. The plugin uses the table definition
to build statements and name the keys in query results.

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
  table name after `register a database table` follows this rule too; the table name is checked when the
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

## What registration does

On SQL backends, registration runs `CREATE TABLE IF NOT EXISTS`, waits for completion, and then reads the
table back from the server and compares it with the declaration.

**Existing tables are never altered.** Adding a column to the script and reloading does not add it to
the database. Repeating an unchanged definition succeeds without changing the table; a changed
definition is checked against the existing table structure and may fail.

**Incompatible definitions fail during registration.** If a script declares `age: int` but the
existing table has no `age` column, `register a database table` fails immediately. The message in
`last database error` identifies the mismatch:

```
Registered table 'users' does not match the table in the database. Registration is 'CREATE TABLE IF
NOT EXISTS', so a table that already exists is never changed, and every statement that uses the column
or type below will fail.
Column(s) 'age' are declared but missing from the table. The table holds: 'id', 'name'.
Drop the table and register it again, or change the table in the database to match this declaration.
```

The check requires every declared column to exist with a compatible type and enough space for its
values. It also checks `not null` on non-key columns and verifies that the database can enforce the
declared primary key. A declaration without a primary key can still match a table that has one.
For `uuid`, the `BINARY(16)` width must match exactly: a wider column would return padded values.

The check does not compare `auto increment`, because database implementations report it differently.
It also allows extra columns in the database. An extra `not null` column without a default may still
cause inserts to fail. To change the table structure, run `ALTER TABLE` yourself, or drop a development table
and let the plugin recreate it.

**Registration is per connection.** Repeating the same definition on a connection succeeds without
another database check, so a script can register its tables again when reloaded.

To change the table structure, update the database directly, or drop a development table and let the plugin
recreate it. Reconnecting refreshes the connection's registrations, but does not alter an existing
database table. For example:

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

`create a connection` starts with no registered tables. The `register a database table "users"` line
registers `users` on that connection; run it again after reconnecting. Repeating the same definition
on a connection that already has it is harmless. See [Connections](connections.md).

[Raw statements](raw-statements.md) bypass registration and table structure checks. They are sent as written, without checking
the named table or its columns against a registration.

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

Without SkBee, `register a database table` refuses a `nbtcompound` column instead of waiting for a
read or write to fail; see [Types](types.md).
