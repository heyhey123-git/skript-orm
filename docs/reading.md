# Reading rows

[简体中文](reading.zh-CN.md) | **English**

Four statements read rows: `select one`, `select many`, `select page` and `select entity ... by id`. All
of them wait for their result, so the lines after one already have the data.

## Select one

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
send "name: %{_user::name}%, age: %{_user::age}%"
```

Each column of the row becomes one key of the variable, named after the column. The `where` block is
optional; without it, one row is taken from the table. Which row that is depends on the database, so a
read that has to be predictable should name a primary key or a unique column.

## Select many

```sk
select many entities from table "users" and store the results in {_users::*}:
    where all:
        active = true
send "first: %{_users::1::name}%, second: %{_users::2::name}%"
```

Rows are keyed by a one-based row index and then the column, such as `{_users::1::name}`. The index is
there even when only one row matched, so `select many` results are always read the same way. There is no
count expression for a `rowIndex::column` result: `size of {_users::*}` counts first-layer values, and
every row is a sub-list, so it does not count rows. Take the number of rows from the row keys themselves,
or keep your own counter.

A `select many` has no `ORDER BY`, so which row ends up as `::1` is the database's business. Sort in the
script when the order matters; `select page` is the one read that has an order of its own.

## Select page

```sk
select page 2 with size 20 from table "users" and store the results in {_page::*}:
    where all:
        active = true
```

- The page number and the size both start at one: `page 1` is the first page and a size of 20 means
  twenty rows.
- Keys are page-local, so `{_page::1::name}` is the first row **of that page**, not of the table.
- Rows come in **primary-key order, ascending**, which is also why pagination needs a registered primary
  key: it is the order every backend can agree on.
- A page past the end is empty rather than an error.
- A page is an **offset into that order, not a snapshot**: a row written or deleted between two page reads
  shifts everything behind it, so a row can be seen twice or missed. While a table is being written to,
  walk it with a key-set filter (`id > {_last}`) instead.

## Select by id

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
```

This one takes no `where` block: it looks the row up by the registered primary key. Nothing is stored
when no row has that value. It has no body either, so it is written without a colon, the way
[writing rows](writing.md) explains; the same goes for a `select one`, `select many` or `select page`
with no `where` block.

## Where blocks

A `where` block holds one condition per line, under `where all:` or `where any:`. All conditions must
hold, or at least one must, respectively. Either header can be negated: `where not all:` asks for "at
least one does not hold", and `where no any:` (or `where not any:`) for "none of them holds".

```sk
select many entities from table "users" and store the results in {_users::*}:
    where any:
        name = "Alice"
        age > 30
        joined between {_from} and {_to}
```

| Condition | Meaning |
| --- | --- |
| `column = value` | Equal. Against `null` this means "is NULL". |
| `column != value` | Not equal. |
| `column > value`, `column >= value` | Greater than, or at least. |
| `column < value`, `column <= value` | Less than, or at most. |
| `column between a and b` | Inclusive range. |

The value side is an expression, so `arg-1`, `{_cutoff}` and `now` all work, and it is evaluated when
the block runs.

## Empty results and NULL columns

Both look the same from a script, and both mean a key is unset:

- **No row matched** on a `select one`, so nothing was stored.
- **The column is NULL** in the row that matched.

What happened to the result variable otherwise is worth knowing too:

- **The statement failed** — the database refused the query, or the result could not be read: the variable
  was cleared, and `last database error` says why.
- **The statement was refused before it ran** — no connection, an unknown table, a `where` value the column
  cannot hold, a page number of zero: the variable was cleared as well. A read either leaves this
  statement's result in the variable or nothing at all, never the one before it.

To tell them apart, look at a column that cannot be NULL, such as the primary key:

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

`last database error` is what says whether the statement ran at all: an unset variable on its own means
"no row, or a NULL column", which is what the two checks above are for.

## Failures

A read always waits and always exposes its failure, so `last database error` right after it says what
went wrong. An `and wait` on a read is accepted and changes nothing, because it waits either way. See
[Errors and waiting](errors-and-waiting.md).
