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

## Delete by condition

```sk
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
```

A delete has no values block; it only takes an optional `where`, an optional limit and `and wait`.

## Delete by id

```sk
delete one entity from table "users" by id {_id} and wait:
if last database error is set:
    send "Delete failed: %last database error%" to console
```

## Leaving the where out

**`update entities` and `delete entities` without a `where` block act on every row the implementation
allows them to.** Both sections accept that, so a forgotten `where` is not an error:

```sk
# Every row in the table.
delete entities from table "users" and wait:
```

A `where` block whose condition list is empty is almost always a mistake; the sections that take a
values block, such as `update`, will refuse one, but a plain delete has nothing else to go on. When the
intent is "all rows", write it as such and keep a `with limit` in mind.

## Limits

`with limit N` asks for at most N rows and must be positive. On MySQL it becomes `... LIMIT N`, which
is a MySQL feature; an implementation that cannot express it says so instead of ignoring it.

The limit is a safety net, not a paging mechanism: which rows it keeps is up to the database, so a
`where` block is what makes an update or a delete predictable.

## Waiting

Both sections take `and wait`. With it, the following lines run after the change has finished and a
failure is readable in `last database error`; without it, the work goes to the background and a
database failure is only logged. The synchronous checks (no connection, unknown table, a value that does
not fit, a limit of zero) are reported in `last database error` either way. See
[Errors and waiting](errors-and-waiting.md).

## Updating from a variable

Both `update` and `upsert` also accept the new values as a variable shaped like a select result, which
is the shortest way to read a row, change it and store it back:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
set {_user::age} to {_user::age} + 1
update one entity {_user::*} in table "users" by id {_user::id} and wait:
```

With `update`, only the columns in the variable are touched; with `upsert` the row is created when the
primary key is not there yet. See [Cookbook](cookbook.md).
