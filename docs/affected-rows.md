# Affected rows

[简体中文](affected-rows.zh-CN.md) | **English**

Writes can report an affected-row count for scripts to act on. For example, you can use it to check whether a conditional update succeeded without a transaction:

```sk
update entities in table "accounts" with limit 1 and store affected rows in {_rows}:
    values:
        balance: {_balance} - {_amount}
    where all:
        id = {_from}
        balance = {_balance}

if {_rows} is 0:
    send "The balance changed while it was being read." to console
```

## The clause

The optional `store affected rows in {_rows}` clause is available on every write: `insert one`, `insert many`, `insert ... if absent`, `update`, `upsert` and `delete`, with or without a section body. Place it after the statement's arguments, joined with `and`. That conjunction tells Skript where the preceding argument ends. You can still append `and wait`, but it does not change the waiting behaviour.

```sk
delete entities from table "logs" with limit 500 and store affected rows in {_deleted}
insert one {_user::*} into table "archived_users" and store affected rows in {_rows}
upsert one entity in table "users" by id {_id} and store affected rows in {_rows}:
    values:
        name: "Alice"
```

The target must be a single variable such as `{_rows}`, not `{_rows::*}`. A list does not specify which key should hold the count, and other expressions cannot be assigned a value. Both are rejected when the script is parsed.

## What the number means

The backend supplies the count: inserts count added rows, deletes count removed rows, and updates follow backend-specific rules. If an update matches a row without changing its values, PostgreSQL and MongoDB count one row, while MySQL counts zero. To use the count as a conditional-update check, ensure the write would change a value, as the example above does when the amount is nonzero.

| Statement | MySQL | PostgreSQL | MongoDB |
| --- | --- | --- | --- |
| `insert one`, `insert many` | rows written | rows written | rows written |
| `update` | rows **changed** | rows matched | rows matched |
| `delete` | rows removed | rows removed | rows removed |
| `upsert` | `1` inserted, `2` updated, `0` if values were unchanged | `1` either way | `1` inserted, rows matched when updated |
| `insert ... if absent` | `1` inserted, `0` if the key already exists | same | same |
| a batch the driver cannot count | no number | same | never happens |

Use `insert ... if absent` to distinguish a new insert from an existing row consistently across all three backends. `upsert` cannot provide that distinction everywhere: MySQL reports `2` for an update, but the other two backends do not distinguish inserts from updates.

Some backends cannot provide per-row counts for a batch. In that case, the result is **no number**, rather than an inaccurate count.

## Set, unset and zero

The variable is **cleared when the statement starts**, before any validation that might reject it. A count is stored only after the statement finishes and an exact count is available:

| The variable holds | It means |
| --- | --- |
| a number | the statement ran and reported a count using the backend's rules |
| nothing | the statement was rejected, skipped or failed, or the backend could not count |

**Zero is a valid result**, not a missing one. Its meaning depends on the statement and backend: no matching rows, an existing key, or unchanged values in a MySQL update, for example. Use `is set` to distinguish zero from an unavailable count:

```sk
if {_rows} is not set:
    send "Nothing reported how many rows were written." to console
else if {_rows} is 0:
    send "No row matched." to console
```

The zero-count message above applies only when zero means no match; it is not suitable for every write.

Clearing the variable prevents a previous count from being mistaken for the current result. Writes wait for completion, so the next statement can read the count without any extra waiting. See [Errors and waiting](errors-and-waiting.md).

This also applies inside a [transaction](transactions.md): every statement with this clause clears its target, including skipped statements. In contrast, `last database error` preserves the first error so the cause of a rollback remains available.

Ending a transaction does not clear this variable, and rolling back does not undo the variable assignment. A `{_rows}` value of `1` after the section may therefore describe a database change that was rolled back. Check `last database error` before relying on a count from a transaction.

## A safe conditional write

Reading a value and then writing a new value takes two statements, with room for another write between them. Without a transaction, include the previously read value in the update condition:

```sk
select one entity from table "accounts" and store the result in {_account::*}:
    where all:
        id = {_from}
set {_balance} to {_account::balance}

loop 3 times:
    update entities in table "accounts" with limit 1 and store affected rows in {_rows}:
        values:
            balance: {_balance} - {_amount}
        where all:
            id = {_from}
            balance = {_balance}

    if {_rows} is 1:
        stop
    # Read the balance again before retrying; production code should also check database errors.
    select one entity from table "accounts" and store the result in {_account::*}:
        where all:
            id = {_from}
    set {_balance} to {_account::balance}
```

The `where` block includes the balance previously read, so the update matches only while that balance is unchanged. If the amount is nonzero and the statement succeeds, one affected row means the update took effect. Zero means the conditions did not match, perhaps because another write changed the balance or deleted the row.

Keep two details in mind:

- MySQL counts only rows whose values actually change, so an amount of `0` can also produce zero affected rows. Reject zero amounts before the loop rather than retrying them.
- Bound the number of retries. A row that is continually updated can otherwise keep the loop running indefinitely. When several rows must change together, use a [transaction](transactions.md).
