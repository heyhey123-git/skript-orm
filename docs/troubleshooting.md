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

This is Skript's rule rather than the plugin's storage, and it applies to any list of sub-lists however it was written. `size of` reads the list through `Variable#size(Event)`, which takes the number of entries and then subtracts every entry whose value is a map with no scalar of its own — a row is exactly that, so a result of rows reports zero, and so does the same shape built by a script. The case that pins this is `elements/34-result-size.sk` in the server test: it asserts 20 for a flat list the script built, 0 for the stored result, 0 for the same shape built by hand, and 20 for a count taken by iterating the result. To count rows, iterate them — the plugin's own benchmark and cookbook do that too.

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
    if {_rows::1::id} is not set:
        stop loop
    # ... use this page ...
```

A multi-row write is treated differently: `insert many` past one statement's budget is sent as several statements, and every row is written rather than the batch being cut or refused. See [Reading rows](reading.md#how-many-rows-one-read-may-store) and [Writing rows](writing.md#how-many-rows-one-write-may-send).

Pagination sorts by the registered primary key and rejects tables without one. Page numbers start at 1, and row indices restart at 1 within each page: `{_page::1::name}` is the first row on that page, not the first row in the table. See [Reading rows](reading.md).

## A quick query, and the server still hitched

Reading has two halves, and only one of them leaves the server thread: the query runs elsewhere, while the answer is written into the variable on the server thread, one value at a time. That costs about 1.4 microseconds per value, measured, so a 5000-row result of a six-column table is about 47 milliseconds — most of a tick. Values cost, not rows: the same 5000 rows of a two-column table cost about 15 milliseconds.

The 5000-row ceiling holds that cost where it is, and results above 10 000 values are stored more cheaply (about a quarter less of that server-thread time, not of the tick — a cost that fits inside a tick's 50 ms does not lengthen one; the numbers are under [what storing a result costs](reading.md#what-storing-a-result-costs)), but no mode makes storing a large result free. To keep a read short, narrow the result — a stricter filter, fewer columns in the table, or one page at a time. See [How many rows one read may store](reading.md#how-many-rows-one-read-may-store).

## "insert many" of a large list pauses the server

Reading the batch out of the variable happens on the server thread, at about 0.33 microseconds per value, measured, and it cannot move: a variable name may contain an expression, which Skript resolves only there. A batch of 30 000 values is about 10 milliseconds of pause before the first statement is sent, and a batch past the budget becomes several statements, each pausing about that much. `insert many` writes every row rather than refusing, so the total pause follows the size of the whole batch rather than the budget.

Keep one write to one page: walk the source with `select page` and insert each page, which also gives a failure a natural boundary. See [How many rows one write may send](writing.md#how-many-rows-one-write-may-send).

## A refused read is not a free read

The ceiling is enforced after the database answers. The statement asks for one row more than it may store, so that 5001 rows can be told from exactly 5000, and it refuses once that row arrives: nothing is stored, but the query was sent and the rows were read. A script that keeps asking for more than the ceiling pays for every attempt. `select page` is the exception, because its page size is known in advance and an oversized page is refused before anything is sent.

## "insert many" got faster, and affected rows stopped storing

You read that the MySQL driver can rewrite a batch, added `rewriteBatchedStatements=true` to the url, and the insert really did get several times faster — but `and store affected rows` now stores nothing.

Connector/J 9.5.0 merges the batch into fewer statements before sending it. The count that comes back then describes the merged statement rather than the rows you sent, and the driver does not split it back up: the `numBatchedArgs > 1` branch of `ClientPreparedStatement` fills every entry of the batch update count with `SUCCESS_NO_INFO` and discards the row count the server reported. The plugin reads `SUCCESS_NO_INFO` as “no number” rather than guessing, so the variable is cleared and stays empty. A count that is quietly wrong would be worse than no count.

Remove the option from the url. It is not a portable setting but a property of one driver, and its name is not one the other drivers know: `"MariaDB"` reaches the server's bulk execute without any option and still counts every row, and the PostgreSQL driver spells its equivalent `reWriteBatchedInserts`. A url is also the wrong place to fix this, because one batch is sent by the same code on every backend; a real fix has to be made in the plugin. See [Affected rows](affected-rows.md) and [How many rows one write may send](writing.md#how-many-rows-one-write-may-send).

## The generic JDBC type refuses "auto increment"

Nearly every database spells a self-filling column differently, so each dialect answers for itself: `JdbcDialect.autoIncrementClause()` refuses with `Auto increment` unless a dialect overrides it, and only some do — the MySQL dialect answers `AUTO_INCREMENT`, the PostgreSQL one answers `GENERATED BY DEFAULT AS IDENTITY`. A database reached through the generic JDBC type, H2 or SQLite for instance, gets the refusal, and the create statement is rejected before it reaches the database.

Give the column an explicit value in the declaration and leave `auto increment` out of it. If the database has a spelling of its own, the dialect is the place to add it: one override, and every table declaration through that type can use it.

## Skript says "Empty configuration section!"

Skript warns when a section has no indented content beneath its colon, regardless of which plugin provides it. The message comes from Skript's parser, and its control flag is internal; neither a config file nor a script can disable it.

Forms that take values from a variable or operate by id need no body, so they use no colon. A line in this form is an effect, not a section, and does not trigger the empty-section warning:

```sk
insert one {_user::*} into table "archived_users"
delete one entity from table "users" by id {_id} and wait
select entity from table "users" by id {_id} and store the result in {_user::*}
```

Use a colon only when the statement has an indented body. To read a single row, you can supply a filter that covers the rows you want; this example suits a table whose ids start at 1:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        id >= 1
```

