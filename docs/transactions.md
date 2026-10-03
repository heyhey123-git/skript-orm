# Transactions

[简体中文](transactions.zh-CN.md) | **English**

A transaction commits a group of database changes together or rolls them all back. Statements
in the transaction share one connection and can read each other's changes before commit.
Whether other scripts can see those changes before commit depends on the database's
isolation setting.

```sk
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}

    update one entity in table "accounts" by id {_to} and wait:
        values:
            balance: {_to::balance} + {_amount}

if last database error is set:
    send "The transfer was rolled back: %last database error%"
```

The `database transaction:` section uses the current connection by default. To choose a named connection, write `database transaction on connection "logs":`. To set a time limit, add `with timeout 5 seconds`; see [Timeouts](#timeouts).

## How it ends

| What happens | What the transaction does |
| --- | --- |
| the body reaches its normal end | commits |
| a statement in the body fails | rolls back when the body ends |
| `exit`, `stop` or `return` leaves the body | rolls back |
| `rollback database transaction` | rolls back and leaves the section |
| the timeout expires before it ends | rolls back automatically |
| the connection is closed or the plugin is disabled | rolls back automatically |

There is no explicit `commit` statement. The transaction commits when the section completes normally.

`rollback database transaction` is available only inside a transaction. Use it to roll back and leave early:

```sk
database transaction:
    if {_from::balance} < {_amount}:
        rollback database transaction
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
```

## A statement that fails

The transaction preserves the first error and can no longer commit: it will roll back when
the `database transaction:` block ends. Subsequent database statements are skipped rather
than sent to the database. A failed database statement does not stop the block: ordinary
Skript statements, such as sending messages or changing variables, may still run, and
their effects are not rolled back with the database changes.

```sk
database transaction:
    insert one entity into table "orders" and wait:
        values:
            item: "sword"
    insert one entity into table "orders" and wait:     # fails: no such column
        values:
            itemm: "sword"
    insert one entity into table "audit" and wait:      # skipped; no write is performed
        values:
            what: "order stored"
```

After the `database transaction:` section ends, `last database error` still contains the first failure, even if the rollback succeeded. Later database statements inside that transaction do not clear the error. Each statement does clear its `store affected rows` target, so a skipped statement cannot leave behind an earlier affected-row count. See [Affected rows](affected-rows.md).

## Execution order

All database statements in a transaction use the connection reserved by the outermost `database transaction:` section and run in order. The transaction waits for each statement before committing. Adding `and wait` to a statement does not change that order.

## Nested transactions

A nested `database transaction` **joins the outer transaction**. Only the outermost block
commits. The nested block is not a savepoint: it cannot roll back only its own changes.
In particular:

- Specifying **another connection** in the nested `database transaction` section fails with `A database transaction is already open on another connection.` The error makes the outer transaction rollback-only, so all its changes will be undone.
- `rollback database transaction` inside the inner section rolls back the **whole transaction**, not just the inner body. Subsequent database statements in the outer body report that the transaction is no longer running.

Database statements in functions called from a transaction use the caller's transaction connection. A `database transaction` inside such a function joins the caller's transaction rather than opening another one.

## Timeouts

A transaction reserves one connection from the pool (the reusable connections kept ready
by the addon) until it ends. The default timeout is 30 seconds.

Set a different duration when opening the transaction:

```sk
database transaction with timeout 2 minutes:
    ...
```

- Use a positive Skript timespan, such as `2 minutes` or `500 milliseconds`.
- `with a timeout of 2 minutes` is an alternative spelling of `with timeout 2 minutes`; the words `a` and `of` are optional.
- The timeout clause must follow `on connection`: `database transaction on connection "logs" with timeout 2 minutes:`.
- Timing starts when the transaction reserves its connection. Database statements, script work, and `wait` all count toward the limit, even if each database statement is quick. The limit also bounds how long the transaction holds its connection and any row locks.
- A nonpositive value reports `The transaction timeout has to be positive.` An expression with no value reports `The transaction timeout is not set.` Neither opens a transaction.
- Set the timeout on each `database transaction` section. A connection can define `statement timeout`, but cannot define a transaction-wide default. Specify a longer duration on the transaction that needs it.

When a transaction times out, it rolls back, and later database statements in its body report the timeout. Avoid long waits inside transactions: the reserved connection and any acquired row locks remain occupied during the wait.

Statements within a transaction use the **remaining transaction time** as their driver execution limit, rounded up to whole seconds with a minimum of one second. Each statement does not receive a fresh, full timeout. At the deadline, cancellation is attempted so the statement does not continue holding the connection. The connection's `statement timeout` does not apply inside a transaction; the remaining transaction time is used instead.

Rollback must use the transaction's reserved connection. If a database statement is still executing on that connection, rollback waits for the statement to finish. If the connection stops responding entirely, rollback and connection release may occur after the deadline; the deadline itself does not change. The `socketTimeout` in the [URL](connections.md#statement-timeout) bounds the wait for the connection.

If a statement may still be running after rollback, or rollback itself fails, the plugin
closes the connection instead of returning it to the pool. This prevents unfinished work
from being committed when the connection returns to its usual mode of committing each
statement separately. A rollback failure is attached to the reported error; after a
timeout, `last database error` still reports the timeout.

If the transaction needs more time, set a longer timeout. Move slow reads and calculations outside the section when they do not need to be part of the transaction.

## What it does not do

- **It is not a lock.** A transaction commits or rolls back a group of changes together, but two scripts reading, modifying and writing the same value can still lose an update. The database's isolation level determines what each can see.
- **It does not span connections.** While a transaction is open, `use connection` and `in connection` cannot switch to another connection. `disconnect` cannot close the transaction's connection, and `make ... the default` is refused inside a transaction even when it names that same connection. Two connections require two transactions, with no guarantee that both commit together.
- **It does not cover everything.** `register a database table` is prohibited inside a transaction because creating a table commits transactions on databases such as MySQL. Non-database operations are not rolled back either: a message already sent cannot be recalled.
