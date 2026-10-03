# Updating and deleting

[简体中文](updating-and-deleting.zh-CN.md) | **English**

Updates and deletes each have two forms: a `where` block can match multiple rows, while a primary key matches at most one.

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

The body contains a `values` block and an optional `where` block, in either order. Values use the same format as inserts: omitted columns remain unchanged, and a literal `null` writes SQL NULL. See [Writing rows](writing.md).

## Update by id

```sk
update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

This updates the row with the given registered primary-key value and does not accept a `where` block.

**A missing key is not an error.** No row is changed, `last database error` remains unset, and `store affected rows` returns `0`. The same applies to `delete one entity ... by id`. For updates on MySQL, however, `0` can also mean an existing row already had the supplied values; it does not necessarily mean the row is missing.

## Delete by condition

```sk
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
```

Deletes accept an optional `where` block and limit, but no `values` block.

## Delete by id

```sk
delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "Delete failed: %last database error%" to console
```

The example's `if last database error is set` checks whether the delete failed. Deleting a missing key changes nothing and reports no error. Use `and store affected rows in {_rows}` to check whether a row was deleted.

`delete one entity ... by id` has no indented body, so it needs no colon. An `update` using a variable and no `where` block also needs no colon, as do `upsert` and `insert` using variables; see [writing rows](writing.md).

## Leaving the where out

**Without a `where` block, `update entities` and `delete entities` do not filter rows.** If `with limit` is also omitted, they update or delete every row in the table. Omitting `where` is not itself an error:

```sk
# Delete every row in the table.
delete entities from table "users" and wait
```

An empty `where` block is rejected when the script is parsed, for both updates and deletes. To act on all rows, omit the block entirely, as in `delete entities from table "users"`. Add `with limit` when you need to cap the number of affected rows.

On a generic `"JDBC"` connection, `update entities` and `delete entities` are refused even without `with limit`. Use the `by id` form for individual rows, or a supported connection type for multi-row operations. See [Troubleshooting](troubleshooting.md#limited-delete-is-not-supported-by-this-jdbc-dialect).

## Limits

`with limit N` affects at most N rows. MySQL uses `... LIMIT N`; PostgreSQL first selects the limited set of rows by `ctid`; MongoDB first selects their ids. Generic `"JDBC"` connections do not support this limit and report an error rather than ignoring it.

The limit must resolve to a **single positive number**. Zero or a negative number is rejected at runtime with `Update limit must be positive.` or `Delete limit must be positive.` If the expression produces no single value, such as an unset variable or one containing multiple values, the statement runs **without a limit**. If you rely on the limit as a safeguard, validate its value before running the statement.

A limit caps the count; it is not a pagination mechanism. The database chooses which rows to affect, so use `where` conditions to define the intended scope.

## Waiting

Both conditional and primary-key forms of `update` and `delete` finish before subsequent statements run. Failures are available in `last database error`. Waiting for the database pauses only the current script execution and does not occupy the server thread. `and wait` is still accepted but no longer changes the behaviour. See [Errors and waiting](errors-and-waiting.md).

## How many rows were changed

Both conditional and primary-key forms of `update` and `delete` accept `and store affected rows in {_rows}` to save the affected-row count:

```sk
delete entities from table "sessions" and store affected rows in {_deleted} and wait:
    where all:
        last_seen < {_cutoff}
```

For deletes, `0` means no row was deleted. For updates, it can mean no row matched, or, on MySQL, that the matched values did not change. When using previously read values as update conditions, ensure the write would change a value before treating zero as an unsuccessful conditional update. See [Affected rows](affected-rows.md).

## Updating from a variable

`update one entity {_user::*} ... by id {_id}` and `upsert one entity {_user::*} ... by id {_id}` read column values from a list variable. Each key names a column, such as `{_user::name}` or `{_user::age}`. You can therefore read a row with `select one`, change its values, and write it back. Pass the primary key separately to `by id` and remove it from the column-value variable:

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
set {_user::age} to {_user::age} + 1
set {_id} to {_user::id}
delete {_user::id}
update one entity {_user::*} in table "users" by id {_id} and wait
```

`update` changes only columns present in the variable; `upsert` also creates the row if its primary key does not exist. Neither `update by id` nor `upsert by id` accepts query results unchanged, because those results include the primary key, which is not allowed in the values. Save the key, remove it from the variable, and pass it separately to `by id`, as shown above. See [Cookbook](cookbook.md).
