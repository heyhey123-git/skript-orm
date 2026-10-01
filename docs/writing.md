# Writing rows

[简体中文](writing.zh-CN.md) | **English**

Five statements write rows: `insert one`, `insert many`, `insert entity if absent`, `upsert one entity`, and `update` for existing rows. They all use the same format for row values.

## The values block

Write values as `column: expression`, either directly in the section body or inside a `values:` block:

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 1 + 24
        joined: now
```

- The right-hand side accepts any Skript expression, including variables, arguments and functions.
- Each `column: expression` must fit on one line. Nested blocks are allowed only where multiple rows are expected (see `insert many`).
- **Omitted columns are left out of the statement.** An `insert` uses the database default, allowing auto-increment keys to be generated. An `update`, or an `upsert by id` that updates an existing row, preserves the stored value. To store SQL NULL explicitly, write `null`; see [Types](types.md).
- **A named column whose expression resolves to nothing is written as SQL NULL.** If `{_nick}` is unset, `name: {_nick}` writes NULL (or fails on a `not null` column). It is not the same as omitting `name`: only omitting the line preserves the stored value during an update.
- **A list variable cannot store SQL NULL.** Skript deletes keys set to null, and query results leave NULL columns unset. When you copy a result row and write it back from a variable, those columns are omitted: an `insert` uses database defaults and fails on a `not null` column without a default, while an `update` leaves the columns unchanged. To write NULL explicitly, use a `values` block with a literal `null`.
- Unknown column names fail before the statement is sent to the database.

## Insert one

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "Insert failed: %last database error%" to console
```

The same section can also read a row from a variable with the structure of a query result:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users"
```

The variable must contain exactly one row. Multiple rows are rejected; use `insert many` for those.

The last statement has no body, so it has no colon. Skript parses this form as an effect; adding a colon without a body produces an empty-section warning. Both forms perform the same operation: use a colon when there is a body, and omit it otherwise. The examples in [reading](reading.md) and [updating and deleting](updating-and-deleting.md) follow the same rule.

## Insert many

Each nested block under `values:` represents one row:

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

You can also supply rows from a variable:

```sk
insert many {_rows::*} into table "archived_users" and wait
```

The column sets follow these rules:

- In a **`values` block**, every row must name the same columns. One statement binds one column list. A row missing a column included in another row is rejected at runtime with `Batch row 2 does not contain the same columns as the first row.`
- A **list variable** containing multiple rows is expanded to the combined set of columns, with NULL written for missing columns. This lets you insert query results directly. MongoDB accepts rows with different column sets in either form.

An unset or empty variable fails with `{_rows::*} is not set.` rather than successfully inserting zero rows. Since `select many` leaves its result unset when nothing matches, check the variable before passing it to `insert many`. See [Reading rows](reading.md).

### How many rows one write may send

One write statement binds at most **30 000 values**, which is the batch a `select` is also held to: 5000 rows of a six-column table. A larger batch is not refused and nothing is dropped: it is sent as several statements, and every row is written. A wider table carries fewer rows per statement, a narrower one more, and a table wider than the budget itself still moves, one row per statement.

Crossing the budget is not a performance boundary, and the measurement is what says so. On the benchmark server (the generic JDBC connection, SQLite there) a batch of 30 000 values was written in one call with the count it reported matching what the table gained, and so was a batch of 60 000 values — 10 000 rows of a six-column table — with no step between the two in the curve: taken against the tick, the first run of each overran it by 70 to 90 milliseconds for 5000 rows and 100 to 110 for 10 000, which is 2.3 to 3.0 microseconds per value and 1.7 to 1.8. Repeating the 30 000-value write with the JVM warm is a range rather than a figure: one run overran by about 30 milliseconds, and another kept the whole write inside a tick and so reported no overhang at all — a cost under a tick's 50 milliseconds has nothing to lengthen, which is the floor [What storing a result costs](reading.md#what-storing-a-result-costs) works out. The per-value reading follows from that range and is not a stable constant. An oversized batch pays for becoming two statements; the work either side of that is the same. The budget is therefore a line about **splitting and protection** — how many values one statement binds, and how much of the server thread one of them may hold — rather than the size past which writing gets expensive.

The count in `and store affected rows in {_rows}` is the total across those statements, so a batch of 8000 rows reports 8000.

The boundary is a value count rather than a row count because that is what both halves of a write are measured in. A driver binds a fixed number of values to a statement and refuses a statement that asks for more, and turning a batch into rows costs the server thread in proportion to its values, not to its rows. This is the same budget [a read is held to](reading.md#how-many-rows-one-read-may-store), and a read states it in rows because a row is what an author counts and what `select page` has to be able to refuse before anything is sent.

Reading the batch out of a variable still costs the server thread in proportion to its size, and that part cannot move off the thread: a variable name may contain expressions, which Skript only resolves there. Measured, that is about 0.33 microseconds per value, so a batch of 30 000 values is about 10 milliseconds of pause before the first statement is sent — and a batch past the budget pauses for roughly that much per statement it becomes, because the budget bounds one statement rather than one trigger. A very large batch is therefore a hitch, and splitting it yourself over several statements, one page of a `select page` walk at a time, is still the way to keep one write short.

One consequence is worth knowing when a batch is split: a failure part way through leaves the statements before it applied unless you are inside a `begin transaction` block, exactly as any other sequence of statements would.


## In-or-out: upsert and if absent

```sk
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

`upsert` writes the row with the given primary key, updating it if it already exists. Put the key in `by id`, **not** in the `values` block. Including it there fails with `The primary key must not be included in upsert values.` The key determines whether to insert or update. MySQL implements this as `INSERT ... ON DUPLICATE KEY UPDATE`.

```sk
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
```

`if absent` inserts only when the database considers the row missing. Otherwise, it preserves the existing row rather than overwriting it as `upsert` would. On MySQL, it attempts a normal insert and treats only a duplicate-key error as “already exists”. Other errors, such as an oversized value or `null` in a `not null` column, still fail the statement.

| Want | Use |
| --- | --- |
| Create the row, or update it with these values | `upsert` |
| Create the row only if missing; preserve an existing row | `insert entity if absent` |
| Check whether a row was created | `insert entity if absent ... and store affected rows in {_rows}`: `1` means inserted, `0` means the key already exists. You can also read the data back for comparison, but only `if absent` preserves the existing row for that comparison. |

Both follow the implementation's conflict rules, as noted in their syntax descriptions. The behaviour above is specific to MySQL, and applies to `"MariaDB"` unchanged, since both use the same dialect.

## Waiting

Writes finish before the following statements run. Failures are available in `last database error`. Waiting pauses only the current trigger, not the server thread, so other players and scripts continue normally.

These statements still accept `and wait`, but it no longer changes their behaviour: every statement now waits, including writes. The examples retain the clause for compatibility with the older syntax. See [Errors and waiting](errors-and-waiting.md).

## How many rows were written

All these statements accept `and store affected rows in {_rows}` to save the affected-row count:

```sk
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

For `insert entity if absent`, `1` means inserted and `0` means the key already exists. Affected-row counts also let you check conditional updates that should succeed only while a previously read value remains unchanged. Other counts depend on the backend: for example, a MySQL `upsert` reports `1` for an insert and `2` for an update, while PostgreSQL and MongoDB report `1` for either. Read [Affected rows](affected-rows.md) before using the count to choose what happens next.
