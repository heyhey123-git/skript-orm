# Errors and waiting

[简体中文](errors-and-waiting.zh-CN.md) | **English**

Every database statement waits for its operation to finish before subsequent statements run. Local variables retain their values during the wait, and failures are recorded in `last database error`. Database operations within a single trigger execution therefore run in the order they are written; writes do not finish silently after later statements have already run.

## What waits

| Section | Waits | Notes |
| --- | --- | --- |
| `create a connection` | always | Neither needs nor accepts `and wait`. |
| `register a database table` | always | Same. |
| `in connection` | never | Statements in the block wait individually; switching itself performs no database work. |
| `use connection` | never | Only changes the connection used by later statements. |
| `make ... the default` | always | Includes closing the connection that previously held that role. |
| `select one`, `select many`, `select page`, `select ... by id` | always | Continues after the query finishes. |
| `insert`, `insert many`, `insert ... if absent`, `update`, `upsert`, `delete` | always | Subsequent statements run after the write finishes. |
| `disconnect ...` | following line waits | Runs asynchronously; the trigger resumes after completion. No form reports success. |

Like other delayed Skript operations, these statements pause the current trigger and resume it later. **Waiting does not block the server thread**, so other players and scripts continue normally.

All reads and writes still accept `and wait`, but it no longer changes their behaviour. The clause originally made writes wait; now every statement waits. These examples retain it for compatibility with the 1.1 syntax, so existing scripts need no changes.

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

- The error belongs to the **event** in which the operation ran. Two players running the same command have separate error values. A later, unrelated event cannot use it to inspect this operation.
- Each operation **clears the error before it starts**, avoiding stale errors. **Transactions preserve the first error instead**: subsequent database statements are skipped without clearing it, so the cause of the rollback remains available. See [Transactions](transactions.md).
- The `store affected rows` target is different: **every** statement with that clause clears its target first, including inside transactions, so a previous count cannot be mistaken for a new result. See [Affected rows](affected-rows.md).
- An unset error is displayed as `<none>`. Test it with `is set`, rather than comparing the displayed text.
- Failures set the error. Outside a transaction, success leaves it unset, which you can use as a success check. Inside a transaction, an earlier error may still be present.
- **Failure does not stop the trigger.** Subsequent statements still run, so the examples check the error and explicitly `stop`. Do the same when later operations depend on the write succeeding.
- Failures are also reported as a Skript runtime error, through Skript's own channel: the console names the script, the syntax, the line number and the line itself, and players with `skript.see_runtime_errors` are told about it. When one line keeps failing, Skript applies its frame limits to those console lines (see `runtime errors.*` in its configuration); what a script reads is always in `last database error`, unaffected.

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

The same ordering applies to reading before writing and deleting before inserting. No extra wait is needed.

## Failures that are not about the database

Some errors occur before a statement is sent: the table is not registered on the current connection, a column is absent from the table definition, Skript cannot convert a value to the column type, or the same table is registered twice. These errors are also available through `last database error` in the next statement.

Database errors use the same reporting mechanism, including `null` in a `not null` column or a value exceeding the column length. The statement waits for the database response before execution continues.
