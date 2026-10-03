# Troubleshooting

[简体中文](troubleshooting.zh-CN.md) | **English**

Some problems are easy to miss: an operation has no effect, a declared column cannot be read, or a table was never created. Each entry below explains the symptom, its cause, and what to do next.

## "I added a column and nothing changed"

Tables are created with `CREATE TABLE IF NOT EXISTS`. Registration never alters an existing table: the new column is added to the plugin's definition, but not to the database, and no warning is issued.

Operations that use the new column will then fail. Reading the table may also fail because the database result has no such column.

Migrate the database yourself:

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

If the data in a development database is disposable, you can drop the table and let the plugin recreate it. For a live database, use your existing migration tools; the plugin does not handle migrations. See [Tables](tables.md).

## "Table 'users' not found."

The table has not been registered **on the current connection**. The script responsible for connecting and registering may not have run, or it may have failed earlier; check `last database error` at the point of failure. Another possibility is that the connection was replaced, leaving the new connection with no registered tables.

## "No database connected."

There is no active connection: none was created, the connection attempt failed, or the connection was closed. A failed connection attempt also leaves the previous connection closed, so incorrect credentials can affect every script sharing that connection. See [Connections](connections.md).

If named connections exist but none is the default, the error says so and lists their names. Use `use connection "logs"` or `make connection "logs" the default`; there is no need to create another connection.

## A column reads as unset, and the row exists

Skript deletes list-variable keys whose values are null, so a NULL column has no key in the result. `{_user::age} is not set` can mean "age is NULL" or "no row", not "age is zero". Check a column that cannot be NULL to distinguish the two:

```sk
if {_user::id} is not set:
    send "No such row."
else if {_user::age} is not set:
    send "The row exists, the age is NULL."
```

See [Types](types.md).

## `{_users::name}` is empty after `select many`

`select many` results start with a one-based row index, as in `{_users::1::name}`. Even a single matching row needs that index. There is no row-count expression for these results: `size of {_users::*}` counts first-layer values, but each row is a sub-list. Walk the row indices or keep your own counter. See [Reading rows](reading.md).