Sections with a `where` block, `values` block, or other body need no changes. Older scripts with empty sections still run but still warn. For the one-line forms above, removing the colon resolves the warning.

## "Limited delete is not supported by this JDBC dialect."

The generic `"JDBC"` type has no portable way to write a delete bounded by a row limit. MySQL and MariaDB append `... LIMIT n`, PostgreSQL picks the rows with a `ctid` subquery first, and MongoDB picks their ids first; the portable dialect has none of those, so `JdbcDialect.applyDeleteLimit()` answers with `Limited delete` and the statement is rejected before it reaches the database. Nothing is deleted — not even in part — and the message is in `last database error`.

**The limit is not the only thing that triggers it.** On the generic type `delete entities` is refused whether or not the script wrote a limit. Measured on this plugin's own server, over SQLite through a generic connection, by `elements/31-statement-shape.sk`:

| statement | what came back |
| --- | --- |
| `delete entities from table "..." and wait` | `Limited delete is not supported by this JDBC dialect.`, no affected-row count |
| the same with a `where all:` block | the same message, no affected-row count |
| the same with `with limit 1` | the same message |

So a filtered delete without a limit is not the workaround it looks like, and it is not only deletes: an `update entities` written the same way comes back `Limited update is not supported by this JDBC dialect.`, which the same file measures. The dialect on its own is not the whole story either: `JdbcDialect.delete()` runs a plain `DELETE FROM t` when it is handed no limit, so what reaches it from these elements is not nothing. Which value is handed over, and where it comes from, is not identified here; what is measured is that the message is the dialect's rather than the `Delete limit must be positive.` one a zero limit produces, and that no rows are reported as affected.

No property or driver setting gives the dialect the form it lacks, so this is a real gap rather than a configuration mistake. To act on all rows, use a type whose dialect answers: `"MySQL"`, `"MariaDB"`, `"PostgreSQL"` and `"MongoDB"` all do. On the generic type, delete by key instead: read the keys you mean to remove — `select page` over the table, or a walk of the key range — and run `delete one entity ... by id {_key}` for each one. `by id` takes no limit, and the batch size is yours to choose.

## A nested loop's values need the loop's own suffix

The plain spelling of a loop expression is ambiguous inside a nested loop, and Skript refuses the **line that reads it** while parsing — not the loop. Measured on Skript 2.16.2, the build this plugin compiles against:

```
[Skript] Line 21: (elements/32-loop-value.sk)
    There are multiple loops that match loop-number. Use loop-number-1/2/3/etc. to specify which loop's value you want.
    Line: set {_plain} to "%loop-number% of %loop-number-2%"
```

The line is dropped, so the body around it looks as if it never ran and a batch filled there comes out empty — which is how this was first seen in a benchmark script, and why the six sizes in `server-benchmark/skript/10-curve.sk` are written out one after another instead of looped over. The loops themselves are fine, and the suffixes work. They count from the **outermost** loop inward: in `loop 2 times:` around `loop 3 times:` the outer loop's last number is `loop-number-1` (2) and the inner loop's is `loop-number-2` (3); in `loop 3 times:` around `loop 2 times:` the values are `loop-value-1` = 3 for the outer loop and `loop-value-2` = 2 for the inner one. Both directions are asserted by `elements/32-loop-value.sk`.

## Setup inside an `if`

An earlier version of this page said that `create a connection` and `register a database table` written inside an `if` block do nothing. That did not reproduce when it was measured: with the connection made inside `if <condition>:` and a statement written through it **outside** the block, the statement ran and reported one affected row, and an `insert` written inside an `if` reported one row too. `elements/31-statement-shape.sk` asserts both counts, so the shape is covered by a run rather than by a note. A statement inside an `if` is not skipped, so the guard each element's `walk` opens with is not the cause of what was seen. What the old observation ran is not recorded, so there is nothing left to reproduce from.

## The table name works on one server and not another

MySQL table names on Linux are usually case-sensitive, depending on the server configuration. Both `register a database table "..."` and every later `table "..."` use the name exactly as written. Keep the spelling and case consistent.
