# Raw statements

[简体中文](raw-statements.zh-CN.md) | **English**

**Raw statements are unsafe and have no safety net.** A raw statement is sent to the server exactly as you wrote it. The plugin does not parse it, does not check it against a registered table, does not verify that the databases, tables or columns it names exist, and does not translate it between implementations. Anything the connection's account can do, a raw statement can do — including dropping a table, dropping a database, or taking the server down. Use it for migrations and for server-specific statements a declaration cannot express. Do not use it to get around a declaration the plugin refused; if a refusal turns out to be wrong, that is a bug to fix rather than SQL to write.

Everywhere else in this documentation, a statement is built from a table you registered. This page is the one place where that stops being true.

## When to use one

- **A migration.** `ALTER TABLE`, `CREATE INDEX`, a data fix-up — the things a declaration has no syntax for.
- **A statement only your server understands.** A vendor extension, a hint, a statement the portable form does not cover.
- **A shape your declaration cannot express.** A join, a computed column, a `GROUP BY`.
- **A dynamic filter.** A `WHERE` or `ORDER BY` clause built while the script runs, which a `where` block cannot express because a column name is not a value.

Everything a declared statement does is also done better by a declared statement: it is checked before it is sent, it converts values to the column's own type, and it does not need you to know SQL. Raw statements are for what is left over.

## SQL statements

Two statements, because a relational server answers in two shapes: rows, or a count of what it affected.

```sk
execute query "SELECT id, name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}
execute update "ALTER TABLE users ADD COLUMN age INT NULL"
execute update "UPDATE users SET age = ? WHERE name = ?" with (30, "Alice") and store affected rows in {_rows}
```

- **`execute query`** stores rows. The keys are the column labels the server reported, numbered from one, exactly as `select many` stores its result: `{_rows::1::name}`, `{_rows::2::name}`. An alias is the name you read, so `SELECT name AS who` gives `{_rows::1::who}`. A read that matched nothing stores nothing, which is not a failure.
- **`execute update`** stores the affected-row count, or nothing when you leave the clause off. A server reports `0` for a statement it does not count, such as DDL, and that `0` is stored as it stands.
- **Both wait.** The lines after the statement run once the database has answered, and `and wait` is accepted but changes nothing.

## MongoDB commands

A document server takes documents, and answers every command with one document. That is a different shape from the two above, so it is a different statement rather than a spelling of one:

```sk
execute command "{ ""count"": ""users"" }" and store the result in {_answer::*}
send "There are %{_answer::n}% users."
```

The command is written as JSON text and parsed by the driver's own parser, so anything MongoDB accepts is accepted here, including its extended JSON forms for dates and object ids. The answer is stored as it arrived, so a field is read by name and a list by index: `{_answer::n}`, `{_answer::cursor::firstBatch::1::name}`.

There are no parameters. A command is the whole statement, so there is no placeholder to bind a value to.

## Parameters

Write a `?` where a value goes, and give the values in the `with` clause, in the same order:

```sk
execute query "SELECT name FROM users WHERE age > ? AND language = ?" with (18, "zh-CN") and store the result in {_rows::*}
```

- The number of values must match the number of `?` characters in the statement. A mismatch is refused before anything is sent.
- The values are bound by the driver, so a value can never be read as SQL. This is the only protection this feature offers, and it is why you should never build a statement by pasting values into it.
- The `?` characters are counted as written, including any inside a string literal or a comment. If your statement contains one of those, the count will not match what the driver expects, and the driver will say so in its own words.
- **A parameter must be a value the driver sends as it stands**: a string, a number, a boolean, a byte array, a date, or `null`. A UUID, an item stack, a location or another domain object is refused rather than converted, because a raw statement names no column and so there is no type to convert it for. Convert it in the script — a UUID to its string form or to its 16 bytes, an item to the bytes its serializer produces.

## What still holds, and what does not

| You may be used to | With a raw statement |
| --- | --- |
| An unknown column fails before the statement is sent | **No.** The server decides, when it receives it. |
| A declaration is compared with the table it names | **No.** A raw statement is registered nowhere. |
| A value is checked against its column's type and range | **No.** Values are bound as given. |
| A connection property nothing reads is refused | **Yes.** That check does not involve a statement. |
| A missing connection is reported before anything runs | **Yes.** The statement is refused, not sent. |
| A failure appears in `last database error`, in the server's own words | **Yes**, including the server's error code. |
| `?` placeholders cannot be read as SQL | **Yes**, and this is the only protection here. |
| A statement runs inside `database transaction` and rolls back with it | **Yes.** A raw statement is part of the transaction it runs in. |

## A statement the connection cannot take

Which kind of raw statement a connection accepts is decided by the implementation, not by the plugin: a SQL connection takes `execute query` and `execute update`, a document connection takes `execute command`. Using the wrong one is refused with the alternative named, before anything reaches the server:

```sk
in connection "logs":          # a MongoDB connection
    execute query "SELECT 1" and store the result in {_rows::*}
```

```
Raw SQL statements are not supported by database 'MongoDB'. It takes raw commands instead:
'execute command "…"'. See the Raw statements page for what a raw statement does and does not guarantee.
```

## The account's privileges are the limit

A raw statement runs as the connection's account, with that account's privileges and no others. An account that can only read and write rows in one database cannot drop it through a raw statement, and an account that can do anything can do anything here.

That is the one lever this feature gives you: give the connection the privileges its scripts need, not the ones the server administrator has. The [Connections](connections.md#credentials) page says the same thing about credentials.

## See also

- [Connections](connections.md) — the account a raw statement runs as.
- [Tables](tables.md) — what a declared statement checks, and why a raw one cannot.
- [Writing rows](writing.md) and [Reading rows](reading.md) — the declared forms, which are checked.
- [Cookbook](cookbook.md) — worked examples.
