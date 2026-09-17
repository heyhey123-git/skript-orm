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
long it may stay open.

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
though the rollback itself succeeded.

## Waiting inside one

Everything written inside a transaction waits, whether or not it says `and wait`. Two statements running
at once on one connection is not something a script should be able to ask for, and the transaction
cannot commit before the statements it is made of have finished. Leaving `and wait` out inside a
transaction is allowed and does nothing.

## The timeout

A transaction holds one of the connection's pooled connections for its whole life, so one that is never
finished is one the pool never gets back. The default timeout is 30 seconds, and it exists for the case
where the script stops mid-body: an error inside the body ends the trigger without telling the section,
and a `wait` parks it for as long as it likes.

```sk
database transaction with timeout 2 minutes:
    ...
```

When it expires, the transaction is rolled back and the next statement inside it reports why. Do not
write a long `wait` inside a transaction: it holds a connection and any row locks the body has taken
while it waits.

A statement inside a transaction is not given the whole timeout but what is left of it, so the last
statement cannot outlive the transaction by another full timeout. The connection's own
[statement timeout](connections.md) still applies underneath: a statement gets the shorter of the two.

## What it does not do

- **It is not a lock.** A transaction decides whether a group of statements happens at all. Two scripts
  that read a value, change it and write it back can still lose one of the changes; the database's
  isolation level decides what each of them sees.
- **It does not span connections.** `use connection` and `in connection` refuse to switch to another
  connection while a transaction is open, and so does `disconnect`. Two connections need two
  transactions and no promise that both commit.
- **It does not cover everything.** `register a database table` is refused inside one, because creating
  a table commits the transaction on MySQL and its relatives. Statements that are not database
  statements are not undone either: a message that was sent stays sent.
