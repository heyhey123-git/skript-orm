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

Rows may name different columns. A row that omits a column is written with that column left out of the
statement where the database allows it, and as NULL where a single statement has to bind one column
list for every row.

## In-or-out: upsert and if absent

```sk
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

`upsert` writes the row with that primary-key value, updating it when it is already there. On MySQL this
is `INSERT ... ON DUPLICATE KEY UPDATE`.

```sk
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
```

`if absent` inserts only when the database considers the row missing, and does nothing when it is there.
On MySQL this is `INSERT IGNORE`, so a duplicate key is silently skipped rather than turned into an
update: an existing row keeps its old values. Which one to reach for is a matter of what "already
there" should mean:

| Want | Use |
| --- | --- |
| Create it, or overwrite it with these values | `upsert` |
| Create it only if it is missing, leave the old row alone | `insert entity if absent` |
| Know whether it was created | Only `if absent` can show it: a skipped insert leaves the old row's values, so a read-back that differs from what you wrote means the row was already there. `upsert` writes your values either way. |

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

That is how a script tells "it was already there" from "it was written", and how it writes a condition
that only takes effect while a value it read is still current. See [Affected rows](affected-rows.md).
