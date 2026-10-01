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
- Pages use an **offset into the sorted rows, not a snapshot**. Inserts or deletions between reads can shift later rows, causing duplicates or omissions. For a table being modified during traversal, consider a primary-key cursor (`id > {_last}`), but also ensure stable primary-key ordering. The condition alone is not enough, and `select many` does not provide `ORDER BY`.

## How many rows one read may store

One read stores at most **5000 rows**. A result larger than that is **refused**: nothing is stored, the result variable is cleared, and `last database error` names the ceiling and says what to do about it.

The ceiling is not about memory. Skript writes a list variable one index at a time, on the server thread, so the size of a result is spent out of the tick budget of the server itself: a hundred thousand rows of a six-column table written that way is about a second of the server stopped, most of which is the writing. 5000 rows is the largest result that stays inside a hitch nobody notices — measured, about fifty milliseconds of a six-column table — and it is the same batch of values one write statement may bind — see [Writing rows](writing.md#how-many-rows-one-write-may-send). A result above ten thousand values is stored more cheaply than this: it is built off the server thread and attached in one step, which measured about a quarter less of the tick, and on a Skript whose variable store differs from what that path needs it falls back to writing value by value.

That the boundary sits where it does is an observation rather than a reading of the constant. On the benchmark server — the generic JDBC connection, which on the machine that ran it is SQLite — reads of 100, 500, 1000, 2500 and 5000 rows of a six-column table each stored every row they asked for, and in none of them did the longest tick inside the statement exceed a tick's own length; the same read at 10 000 rows stored nothing and was refused, with `select many read more than 5000 rows from table 'orm_bench_probe' and stored nothing.` The script's own wall clock over those reads was 50 to 100 milliseconds each.

A refusal is preferred over a truncated result because the two cannot be told apart afterwards. A script handed the first 5000 rows of a table goes on to answer questions about rows it never saw, and the answer is wrong rather than missing.

Narrow the result, or walk it a page at a time:

```sk
loop 100 times:
    select page loop-number with size 1000 from table "users" and store the results in {_rows::*}:
        where all:
            active = true
    if {_rows::1::id} is not set:
        stop loop
    # ... use this page ...
```

### What storing a result costs

The query runs off the server thread; storing its answer does not. Skript writes a list variable one value at a time, and only on the server thread, because the name of a variable may contain expressions that only that thread can resolve. Measured on this plugin's own test server, one value costs about 1.4 microseconds in a global variable (`{users::*}`), 1.1 in an ephemeral one (`{-users::*}`) and 1.0 in a local one (`{_users::*}`). A six-column result is six values per row, so 5000 rows is 30 000 values, about 47 milliseconds of the server — most of a tick. Values are what cost, not rows: 5000 rows of a two-column table cost about 15 milliseconds.

Where that time goes is worth knowing before trying to shrink it. About a quarter is persistence — serializing the change and queueing it for the save — together with the lock and the bookkeeping around the store, and the rest is Skript's own variable tree, which is what the store is for. Only the tree can be built somewhere else.

**Above 10 000 values the result is built that way.** The tree is assembled on the thread that read the rows, and the server thread then attaches it in one step: it takes the write lock once, detaches what the variable held, and puts the new subtree in its place. Measured, that is about a quarter less of the tick — 30 000 values went from 47 to 36 milliseconds, and 9000 from 14.9 to 12.8 — while below the threshold the two ways measure the same (600 values: 1.25 milliseconds against 1.35). That is why there is a threshold rather than one way of storing everything.

The path has conditions, and the plugin checks them on a real result instead of assuming them:

- It applies to **global list variables**. A result stored in a local or ephemeral variable is written value by value.
- The **first** result above the threshold after the server starts is written value by value. The technique depends on the running Skript's variable store looking a particular way, which the plugin confirms once before using it.
- If it does not look that way — a different Skript version, or another plugin replacing the store — every result is written value by value. Nothing else changes and no setting has to be adjusted.

The properties of the store are kept either way: the change still notifies persistence for every value, in the order the values arrived, so a saved variable and a variable in memory do not drift apart.

## Select by id

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
```

This statement looks up the registered primary key directly and does not accept a `where` block. No result is stored if the key does not exist. It has no body, so it needs no colon, as explained in [writing rows](writing.md). The same applies to `select one`, `select many` and `select page` without a `where` block.

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
if {_user::id} is not set:
    send "No such user." to sender
    stop
if {_user::age} is not set:
    send "That user has no age stored." to sender
```

Check `last database error` first. The two checks above distinguish a missing row from a NULL column only after a successful query; an unset variable alone does not rule out failure.

## Failures

Reads always wait and report failures through `last database error`, which is available to the next statement. They also accept `and wait`, but it does not change their behaviour. See [Errors and waiting](errors-and-waiting.md).
