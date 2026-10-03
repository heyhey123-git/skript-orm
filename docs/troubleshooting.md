# Troubleshooting

[简体中文](troubleshooting.zh-CN.md) | **English**

Some problems are easy to miss: an operation has no effect, a declared column cannot be read, or a table was never created. Each entry below explains the symptom, its cause, and what to do next.

## "I added a column and nothing changed"

Tables are created with `CREATE TABLE IF NOT EXISTS`. Registration never alters an existing table: adding a column to the script does not add it to the database. Registration checks the existing table and reports the missing column in `last database error`.

The table cannot be registered with the new definition until the database table is changed to match it.

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

Skript uses the same row-index layer for nested lists built by a script. To count result rows, walk the indices and check `{_users::%loop-number%::id}` at each one, as shown in the [cookbook](cookbook.md#walk-every-page).

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

On SQL implementations, a `date` column stores SQL `DATE`, which discards the time of day. For an exact moment, store a Unix timestamp in a `bigint`, with a consistent choice of seconds or milliseconds, or use a `string` containing a timestamp with a time zone. `timespan` represents a duration, not a point in time. MongoDB's `date` column keeps epoch milliseconds instead. A `time` column represents a Minecraft time of day, not the time shown on a real-world clock. See [Types](types.md).

## Paging skips or repeats rows

`select page` skips rows in the table's current primary-key order; it does not freeze the rows between page requests. If another operation inserts or deletes rows while the script pages through the table, later pages can repeat or miss rows. Remembering the last `id` and filtering with `id > {_last}` only works as a paging strategy when results are consistently ordered by that primary key. `select many` has no `ORDER BY`, so that filter alone cannot replace `select page`.

## "select many read more than 5000 rows ... and stored nothing."

One read stores at most 5000 rows. If `select many` finds more, the result variable is cleared and the refusal is in `last database error`. A `select page` larger than 5000 rows is refused before the query is sent. The 5000-row storage limit protects the server thread: Skript writes result values into a list variable on that thread.

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

`insert many` has a separate limit: when one write would need more than 30,000 bound values, the plugin splits it into several statements instead of dropping rows. See [Reading rows](reading.md#how-many-rows-one-read-may-store) and [Writing rows](writing.md#how-many-rows-one-write-may-send).

Pagination sorts by the registered primary key and rejects tables without one. Page numbers start at 1, and row indices restart at 1 within each page: `{_page::1::name}` is the first row on that page, not the first row in the table. See [Reading rows](reading.md).

## A quick query, and the server still hitched

The query runs in the background, but Skript stores its results on the server thread. The number of column values matters: in one benchmark, storing 5000 six-column rows took about 47 ms, while 5000 two-column rows took about 15 ms. Use a stricter filter or read one page at a time. See [What storing a result costs](reading.md#what-storing-a-result-costs).

## "insert many" of a large list pauses the server

The plugin reads an `insert many` source variable on the server thread before sending SQL. It now spreads that read across ticks, with at most 4096 processing steps per slice; reading one row can take several steps. Each slice aims for about 2 ms, but an individual value conversion can exceed that budget. A larger list may take more ticks to finish, and other server work can still lengthen a tick. The earlier 10 ms measurement for reading 30,000 values predates sliced reading; it is not the current cost of one tick. For large jobs, `select page` can divide the work into smaller inserts. See [How many rows one write may send](writing.md#how-many-rows-one-write-may-send).

## A refused read is not a free read

The 5000-row `select many` limit is enforced after the database answers. The statement asks for up to 5001 rows to distinguish an oversized result from exactly 5000 rows. If row 5001 arrives, the result variable is cleared, but the query has already run. A script that repeatedly requests oversized results pays for every attempt. `select page` is different: its requested page size is known in advance, so a page larger than 5000 is refused before the query is sent.

## "insert many" got faster, and affected rows stopped storing

This symptom applied to older addon releases that sent `"MySQL"` inserts as JDBC batches. With `rewriteBatchedStatements=true`, Connector/J could report `SUCCESS_NO_INFO`, leaving the affected-row variable unset. A current `"MySQL"` connection sends a parameterized multi-row `INSERT` and obtains its count from that statement; the URL option does not control this path. On an older release using the batch path, remove the option if the script needs an exact count. See [Affected rows](affected-rows.md).

## The generic JDBC type refuses "auto increment"

The generic `"JDBC"` connection has no SQL syntax for declaring an auto-increment column, so registration fails before sending `CREATE TABLE`. Remove `auto increment` from the column declaration and supply its value when inserting rows. To support auto-increment on another database, the addon needs SQL generation written for that database.

## Skript says "Empty configuration section!"

Skript shows this warning when a statement ends with a colon but has no indented body below it.

Forms that take values from a variable or operate by id need no body, so they use no colon. Skript treats each as a standalone statement (an effect), rather than a statement with an indented body (a section). They do not trigger the empty-section warning:

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
