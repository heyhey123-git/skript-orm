# Writing rows

[简体中文](writing.zh-CN.md) | **English**

Use `insert one` or `insert many` to add rows. Use `insert entity if absent` to preserve an existing row, `upsert one entity` to insert or update by primary key, and `update` to change existing rows. These statements use the same format for column values.

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

The last statement has no indented body, so it needs no colon. Adding one produces an empty-section warning. Use a colon only when the statement has a body; the same rule applies to [reads](reading.md) and [updates and deletes](updating-and-deleting.md).

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

One write statement binds at most **30,000 values**. For a six-column table, that is 5000 rows. A larger batch is split into multiple statements; no rows are silently dropped. Wider rows mean fewer rows per statement. Even a row wider than the limit is sent on its own.

`and store affected rows in {_rows}` reports the total across the split statements, when the backend can provide an exact count. If a later statement fails, earlier statements remain applied unless the batch runs inside a `database transaction` block.

When `insert many` reads a list variable, it checks and converts the rows on the server thread in slices of at most 4096 steps, aiming for about 2 ms per slice. If more work remains, it continues on a later tick. It validates **every row before sending any SQL**, so an unknown column or invalid value near the end of the variable cannot leave a partial write. The trigger resumes only after the database write finishes, and local variables remain available afterwards. A `values:` block does not use this sliced reader.

Keep the source variable unchanged until the write finishes. The reader rejects changes it detects, but cannot catch every edit. In particular, it may miss changes to rows already read or values replaced at existing keys. In a transaction, the time spent reading across ticks counts toward the transaction timeout. Slicing limits the work attempted in one tick; it does not guarantee a fixed tick duration, particularly when converting an expensive value. For large jobs, smaller batches such as one `select page` result at a time also limit memory use. See [Reading rows](reading.md#how-many-rows-one-read-may-store).

MySQL uses parameterized multi-row inserts, splitting them by both the parameter limit and the server's `max_allowed_packet`. Values whose serialized size cannot be estimated safely, such as NBT and item stacks, are sent one row at a time. A single row must still fit within the server's packet limit. Other JDBC backends keep their existing batch insert paths.


## Upsert or insert if absent

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
