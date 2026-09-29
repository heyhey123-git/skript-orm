# Tables

[简体中文](tables.zh-CN.md) | **English**

Define the table in the script that creates the connection. The plugin uses this definition to build
statements and name the keys in query results.

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

On SQL backends, registration runs `CREATE TABLE IF NOT EXISTS`, waits for completion, and then reads the
table back from the server and compares it with the declaration.

**Existing tables are left unchanged.** Adding a column to the script and reloading updates the plugin's
definition, not the database schema. Registration produces no warning because it has succeeded, and
`CREATE TABLE IF NOT EXISTS` is what makes registering the same table on every startup safe.

**A declaration that no longer matches the table is refused at registration.** Because the table above is
never altered, a script that adds `age: int` to a table that already exists without it would otherwise
fail later, at the first statement that names the column, with the server's
`Unknown column 'age' in 'INSERT INTO'` — which names the write rather than the registration. Instead,
`register a database table` fails right there and `last database error` names the difference:

```
Registered table 'users' does not match the table in the database. Registration is 'CREATE TABLE IF
NOT EXISTS', so a table that already exists is never changed, and every statement that uses the column
or type below will fail.
Column(s) 'age' are declared but missing from the table. The table holds: 'id', 'name'.
Drop the table and register it again, or change the table in the database to match this declaration.
```

The comparison covers which columns exist, each column's type and size, `not null` on columns that are
not the key, and which column is the primary key. It does not compare `auto increment`, which the two
servers report through different metadata. To change the schema, run `ALTER TABLE` yourself, or drop the
table in a development database and let the plugin recreate it.

**Registrations belong to a connection.** Registering `"users"` again on the same connection fails with
`Table 'users' is already registered.`. This can happen when two scripts register the same table on a
shared connection, or when a script is reloaded without recreating its connection.
The following example avoids duplicate registration by creating a fresh connection each time:

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

`create a connection` always creates a fresh connection with no registered tables. A script that
connects before registering can therefore be reloaded: it replaces the connection rather than
registering on the old one. Only duplicate registration on the same connection is rejected.
See [Connections](connections.md).

A statement that is not built from a registration is a [raw statement](raw-statements.md). It is sent as
written, so it is not compared with the table it names — not the columns, not the types, not the table's
existence. Everything on this page is about the declared form, which is the one that is checked.

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