This is how Skript handles nested lists, including ones built by a script. Count rows by walking their indices, as shown in the [cookbook](cookbook.md#walk-every-page).

## A `loop 1 to 20` counts nothing

Skript does not support `loop 1 to 20`. It reports a parse error when the script loads and skips the loop, so the loop body never runs. Use `loop 20 times` and its `loop-number` instead:

```sk
loop 20 times:
    if {_users::%loop-number%::id} is not set:
        stop loop
    # loop-number is the row index here, 1 to 20
```

```sk
loop {_indices::*}:
    # loop-value is the row index for this iteration
```

If you already have the indices in a list, you can loop that list instead. A comma-separated list such as `loop 1, 2, 3` also works.

## A delete or update touched every row

`delete entities` and `update entities` allow you to omit `where`, in which case they affect every row the implementation allows. Forgetting the condition is not an error. Add a filter or use `by id`. See [Updating and deleting](updating-and-deleting.md).

## "Data type 'nbtcompound' cannot be used: SkBee is not installed, and it is what provides NBT compounds."

`nbtcompound` requires SkBee. Without it, table registration fails rather than waiting until the first write. Install SkBee or choose another type. Scripts also need SkBee to construct NBT compounds; servers that do not use this type are unaffected. See [Types](types.md).

## "Auto-increment column 'id' must also be a primary key."

`auto increment` requires `primary key` on the same column because the value must identify the row:

```sk
    id: bigint, primary key, auto increment, not null
```

## The id after an insert is unknown

The plugin does not return generated ids. Either assign the id in your script and use `upsert`, or find the inserted row by another column. See [Cookbook](cookbook.md).

## A `date` column lost its time

On SQL implementations, a `date` column stores SQL `DATE`, which discards the time of day. For an exact moment, store a Unix timestamp in a `bigint`, with a consistent choice of seconds or milliseconds, or use a `string` containing a timestamp with a time zone. `timespan` represents a duration, not a point in time. MongoDB's `date` column keeps epoch milliseconds instead. A `time` column represents a Minecraft time of day, not a wall-clock reading. See [Types](types.md).

## Paging skips or repeats rows

A page is an offset into primary-key order, not a snapshot. Inserts or deletes during pagination can shift later rows, causing duplicates or omissions. A keyset filter (`id > {_last}`) requires a stable primary-key sort order. `select many` does not offer `ORDER BY`, so adding that filter alone is not a complete replacement for `select page`.

## "select many read more than 5000 rows ... and stored nothing."

One statement moves at most 5000 rows, and a read past that stores nothing: the result variable is cleared and the refusal is in `last database error`. A `select page` larger than 5000 rows is refused before the query is sent. The ceiling is about the server thread rather than memory — a result is written into a list variable one index at a time, on that thread.

Narrow the read, or walk it a page at a time:

```sk
loop 100 times:
    select page loop-number with size 1000 from table "users" and store the results in {_rows::*}:
        where all:
            active = true
    if last database error is set:
        send "Read failed: %last database error%" to console
        stop loop
    if {_rows::1::id} is not set:
        stop loop
    # ... use this page ...
```

A multi-row write is treated differently: `insert many` past one statement's budget is sent as several statements, and every row is written rather than the batch being cut or refused. See [Reading rows](reading.md#how-many-rows-one-read-may-store) and [Writing rows](writing.md#how-many-rows-one-write-may-send).

Pagination sorts by the registered primary key and rejects tables without one. Page numbers start at 1, and row indices restart at 1 within each page: `{_page::1::name}` is the first row on that page, not the first row in the table. See [Reading rows](reading.md).

## A quick query, and the server still hitched

The query runs in the background, but Skript stores its results on the server thread. The number of column values matters: in one benchmark, storing 5000 six-column rows took about 47 ms, while 5000 two-column rows took about 15 ms. Use a stricter filter or read one page at a time. See [What storing a result costs](reading.md#what-storing-a-result-costs).

## "insert many" of a large list pauses the server

The plugin reads the list variable on the server thread before sending it to the database. In one benchmark, reading 30,000 values took about 10 ms. Larger lists take longer even though the plugin splits the database writes. For large jobs, use `select page` and insert one page at a time. See [How many rows one write may send](writing.md#how-many-rows-one-write-may-send).

## A refused read is not a free read

The ceiling is enforced after the database answers. The statement asks for one row more than it may store, so that 5001 rows can be told from exactly 5000, and it refuses once that row arrives: nothing is stored, but the query was sent and the rows were read. A script that keeps asking for more than the ceiling pays for every attempt. `select page` is the exception, because its page size is known in advance and an oversized page is refused before anything is sent.

## "insert many" got faster, and affected rows stopped storing

If you set `rewriteBatchedStatements=true` in a MySQL URL, Connector/J may report `SUCCESS_NO_INFO` for each row in a rewritten batch. The plugin then leaves the affected-row variable unset because the exact count is unavailable. Remove that URL option if your script needs the count. See [Affected rows](affected-rows.md).

## The generic JDBC type refuses "auto increment"

The generic `"JDBC"` type does not know how to declare an auto-increment column, so registration fails before sending a `CREATE TABLE` statement. Remove `auto increment` from the column declaration and supply its value when inserting rows. Support for a database-specific syntax requires an implementation for that dialect.

## Skript says "Empty configuration section!"

Skript shows this warning when a section has a colon but no indented body.

Forms that take values from a variable or operate by id need no body, so they use no colon. A line in this form is an effect, not a section, and does not trigger the empty-section warning:

```sk
insert one {_user::*} into table "archived_users"
delete one entity from table "users" by id {_id} and wait
select entity from table "users" by id {_id} and store the result in {_user::*}
```

Use a colon only when the statement has an indented `where`, `values`, or other body. Removing the colon from a one-line statement resolves the warning.

## "Limited delete is not supported by this JDBC dialect."

On a generic `"JDBC"` connection, `delete entities` reports this error whether or not you wrote `with limit` or `where`. `update entities` behaves the same way and reports `Limited update is not supported by this JDBC dialect.` No rows are changed.

Use `delete one entity ... by id` or `update one entity ... by id` for individual keys. To update or delete multiple rows with one statement, use a supported connection type such as `"MySQL"`, `"MariaDB"`, `"PostgreSQL"`, or `"MongoDB"`.

## A nested loop's values need the loop's own suffix

Inside nested loops, Skript needs a suffix to identify which loop you mean. An unsuffixed `loop-number` or `loop-value` is ambiguous and the line using it fails to parse. Number the loops from the outside in: `loop-number-1` refers to the outer loop and `loop-number-2` to the inner one. The same applies to `loop-value-1` and `loop-value-2`.

## The table name works on one server and not another

MySQL table names on Linux are usually case-sensitive, depending on the server configuration. Both `register a database table "..."` and every later `table "..."` use the name exactly as written. Keep the spelling and case consistent.
