# Troubleshooting

[简体中文](troubleshooting.zh-CN.md) | **English**

The traps in this plugin are mostly quiet ones: an operation that did nothing, a column that is there
but unreadable, a failure that was only logged. Each entry below is a symptom, what causes it, and what
to do.

## "I added a column and nothing changed"

The table is created with `CREATE TABLE IF NOT EXISTS`, and registering never alters a table that
already exists. The plugin accepts the new column into its own description, the database does not grow
one, and there is no warning.

What you see afterwards: operations that mention the new column fail, and a read of the table may fail
because the result has no such column.

Fix it by migrating the database yourself:

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

In a development database, dropping the table and letting the plugin create it again is quicker. For a
live database, keep the schema under whatever migration tooling you already use; the plugin is not one.
See [Tables](tables.md).

## "Table 'users' is already registered."

The registration is remembered per connection, so a script that reloads and registers again is refused.
Connect first, which gives a fresh connection with nothing registered:

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

## "Table 'users' not found."

The table was never registered **on this connection**. Usual causes: the script that connects and
registers never ran, it failed earlier (check `last database error` at that point), or the connection
was replaced since, which starts with no tables registered.

## "No database connected."

There is no current database: nobody connected yet, the connection failed, or something disconnected.
Since a failed connection leaves the previous one closed, a single bad credential can put the server in
this state for every script. See [Connections](connections.md).

## A column reads as unset, and the row exists

A NULL column leaves its key unset, because Skript deletes a list-variable key whose value is null. So
`{_user::age} is not set` means "NULL or no row", not "zero". Check a column that cannot be NULL to tell
the two apart:

```sk
if {_user::id} is not set:
    send "No such row."
else if {_user::age} is not set:
    send "The row exists, the age is NULL."
```

See [Types](types.md).

## `{_users::name}` is empty after `select many`

`select many` keys rows by a one-based row index first: `{_users::1::name}`. A single matching row still
has index 1. Use `size of {_users::*}` for the count. See [Reading rows](reading.md).

## The read after a write shows the old data

The write had no `and wait`, so it was still running when the read started. Reads wait, unwaited writes
do not. Add `and wait` to the write. See [Errors and waiting](errors-and-waiting.md).

## A failure was never reported

`last database error` says nothing, but the console has the failure. That is what an unwaited write
does with a database-side failure: it logs instead of reporting. The same is true of a `where` value or
a column type problem that only the database can judge. Either add `and wait`, or read the console; see
[Errors and waiting](errors-and-waiting.md).

## A delete or update touched every row

`delete entities` and `update entities` accept a missing `where` block and then act on every row the
implementation allows. A forgotten `where` is not an error. Write the conditions, or use the `by id`
form. See [Updating and deleting](updating-and-deleting.md).

## "Data type 'nbtcompound' needs SkBee, which is not installed."

The column type depends on SkBee, and registering the table is refused rather than letting the first
row fail later. Install SkBee, or use a different type. Note that without SkBee a script cannot build an
NBT compound in the first place, so this only matters on a server where the column was meant to be used.
See [Types](types.md).

## "Auto-increment column 'id' must also be a primary key."

`auto increment` needs `primary key` on the same column, because the value has to identify the row:

```sk
    id: bigint, primary key, auto increment, not null
```

## The id after an insert is unknown

The plugin does not hand back a generated id. Either write the value yourself and use `upsert`, or look
the row up again by another column. See [Cookbook](cookbook.md).

## A `date` column lost its time

A `date` column is a SQL `DATE`, so the time of day is not stored. Use a `bigint` of epoch milliseconds
or a `timespan` when the exact moment matters. A `time` column is a Minecraft time of day, not a wall
clock reading either. See [Types](types.md).

## Paging skips or repeats rows

Pagination orders by the registered primary key, so a table without one is refused, and pages are
one-based. Keys inside a page restart at 1 (`{_page::1::name}`), which is easy to mistake for the first
row of the table. See [Reading rows](reading.md).

## The table name works on one server and not another

On Linux, MySQL table names are case-sensitive. The name in `register a database table "..."` is used as
written, and so is every `table "..."` afterwards. Keep one spelling.
