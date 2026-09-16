# Errors and waiting

[简体中文](errors-and-waiting.zh-CN.md) | **English**

Two things decide what a script can see about a database operation: whether the operation waits, and
`last database error`.

## What waits

| Section | Waits | Notes |
| --- | --- | --- |
| `create a connection` | always | No `and wait`, and none is accepted. |
| `register a database table` | always | Same. |
| `select one`, `select many`, `select page`, `select ... by id` | always | A read has nothing to do until it has the rows. |
| `insert`, `insert many`, `insert ... if absent`, `update`, `upsert`, `delete` | with `and wait` | Without it the work goes to the background. |
| `disconnect from the current database` | following line waits | Asynchronous, but the trigger continues after it finishes. |

A section that waits behaves like any other delayed part of a trigger: the lines after it run later, and
local variables keep their values across it.

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
  an older one.
- An unset error is printed as `<none>`, so compare with `is set` rather than against text.
- It is set by a failure and left unset by a success. See the next section, though: "unset" is weaker
  than it sounds.

## What an unwaited write does not tell you

Without `and wait` the section hands the work to the background and the next line runs immediately:

```sk
insert one entity into table "users":
    values:
        name: "Alice"
# Runs before the insert is done, and the insert's failure would only be logged.
if last database error is set:
    send "This will not report a failed insert." to console
```

What is still reported without waiting are the checks the section can make on the spot: no current
database, an unknown table, a value that cannot be converted, a `where` condition naming a column that
is not in the table. What is **not** reported is anything the database itself refused, which is logged to
the console with the script line instead. So without `and wait`:

- A write that failed may leave the error unset.
- A write that succeeded also leaves it unset.

If a script needs to know, use `and wait`.

## Writing and then reading

Reads always wait, and a write without `and wait` does not, so this can read stale data:

```sk
insert one entity into table "users" and wait:   # the wait is what makes the read below reliable
    values:
        name: "Alice"

select many entities from table "users" and store the results in {_users::*}:
    where all:
        name = "Alice"
```

The same applies to a read followed by a write that depends on it, and to a delete before a re-insert.
When the order matters, write `and wait` and let the script say so.

## Failures that are not about the database

Some failures happen before anything is sent, and they are reported the same way: a table that was never
registered on this connection, a column that the table description does not have, a value the column
cannot hold, `null` where the column says `not null`, or a second registration of the same table. All of
them appear in `last database error` right after the section.
