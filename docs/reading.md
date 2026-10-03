# Reading rows

[简体中文](reading.zh-CN.md) | **English**

Four statements read rows: `select one`, `select many`, `select page` and `select entity ... by id`. All wait for the query to finish, so subsequent statements can use the result immediately.

## Select one

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
send "name: %{_user::name}%, age: %{_user::age}%"
```

Each column becomes a key in the variable, using the column name. The `where` block is optional; without it, the database chooses which row to return. To select a specific row reliably, query by a primary key or unique column.

## Select many

```sk
select many entities from table "users" and store the results in {_users::*}:
    where all:
        active = true
send "first: %{_users::1::name}%, second: %{_users::2::name}%"
```

Results use a one-based row index followed by the column name, such as `{_users::1::name}`. The index remains even if only one row matches, so the structure is consistent. There is no built-in row-count expression for this `rowIndex::column` structure: `size of {_users::*}` counts top-level values, whereas each row is a sub-list. Count the row keys instead, or maintain a separate counter.

`select many` has no `ORDER BY`, so the database determines which row becomes `::1`. Sort the results in your script if order matters. Of the reads described here, only `select page` guarantees an order.

## Select page

```sk
select page 2 with size 20 from table "users" and store the results in {_page::*}:
    where all:
        active = true
```

- Both the page number and page size must be at least 1. `page 1` is the first page; a size of 20 means twenty rows per page.
- Row indices restart on each page, so `{_page::1::name}` is the first row **of that page**, not of the table.
- Results are returned in **ascending primary-key order**. Pagination therefore requires a registered primary key, giving all backends a consistent ordering rule.
- A page beyond the last page returns an empty result, not an error.
- A page may hold at most **5000 rows**, and a page size above that is refused before the query runs. See [How many rows one read may store](#how-many-rows-one-read-may-store).
- Each page runs a new query, sorts by primary key, and skips the rows covered by earlier pages. It does not preserve the data as it was on the first read. Inserts or deletions between reads can shift later rows, causing duplicates or omissions. Another approach is to remember the last primary key read and query `id > {_last}`, but that query must also sort consistently by primary key. `select many` does not provide `ORDER BY`, so the condition alone cannot reliably page through the table.

## How many rows one read may store

A read can store at most **5000 rows**. If more rows match, it clears the result variable and sets `last database error`; it never returns a partial result.

This limit protects the server thread. The query runs asynchronously, but Skript stores the result in variables on the server thread. In a benchmark with a six-column table, storing 5000 rows took roughly 36–47 ms of server-thread time; a 10,000-row read was refused. The cost depends on the number of values and the server's Skript installation. See [What storing a result costs](#what-storing-a-result-costs) and the write limit in [Writing rows](writing.md#how-many-rows-one-write-may-send).

Narrow the result, or walk it a page at a time:

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

### What storing a result costs

Skript normally stores each result value separately on the server thread. On the benchmark server, this took about 1.0–1.4 microseconds per value, depending on the variable type. A six-column row contributes six values; storing 5000 such rows took about 47 ms. A two-column result of the same length took about 15 ms. These measurements describe work on the server thread, not a guarantee about tick length on another server.

For results above **10,000 values**, the plugin can arrange the result into the list variable's nested structure in the background, then attach it to Skript's global variable store on the server thread. In the same test, this reduced server-thread time for 30,000 values from 47 ms to 36 ms. The optimization has these limits:

- It applies to **global list variables**. A result stored in a local or ephemeral variable is written value by value.
- The **first** qualifying result after startup is stored value by value while the plugin checks compatibility with Skript's variable store.
- If the store is incompatible, every result uses the ordinary value-by-value path. No configuration change is needed.

Whether values are stored individually or attached together, the plugin notifies Skript to save each value in order. The optimization does not skip the normal variable-saving process.

## Select by id

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
```

`select entity ... by id` looks up the registered primary key directly and does not accept a `where` block. No result is stored if the key does not exist. A primary-key lookup has no indented body, so it needs no colon. `select one`, `select many`, and `select page` also need no colon when they have no `where` block; see [writing rows](writing.md).

## Where blocks

A `where` block contains one condition per line. Under `where all:`, every condition must hold; under `where any:`, at least one must hold. Both can be negated: `where not all:` means at least one condition does not hold, while `where no any:` (or `where not any:`) means none holds.

```sk
select many entities from table "users" and store the results in {_users::*}:
    where any:
        name = "Alice"
        age > 30
        joined between {_from} and {_to}
```

| Condition | Meaning |
| --- | --- |
| `column = value` | Equal. Comparing with `null` means “is NULL”. |
| `column != value` | Not equal. |
| `column > value`, `column >= value` | Greater than, greater than or equal to. |
| `column < value`, `column <= value` | Less than, less than or equal to. |
| `column between a and b` | Inclusive range. |

Values can be expressions, including `arg-1`, `{_cutoff}` and `now`. They are evaluated when the block runs.

## Empty results and NULL columns

Both of these cases leave a key unset:

- **No row matched** a `select one`, so no result was stored.
- **The column is NULL** in the matching row.

Failures also affect the result variable:

- **The statement fails**, for example because the database rejects the query or the result cannot be read: the variable is cleared, and `last database error` contains the reason.
- **The statement is rejected before execution**, for example because there is no connection, the table is unknown, a `where` value does not fit its column, or the page number is zero: the variable is also cleared. A read leaves only its own result or an empty variable, never a previous result.

After confirming the query succeeded, check a non-NULL column such as the primary key to distinguish a missing row from a NULL column:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
if last database error is set:
    send "Lookup failed: %last database error%" to sender
    stop
if {_user::id} is not set:
    send "No such user." to sender
    stop
if {_user::age} is not set:
    send "That user has no age stored." to sender
```

Always check `last database error` before treating an unset key as a missing row or a NULL column.

## Failures

Reads always wait and report failures through `last database error`, which is available to the next statement. They also accept `and wait`, but it does not change their behaviour. See [Errors and waiting](errors-and-waiting.md).
