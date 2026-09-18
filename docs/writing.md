# Writing rows

[简体中文](writing.zh-CN.md) | **English**

Five statements write rows: `insert one`, `insert many`, `insert entity if absent`, `upsert one entity`
and, for existing rows, `update`. They share one way of describing the row.

## The values block

Values are written as `column: expression` lines, either directly in the section body or inside a
`values:` block:

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 1 + 24
        joined: now
```

- The right-hand side is any Skript expression, so variables, arguments and functions work.
- Every line must be `column: expression` on one line; a nested block is only allowed where rows are
  expected (see `insert many`).
- **A column that is left out is not part of the statement.** An `insert` leaves it to the database
  default, which is how an auto-increment key stays automatic, while an `update` or an `upsert by id`
  leaves it at its stored value. Writing `null` instead stores SQL NULL; see [Types](types.md).
- **A column that is named but resolves to nothing is written as SQL NULL.** `name: {_nick}` with
  `{_nick}` unset is not the same as leaving `name` out: the key is there with no value, and the statement
  stores NULL (or fails on a `not null` column). Only leaving the line out keeps the stored value.
- **A list variable cannot carry SQL NULL.** Skript deletes a key that is set to null, and a select leaves
  the key of a NULL column unset, so a row copied out of a select result and written back from a variable
  has no value for those columns at all: an `insert` gives them the database default — and fails on a
  `not null` column that has none — while an `update` leaves them untouched. A `values` block with a
  literal `null` is the only way to write NULL.
- An unknown column name fails before anything is sent to the database.

## Insert one

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "Insert failed: %last database error%" to console
```

The same section also takes the row from a variable shaped like a select result:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users"
```

Such a variable must hold exactly one row; a variable holding several rows is refused here and belongs
in `insert many`.

There is nothing to indent under that last line, so it is written without a colon, and Skript reads a
line without one as an effect. An empty section is what Skript warns about, and the two spellings do the
same thing: keep the colon when the statement has a body to give, leave it out when it has none. The
same rule holds on the [reading](reading.md) and [updating and deleting](updating-and-deleting.md)
pages.

## Insert many

Each nested block under `values:` is one row:

```sk
insert many entities into table "users" and wait:
    values:
        1:
            name: "Alice"
            age: 25
        2:
            name: "Bob"
            age: 30
```

or the rows come from a variable:

```sk
insert many {_rows::*} into table "archived_users" and wait
```

Rows may name different columns, with two rules:

- A **`values` block** has to name the same columns in every row. One statement binds one column list, so
  a row that omits a column another row names is refused at runtime with
  `Batch row 2 does not contain the same columns as the first row.`
- A **list variable** holding several rows is filled to its common column set instead, and a row that
  omits a column is written with NULL there. That is what lets a select result be inserted straight back.
  MongoDB takes ragged rows either way.

A variable that is not set, or holds nothing, fails the statement with `{_rows::*} is not set.` rather
than writing no rows — and a `select many` that matched nothing leaves its result variable unset, so a
script that feeds one into `insert many` should check it first. See [Reading rows](reading.md).

## In-or-out: upsert and if absent

```sk
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

`upsert` writes the row with that primary-key value, updating it when it is already there. The key goes in
`by id` and **not** in the `values` block: a `values` entry naming the primary key is refused with
`The primary key must not be included in upsert values.`, because the key is what decides whether the row
is inserted or updated. On MySQL this is `INSERT ... ON DUPLICATE KEY UPDATE`.

```sk
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
```

`if absent` inserts only when the database considers the row missing, and does nothing when it is there: an
existing row keeps its old values, where `upsert` would overwrite them. On MySQL the insert is attempted
and a key that is already taken is the one error read as "the row is there"; everything else — a value too
long for its column, a `null` in a `not null` column — fails the statement the way any other write does.
Which one to reach for is a matter of what "already there" should mean:

| Want | Use |
| --- | --- |
| Create it, or overwrite it with these values | `upsert` |
| Create it only if it is missing, leave the old row alone | `insert entity if absent` |
| Know whether it was created | `insert entity if absent ... and store affected rows in {_rows}`: `1` means the row was written and `0` means a key already held it. A read-back works too, but only `if absent` leaves the old row alone to be compared against. |

Both depend on the implementation's conflict rules, as their description says; the behaviour above is
MySQL's.

## Waiting

A write waits for its work: the lines after it run once the change has been taken, and a failure is
readable in `last database error`. The wait parks the trigger, not the server thread, so other players and
other scripts carry on while the statement is in flight.

`and wait` is still accepted on these statements and does nothing. Every statement waits now, writes
included; it used to be how a write asked for exactly this, so the examples here keep it. See
[Errors and waiting](errors-and-waiting.md).

## How many rows were written

Any of these statements can keep the number of rows it affected in a variable, by ending with
`and store affected rows in {_rows}`:

```sk
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

For `insert entity if absent` that count is exactly "was it written": `1` it was, `0` a key already held the
row. It is also how a script writes a condition that only takes effect while a value it read is still
current. For anything else the number is the backend's answer for that statement — an `upsert` on MySQL
counts `1` for an insert and `2` for an update, while PostgreSQL and MongoDB report `1` either way — so
read [Affected rows](affected-rows.md) before branching on it.
