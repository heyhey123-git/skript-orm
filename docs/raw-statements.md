# Raw statements

[简体中文](raw-statements.zh-CN.md) | **English**

Raw statements let you send SQL or MongoDB commands directly. They do not need a registered table, and the plugin does not check their table or column names, convert values using declared column types, or adapt SQL for another database. They can do anything the connection's account is allowed to do, including dropping tables or databases.

Use them for migrations or operations the table-based syntax cannot express. If a table-based statement is rejected unexpectedly, report the problem instead of working around it with raw SQL.

Server tests parse the [example script](examples/raw-statements.sk) to check its syntax.

## When to use one

- **Schema changes and data migrations:** `ALTER TABLE`, `CREATE INDEX`, or a data correction.
- **Server-specific SQL:** vendor extensions and optimizer hints.
- **Queries beyond the table-based syntax:** joins, computed columns, and `GROUP BY`.
- **Dynamic clauses:** a `WHERE` or `ORDER BY` built at runtime. A `where` block cannot use a value as a column name.

Use table-based statements when possible. They check columns and convert values before sending a query.

## SQL statements

Use `execute query` for rows and `execute update` for an affected-row count:

```sk
execute query "SELECT id, name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}
execute update "ALTER TABLE users ADD COLUMN age INT NULL"
set {_values::*} to 30, "Alice"
execute update "UPDATE users SET age = ? WHERE name = ?" with {_values::*} and store affected rows in {_rows}
```

- **`execute query`** stores rows like `select many`: `{_rows::1::name}`, `{_rows::2::name}`. Keys use the column labels returned by the server. For example, `SELECT name AS who` gives `{_rows::1::who}`. No matching rows means nothing is stored; it is not an error.
- **`execute update`** can store the affected-row count. If the server reports `0`, as it may
  for a schema change such as `ALTER TABLE`, the stored value is `0`.
- **`execute query` and `execute update` both wait** for the database before the next line runs. Adding `and wait` has no effect.

## MongoDB commands

MongoDB commands return a document (a JSON-like object) rather than SQL rows or an affected-row
count. Use `execute command`:

```sk
execute command "{ ""count"": ""users"" }" and store the result in {_answer::*}
send "There are %{_answer::n}% users."
```

Write the `execute command` argument as JSON text. The MongoDB driver parses the JSON, including extended JSON for dates and object IDs. Read fields from the returned document by name and array elements by index, for example `{_answer::n}` or `{_answer::cursor::firstBatch::1::name}`.

`execute command` does not support parameters. Supply the complete command as JSON.

## Parameters

Write a `?` where a value goes, and give the values in the `with` clause, in the same order:

```sk
execute query "SELECT name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}

set {_values::*} to 18, "zh-CN"
execute query "SELECT name FROM users WHERE age > ? AND language = ?" with {_values::*} and store the result in {_rows::*}
```

The `with` clause accepts one value or a list variable. For multiple values, put them in a list variable first. `with (18, "zh-CN")` fails to parse because Skript cannot pass that literal list to `with`.

- The number of values must match the number of `?` characters in the statement. A mismatch is refused before anything is sent.
- The driver sends parameter values separately from the SQL text, so a value is not interpreted
  as part of the command. Always use `?` parameters for values instead of concatenating them
  into the SQL string.
- The plugin counts every `?`, including those in SQL strings and comments. Avoid `?` in those places: the plugin's count can then disagree with the driver's parameter count.
- **Parameters must be values the driver can send directly:** strings, numbers, booleans, byte arrays, dates, or `null`. UUIDs, item stacks, locations, and other application objects are rejected because raw statements provide no column type to guide conversion. Convert them before passing them as parameters.

## Checks and guarantees

| Behavior | Raw statements |
| --- | --- |
| An unknown column in raw SQL fails before the statement is sent | **No.** The database checks column names after receiving the raw SQL. |
| Registered table and column declarations are checked | **No.** Raw statements do not use the addon's registered table definitions. |
| A value is checked against its column's type and range | **No.** Values are bound as given. |
| An unrecognized connection property is refused | **Yes.** Connection creation checks properties independently of raw statements. |
| A missing connection is reported before anything runs | **Yes.** The statement is refused, not sent. |
| A failure appears in `last database error`, in the server's own words | **Yes**, including the server's error code. |
| Parameter values are kept separate from SQL | **Yes**, when you use `?` placeholders. |
| A statement runs inside `database transaction` and rolls back with it | **Yes.** A raw statement inside `database transaction:` belongs to that transaction. |

## Using the right form for the connection

SQL connections accept `execute query` and `execute update`; MongoDB connections accept `execute command`. If you use the wrong form, the plugin reports the expected one before sending anything:

```sk
in connection "logs":          # a MongoDB connection
    execute query "SELECT 1" and store the result in {_rows::*}
```

```
Raw SQL statements are not supported by database 'MongoDB'. It takes raw commands instead:
'execute command "…"'. See the Raw statements page for what a raw statement does and does not guarantee.
```

## Account permissions

A raw statement runs with the connection account's privileges. Give that account only the permissions its scripts need. See [Credentials](connections.md#credentials).

## See also

- [Connections](connections.md) — the account a raw statement runs as.
- [Tables](tables.md) — the checks available to table-based statements.
- [Writing rows](writing.md) and [Reading rows](reading.md) — table-based statements that check columns and types.
- [Cookbook](cookbook.md) — worked examples.
