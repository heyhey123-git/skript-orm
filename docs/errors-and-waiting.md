# Errors and waiting

[简体中文](errors-and-waiting.zh-CN.md) | **English**

During one run of a Skript command or event handler, each database operation finishes before the
next statement runs. Local variables keep their values during the wait. If an operation fails,
the reason is available in `last database error`.

## What waits

| Statement | Waits | Notes |
| --- | --- | --- |
| `create a connection` | always | Neither needs nor accepts `and wait`. |
| `register a database table` | always | Does not need or accept `and wait`. |
| `in connection` | never | Statements in the block wait individually; switching itself performs no database work. |
| `use connection` | never | Only changes the connection used by later statements. |
| `make ... the default` | always | Includes closing the connection that previously held that role. |
| `select one`, `select many`, `select page`, `select ... by id` | always | Continues after the query finishes. |
| `insert`, `insert many`, `insert ... if absent`, `update`, `upsert`, `delete` | always | Subsequent statements run after the write finishes. |
| `disconnect ...` | yes | The script continues after disconnection finishes. |

The operations marked as waiting pause the current script execution until the operation finishes.
Waiting for JDBC does not block the server's main thread, which runs game ticks. Reading values
from a list variable still uses that thread; `insert many` spreads the reading across ticks as
described in [Writing rows](writing.md#how-many-rows-one-write-may-send).

All reads and writes still accept `and wait`, but it no longer changes their behaviour. The clause originally made writes wait; now every read and write waits. The examples on this page retain `and wait` for compatibility with the 1.1 syntax, so existing scripts need no changes.

## last database error

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
if last database error is set:
    send "Insert failed: %last database error%" to console
    stop
send "Stored." to console
```

- The error belongs to the **event** in which the operation ran. Two players running the same command have separate error values. A later, unrelated event cannot read the earlier operation's error through `last database error`.
- Each operation **clears the error before it starts**, avoiding stale errors. **Transactions preserve the first error instead**: subsequent database statements are skipped without clearing it, so the cause of the rollback remains available. See [Transactions](transactions.md).
- The `store affected rows` target is different: **every** statement with that clause clears its target first, including inside transactions, so a previous count cannot be mistaken for a new result. See [Affected rows](affected-rows.md).
- An unset error is displayed as `<none>`. Test it with `is set`, rather than comparing the displayed text.
- Failures set the error. Outside a transaction, success leaves it unset, which you can use as a success check. Inside a transaction, an earlier error may still be present.
- **Failure does not stop the script.** Subsequent statements still run, so the insertion example above checks the error and explicitly calls `stop`. Check `last database error` before any later operation that depends on the write succeeding.
- Skript also reports failures as runtime errors in the console and to players with `skript.see_runtime_errors`. Skript may limit repeated console messages, but the script can still read every failure through `last database error`.

For `insert many` from a list variable, a validation error can arrive after several ticks of reading. No SQL is sent until every row passes validation; the affected-row target stays unset on failure. Do not change the source variable while it is being read. A transaction timeout includes this reading time.

## Writing and then reading

A write finishes before the next statement runs, so a query immediately after it can read the successfully written data:

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"

select many entities from table "users" and store the results in {_users::*}:
    where all:
        name = "Alice"
```

Database operations also finish in order when a read precedes a write or a delete precedes an insert. No extra wait is needed.

## Failures that are not about the database

Some errors occur before a statement is sent: the table is not registered on the current connection, a column is absent from the table definition, Skript cannot convert a value to the column type, or a table name is invalid. The script can read any of these validation errors through `last database error` on its next line. Repeating an unchanged table registration succeeds; a changed declaration may fail if it conflicts with the existing database table. See [Tables](tables.md#registering).

The database also reports failures through `last database error`, such as inserting `null` into a `not null` column or a value that exceeds the column length. The statement waits for the database response before execution continues.
