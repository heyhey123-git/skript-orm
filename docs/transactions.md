# Transactions

[简体中文](transactions.zh-CN.md) | **English**

A transaction makes a group of statements all-or-nothing. It runs on one connection, so the statements
inside it see each other's unfinished work, and nothing another script reads sees any of it until it
commits.

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

The section uses the connection in effect, the same way every other statement does. `database
transaction on connection "logs":` pins a named one instead, and `with timeout 5 seconds` changes how
long it may stay open — see [The timeout](#the-timeout).

## How it ends

| What happens | What the transaction does |
| --- | --- |
| the body reaches its end | commits |
| a statement in the body fails | rolls back, once the body ends |
| `exit`, `stop` or `return` leaves the body | rolls back |
| `rollback database transaction` | rolls back, and leaves the section |
| it stays open past its timeout | rolls back on its own |
| the connection is closed, or the plugin is disabled | rolls back on its own |

There is no `commit` to write. A commit that carried on with the body would leave the statements after
it in a transaction that no longer exists, and every answer to "which transaction are they in now" is a
surprise.

`rollback database transaction` can only be written inside a transaction, and it is the way out of the
middle of one:

```sk
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
    if {_from::balance} < {_amount}:
        rollback database transaction
```

## A statement that fails

The first failure is kept, and the transaction becomes one that can only be rolled back. The statements
after it do nothing: sending them to the database would be writing work that is about to be thrown away.

```sk
database transaction:
    insert one entity into table "orders" and wait:
        values:
            item: "sword"
    insert one entity into table "orders" and wait:     # fails: no such column
        values:
            itemm: "sword"
    insert one entity into table "audit" and wait:      # does nothing
        values:
            what: "order stored"
```

`last database error` holds the first failure, so the line after the section says what went wrong even
though the rollback itself succeeded. That slot is the one thing a transaction keeps across statements:
a statement inside one does not clear it, because the cause of the rollback is worth more than the
silence of the statements that were skipped. A `store affected rows` variable is not kept that way — it
is cleared per statement, in a transaction too, so that no number is left over from a statement that
never answered. See [Affected rows](affected-rows.md).

## One connection at a time

Every statement inside runs on the connection the transaction pinned, so two of them cannot be in flight
at once and cannot overtake each other: a second statement would be writing into a transaction that is
still being built, and the transaction cannot commit before the statements it is made of have finished.
Nothing has to be written for that, because every statement waits anyway, and `and wait` inside a
transaction is accepted and does nothing.

## One inside another

A `database transaction` written inside another one joins it: only the outermost section commits, so an
inner one is not a savepoint and cannot be undone on its own. Two consequences are worth knowing:

- Naming **another connection** in the inner section is refused with
  `A database transaction is already open on another connection.` That refusal is a statement of the outer
  body which did not run, so the outer transaction becomes rollback-only and everything the body did is
  undone.
- `rollback database transaction` in the inner section rolls back the **whole** transaction rather than the
  inner body, and the statements after it in the outer body report that the transaction is no longer
  running.

A function called from inside a transaction is part of it: its statements run on the transaction's
connection, and a `database transaction` written in the function joins the caller's rather than opening a
second one.

## The timeout

A transaction holds one of the connection's pooled connections for its whole life, so one that is never
finished is one the pool never gets back. The default timeout is 30 seconds, and it exists for the case
where the script stops mid-body: an error inside the body ends the trigger without telling the section,
and a `wait` parks it for as long as it likes.

Write the timeout on the section that wants another one:

```sk
database transaction with timeout 2 minutes:
    ...
```

- It takes a Skript timespan (`2 minutes`, `500 milliseconds`), and it has to be positive.
- `with a timeout of 2 minutes` is the same clause: the `a` and the `of` are optional.
- It comes after `on connection`, in that order:
  `database transaction on connection "logs" with timeout 2 minutes:`.
- The clock starts when the transaction opens, because opening it is what takes the connection — not at
  its first statement — and it does not pause: statements, the script's own work between them and a
  `wait` all count towards it. A large or slow body is rolled back even when every statement in it is
  quick, because what the deadline bounds is how long a connection and its locks are held, not how fast
  any one statement is.
- A value that is not positive, or an expression that resolved to nothing, is reported as
  `The transaction timeout has to be positive.` and `The transaction timeout is not set.`, and no
  transaction is opened.
- It is written per transaction. A connection sets a timeout for its statements
  (`statement timeout`), but there is no connection-wide transaction timeout: a script that wants a
  longer one says so where it opens the transaction.

When it expires, the transaction is rolled back and the next statement inside it reports why. Do not
write a long `wait` inside a transaction: it holds a connection and any row locks the body has taken
while it waits.

A statement inside a transaction is not given the whole timeout but what is left of it, rounded up to a
second and never below one, as the limit the driver enforces. The last statement of a transaction
therefore cannot outlive it by another full timeout, and a statement that runs into the deadline is
cancelled rather than left holding the connection. The connection's `statement timeout` does not apply
inside a transaction: the remaining transaction time is the only limit there is.

Nothing can interrupt a statement that is already running, because the rollback needs the same connection
and waits for it to come back. That is why the statement is the one that gives up first. A connection
that stopped answering altogether is the case where the deadline can slip, and the `socketTimeout` in the
[url](connections.md#statement-timeout) is what bounds it.

A transaction that legitimately needs longer says so, and one that is slow because it holds a lot of work
is better off split: what is inside it is what the transaction protects, so slow reads and long
computation can move out of the body while the statements that have to happen together stay in it.

## What it does not do

- **It is not a lock.** A transaction decides whether a group of statements happens at all. Two scripts
  that read a value, change it and write it back can still lose one of the changes; the database's
  isolation level decides what each of them sees.
- **It does not span connections.** `use connection` and `in connection` refuse to switch to another
  connection while a transaction is open, and so do `disconnect` and `make ... the default`. Two
  connections need two transactions and no promise that both commit.
- **It does not cover everything.** `register a database table` is refused inside one, because creating
  a table commits the transaction on MySQL and its relatives. Statements that are not database
  statements are not undone either: a message that was sent stays sent.
