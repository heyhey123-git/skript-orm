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

**A declaration the table cannot serve is refused at registration.** Because the table above is
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

The comparison asks one question: can this table serve every statement the declaration allows? So it
requires every declared column to be there, with a storage the declaration's values fit — the type it names,
and at least the size it names, because a column with more room holds what a narrower declaration writes
while a narrower one does not; it requires `not null` on declared columns that are not the key; and it
requires the table to guarantee the declared key. A table keyed on a column the declaration does not mark as
a key is refused, because a statement that addresses a row by the declared key could then match several
rows. A declaration with no key asks nothing about identity, so a key the table carries is not a difference
either. A `uuid` is the exception to the size: its `BINARY(16)` is a width rather than a capacity, and a
wider column is handed back padded, so the table has to hold exactly that.

What it does not compare is `auto increment`, which the two servers report through different metadata, and
columns the table carries that the declaration never names. Those are not a difference: a table shared with
another tool, or one an older declaration of the same script created with columns this one no longer names,
answers every statement the declaration can make. The one exception is a column of that kind that is
`not null` with no default — an insert that leaves it out is refused by the server, and its message names
the column. To change the schema, run `ALTER TABLE` yourself, or drop the table in a development database
and let the plugin recreate it.

**Registrations belong to a connection.** Registering `"users"` again on a connection that already holds it
does nothing and succeeds: the declaration is accepted, the registered schema is left as it is, and no error
is reported. This is what makes a script that declares its tables in `on load` safe to reload — the tables are
already registered on the connection it reaches, and re-declaring them is the expected result rather than a
mistake.

To change a schema, reconnect so the registration is created afresh, or drop the table and let the plugin
create it again. Re-declaring a table with different columns does not change the existing one, so a table
whose declaration was edited keeps the schema it was created with:

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

`create a connection` always creates a fresh connection with no registered tables. A script that connects
before registering therefore registers on a connection the plugin just made, and one that only registers —
reaching a connection that already holds the tables — is a no-op rather than an error. See
[Connections](connections.md).

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
