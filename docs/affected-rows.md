# Affected rows

[简体中文](affected-rows.zh-CN.md) | **English**

A write can report how many rows it touched, and a script can act on that number. This is what makes a
conditional write possible without a transaction:

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

`store affected rows in {_rows}` is optional and belongs to every statement that writes: `insert one`,
`insert many`, `insert ... if absent`, `update`, `upsert` and `delete`, in the section form and in the
colon-free form alike. It is written with `and`, after the statement's own arguments: that conjunction is
what tells Skript where the argument in front of the clause ends. An `and wait` after it is accepted and
changes nothing.

```sk
delete entities from table "logs" with limit 500 and store affected rows in {_deleted}
insert one {_user::*} into table "archived_users" and store affected rows in {_rows}
upsert one entity in table "users" by id {_id} and store affected rows in {_rows}:
    values:
        name: "Alice"
```

The target is a single variable, `{_rows}` and not `{_rows::*}`: a list would hold the number under a key
nobody chose, and an expression cannot be written into at all. Both are refused when the script is parsed.

## What the number means

It is what the backend reports as written: rows an insert added, rows a delete removed, rows an update
wrote. Which updates count is the backend's answer rather than this addon's — a row an update matched
without changing is one row to PostgreSQL and to MongoDB, and zero rows to MySQL. Compare the number only
where the statement changes a value, as the example above does.

A backend that answers a batch without a per-row count reports no number at all rather than a wrong one.

## Set, unset and zero

The variable is **cleared when the statement starts**, before anything that could refuse it, and written
once the statement has finished and could count exactly:

| The variable holds | It means |
| --- | --- |
| a number | the statement ran and affected that many rows |
| nothing | no statement answered: it was refused or skipped, it failed, or the backend could not count |

**Zero is a real answer**, not a missing one: the statement ran and matched nothing. That is the useful
case, and `is set` is what tells the two apart:

```sk
if {_rows} is not set:
    send "Nothing reported how many rows were written." to console
else if {_rows} is 0:
    send "No row matched." to console
```

Clearing the variable first is what stops an older number from being read as this statement's answer. The
count is there when the next line runs: a write waits for its work, so nothing extra has to be written for
the clause to be readable. See [Errors and waiting](errors-and-waiting.md).

Inside a [transaction](transactions.md) it works the same way, and the variable is still cleared statement
by statement there: a statement the transaction skipped has not answered, and a number left over from the
statement before it would say otherwise. `last database error` is the one that is kept instead, so that
the cause of a rollback can still be read.

## A safe conditional write

Reading a value and writing a new one based on it is two statements, and another write can land between
them. Without transactions, the defence is to make the write itself check the value it was based on:

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
    # Nobody matched the balance that was read, so the row moved: read it again and retry.
    select one entity from table "accounts" and store the result in {_account::*}:
        where all:
            id = {_from}
    set {_balance} to {_account::balance}
```

The `where` repeats the value the script read, so the update can only match while the row still holds it.
One row affected means this statement was the one that changed it; zero means somebody else got there
first.

Two details are worth keeping in mind:

- The write has to change the row for MySQL to count it, so an amount of `0` looks like a lost race. Refuse
  a zero transfer before the loop rather than retrying it.
- The retry loop is bounded. A loop that never gives up can spin forever against a row that keeps being
  written, and a [transaction](transactions.md) is the answer when several rows have to move together
  rather than one.
