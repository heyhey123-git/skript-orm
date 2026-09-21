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
- Pages use an **offset into the sorted rows, not a snapshot**. Inserts or deletions between reads can shift later rows, causing duplicates or omissions. For a table being modified during traversal, consider a primary-key cursor (`id > {_last}`), but also ensure stable primary-key ordering. The condition alone is not enough, and `select many` does not provide `ORDER BY`.

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
