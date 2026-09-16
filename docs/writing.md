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
- **A column that is left out is not part of the statement.** The database default applies, which is how
  an auto-increment key stays automatic. Writing `null` instead stores SQL NULL; see [Types](types.md).
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
| Know whether it was created | `upsert` or `if absent`, then read the row back and compare |

Both depend on the implementation's conflict rules, as their description says; the behaviour above is
MySQL's.

## Waiting

`insert`, `update`, `upsert` and `if absent` take `and wait`:

- **With it**, the rest of the trigger runs after the write has finished, and a failure is readable in
  `last database error`.
- **Without it**, the write goes to the background and the next line runs immediately. The synchronous
  checks still report through `last database error` (no connection, unknown table, a value that does not
  fit), but a failure from the database itself is only logged.

Reads always wait, so a script that writes and then reads in the same trigger should write with
`and wait`; otherwise the read may see the row as it was before. See
[Errors and waiting](errors-and-waiting.md).
