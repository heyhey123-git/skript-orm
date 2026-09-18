# Updating and deleting

[简体中文](updating-and-deleting.zh-CN.md) | **English**

Both come in two shapes: by a `where` block, which can match many rows, and by primary key, which
matches one.

## Update by condition

```sk
update entities in table "users" with limit 10 and wait:
    values:
        active: false
    where all:
        last_seen < {_cutoff}
        active = true
if last database error is set:
    send "Update failed: %last database error%" to console
```

The body holds a `values` block and an optional `where` block, in either order, and the values are
written exactly as in an insert: a column left out is not touched, and a literal `null` stores SQL NULL.
See [Writing rows](writing.md).

## Update by id

```sk
update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

This takes no `where` block: it updates the row whose registered primary key has that value.

**A key the table does not hold is not an error.** The statement touches nothing, `last database error`
stays unset, and `store affected rows` reports `0` — which is what to check when it matters whether the
row was there. `delete one entity ... by id` behaves the same way.

## Delete by condition

```sk
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
```

A delete has no values block; it only takes an optional `where` and an optional limit.

## Delete by id

```sk
delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "Delete failed: %last database error%" to console
```

That check answers "did the statement run", not "was there a row": a key nothing holds deletes nothing and
reports no error, so the count from `and store affected rows in {_rows}` is what says whether it did.

The colon marks a statement that has a body. This one has none, and neither has an `update`, an `upsert`
or an `insert` whose values come from a variable; those are written without it, as
[writing rows](writing.md) explains.

## Leaving the where out

**`update entities` and `delete entities` without a `where` block act on every row the implementation
allows them to.** Both sections accept that, so a forgotten `where` is not an error:

```sk
# Every row in the table.
delete entities from table "users" and wait
```

A `where` block whose condition list is empty is refused when the script is parsed, for `delete` just as
much as for `update`; to act on every row, write `delete entities from table "users"` with no `where`
block at all. When the intent is "all rows", write it as such and keep a `with limit` in mind.

## Limits

`with limit N` asks for at most N rows. MySQL writes `... LIMIT N`, PostgreSQL picks the rows by `ctid`
first so that `LIMIT` still applies, and MongoDB selects the ids the same way; only a generic `"JDBC"`
connection cannot express it, and it says so instead of ignoring the limit.

The limit has to resolve to a single positive number. A number that is zero or less is refused at run time
with `Update limit must be positive.` (or `Delete limit must be positive.`), but an expression that
resolves to nothing at all — an unset variable, or one holding several values — leaves the statement
**unlimited**, because there is nothing to apply. A limit used as a safety net is worth checking in the
script before the statement runs.

The limit is a safety net, not a paging mechanism: which rows it keeps is up to the database, so a
`where` block is what makes an update or a delete predictable.

## Waiting

Both sections wait for their work: the following lines run after the change has finished and a failure is
readable in `last database error`. The wait parks the trigger, not the server thread. `and wait` is still
accepted and does nothing, because every statement waits now. See
[Errors and waiting](errors-and-waiting.md).

## How many rows were changed

Both also take `and store affected rows in {_rows}`, which keeps the number of rows the statement touched:

```sk
delete entities from table "sessions" and store affected rows in {_deleted} and wait:
    where all:
        last_seen < {_cutoff}
```

A count of `0` means the statement ran and matched nothing, which is how an update guarded by the values
it read says that somebody else got there first. See [Affected rows](affected-rows.md).

## Updating from a variable

Both `update` and `upsert` also accept the new values as a variable shaped like a select result, which
is the shortest way to read a row, change it and store it back:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
set {_user::age} to {_user::age} + 1
set {_id} to {_user::id}
delete {_user::id}
update one entity {_user::*} in table "users" by id {_id} and wait
```

With `update`, only the columns in the variable are touched; with `upsert` the row is created when the
primary key is not there yet. Neither `update by id` nor `upsert by id` accepts a variable straight from a
select: a select result always contains the primary key, and both refuse values that contain it. Take the
key out first, as the example does, and pass its value to `by id` separately. See
[Cookbook](cookbook.md).
