# Errors and waiting

[简体中文](errors-and-waiting.zh-CN.md) | **English**

Every statement that touches the database waits for its work. The lines after it run once the work is
done, local variables keep their values across it, and a failure is waiting in `last database error`.
That is what makes the order of a script the order of its statements, and it is why there is no such
thing here as a write that silently happens later.

## What waits

| Section | Waits | Notes |
| --- | --- | --- |
| `create a connection` | always | No `and wait`, and none is accepted. |
| `register a database table` | always | Same. |
| `in connection` | never | The block's own statements decide; the switch itself does no database work. |
| `use connection`, `make ... the default` | never | They only change which connection later statements use. |
| `select one`, `select many`, `select page`, `select ... by id` | always | A read has nothing to do until it has the rows. |
| `insert`, `insert many`, `insert ... if absent`, `update`, `upsert`, `delete` | always | The lines after a write run once the change has been taken. |
| `disconnect ...` | following line waits | Asynchronous, but the trigger continues after it finishes. No form reports success. |

A statement that waits behaves like any other delayed part of a trigger: the lines after it run later.
The **server thread is not held while it waits**, so other players and other scripts carry on; only this
trigger is parked.

`and wait` is still accepted on every read and every write, and does nothing. It used to be how a write
asked for the behaviour every statement has now, so the examples on these pages keep it: a script written
against 1.1 keeps working, and the wait it asks for is already there.

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

- It belongs to the **event** the operation ran in. Two players running the same command each have their
  own, and a value read in a later, unrelated event tells you nothing.
- Each operation **clears it before it runs**, so what you read is about the operation you just ran, not
  an older one. **Inside a transaction it is deliberately not cleared**: the statement that failed leaves
  its cause there, the statements after it are skipped without reporting anything of their own, and
  clearing it would leave the script reading nothing at all. See [Transactions](transactions.md).
- The `store affected rows` variable is the other way round: it is cleared for **every** statement that
  names it, inside a transaction too, because a number must never outlive the statement that produced it.
  See [Affected rows](affected-rows.md).
- An unset error is printed as `<none>`, so compare with `is set` rather than against text.
- It is set by a failure and left unset by a success: a statement that worked has nothing to say, so the
  lines after it can treat an unset error as success.
- A failure is also printed to the console, with the script line it came from. The slot is what a script
  reads; the console line is what an admin reads.

## Writing and then reading

The write has finished by the time the next line runs, so a read after it sees it:

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"

select many entities from table "users" and store the results in {_users::*}:
    where all:
        name = "Alice"
```

The same holds for a read followed by a write that depends on it, and for a delete before a re-insert.
Nothing has to be written to get this order: it is what a statement does.

## Failures that are not about the database

Some failures happen before anything is sent, and they are reported the same way: a table that was never
registered on this connection, a column the table description does not have, a value Skript cannot
convert to the column's type, or a second registration of the same table. All of them appear in
`last database error` right after the statement. What the database itself refuses, such as a `null` where
the column says `not null` or a value that is too long for the column, arrives the same way: the statement
waits for the answer before the lines after it run.
