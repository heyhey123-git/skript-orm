# Transactions

[简体中文](transactions.zh-CN.md) | **English**

A transaction commits a group of database changes together or rolls them all back. Its statements share one connection and can read each other's uncommitted changes. What other scripts can see depends on the backend and its isolation level.

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

Like other statements, this section uses the current connection. Use `database transaction on connection "logs":` to choose a named connection, or `with timeout 5 seconds` to set its maximum duration; see [The timeout](#the-timeout).

## How it ends

| What happens | What the transaction does |
| --- | --- |
| the body reaches its normal end | commits |
| a statement in the body fails | rolls back when the body ends |
| `exit`, `stop` or `return` leaves the body | rolls back |
| `rollback database transaction` | rolls back and leaves the section |
| the timeout expires before it ends | rolls back automatically |
| the connection is closed or the plugin is disabled | rolls back automatically |

There is no explicit `commit` statement. Committing only at the normal end of the body avoids ambiguity about which transaction any remaining statements would belong to.

`rollback database transaction` is available only inside a transaction. Use it to roll back and leave early:

```sk
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
    if {_from::balance} < {_amount}:
        rollback database transaction
```

## A statement that fails

The transaction preserves the first error and becomes rollback-only. Subsequent database statements are skipped rather than sent to the database. This does not stop the entire body: ordinary Skript statements, such as sending messages or changing variables, may still run, and their effects are not rolled back with the database changes.

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

After the section, `last database error` still contains the original error, even if the rollback succeeded. Statements inside the transaction do not clear it. In contrast, each statement still clears its `store affected rows` target, preventing an earlier count from being mistaken for the result of a skipped statement. See [Affected rows](affected-rows.md).

## One connection at a time

All statements in a transaction use the same reserved connection and run sequentially, not concurrently or out of order. The transaction waits for them to finish before committing. Every statement already waits, so no additional setting is needed. `and wait` remains accepted inside a transaction but does not change the behaviour.

## One inside another

A nested `database transaction` **joins the outer transaction**. Only the outermost section commits; an inner section is not a savepoint and cannot be rolled back independently. In particular:

- Specifying **another connection** in the inner section fails with `A database transaction is already open on another connection.` This makes the outer transaction rollback-only, so all its changes will be undone.
- `rollback database transaction` inside the inner section rolls back the **whole transaction**, not just the inner body. Subsequent database statements in the outer body report that the transaction is no longer running.

Functions called inside a transaction are part of it. Their statements use the transaction's connection, and a `database transaction` inside the function joins the caller's transaction rather than opening another one.

## The timeout

A transaction reserves one pooled connection for its entire lifetime. If it never ends, that connection cannot return to the pool. The default timeout of 30 seconds guards against scripts stopping mid-transaction: an error may end the Skript trigger without notifying the section, and a `wait` may pause it for a long time.

Set a different duration when opening the transaction:

```sk
database transaction with timeout 2 minutes:
    ...
```

- Use a positive Skript timespan, such as `2 minutes` or `500 milliseconds`.
- `with a timeout of 2 minutes` is the same clause; `a` and `of` are optional.
- The timeout clause must follow `on connection`: `database transaction on connection "logs" with timeout 2 minutes:`.
- Timing starts when the transaction opens and reserves its connection, not at the first statement. The timer **does not pause**: database statements, script work between them and `wait` all count. A long body may time out even if each statement is quick. The limit bounds how long the connection and row locks are held, not just individual statement execution.
- A nonpositive value reports `The transaction timeout has to be positive.` An expression with no value reports `The transaction timeout is not set.` Neither opens a transaction.
- The timeout is set per transaction. Connections can define a `statement timeout`, but there is no connection-wide transaction timeout. Specify a longer duration where you open the transaction that needs it.

When the timeout expires, the transaction rolls back, and subsequent statements inside it report the reason. Avoid long waits inside transactions: the connection and any acquired row locks remain occupied during the wait.

Statements within a transaction use the **remaining transaction time** as their driver execution limit, rounded up to whole seconds with a minimum of one second. Each statement does not receive a fresh, full timeout. At the deadline, cancellation is attempted so the statement does not continue holding the connection. The connection's `statement timeout` does not apply inside a transaction; the remaining transaction time is used instead.

Rollback needs the same connection, so it cannot run immediately while a statement is still executing. It must first wait for that statement to finish. If the connection stops responding entirely, rollback and connection release may occur after the deadline; the deadline itself does not change. The `socketTimeout` in the [url](connections.md#statement-timeout) bounds that wait.

Set a longer timeout when the transaction genuinely needs it. If it is slow because it includes too much work, consider splitting it up: move slow reads and lengthy calculations outside when they do not need transaction protection, and keep only the statements that must succeed together inside.

## What it does not do

- **It is not a lock.** A transaction commits or rolls back a group of changes together, but two scripts reading, modifying and writing the same value can still lose an update. The database's isolation level determines what each can see.
- **It does not span connections.** While a transaction is open, `use connection` and `in connection` cannot switch to another connection; `disconnect` and `make ... the default` are also restricted accordingly. Two connections require two transactions, with no guarantee that both commit together.
- **It does not cover everything.** `register a database table` is prohibited inside a transaction because creating a table commits transactions on databases such as MySQL. Non-database operations are not rolled back either: a message already sent cannot be recalled.
