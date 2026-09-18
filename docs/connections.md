# Connections

[简体中文](connections.zh-CN.md) | **English**

A connection is made by a script and belongs to the whole server. A script can keep several at once,
and each statement decides which one it uses.

## Creating one

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
```

- `"MySQL"` names the implementation. The jar registers `"MySQL"`, `"PostgreSQL"`, `"MongoDB"` and
  `"JDBC"`; see [The implementation name](#the-implementation-name) below.
- `url` is required. `username` and `password` may be empty strings.
- Any other literal property in the block is handed to the implementation, which looks up only the names
  it knows: one it does not know is **ignored without a word**, so a misspelled `statment timeout`
  changes nothing at all and reports nothing. What the implementations read is `statement timeout`, the
  `driver` of a `"JDBC"` connection, and MongoDB's `database` and `auth database`.
- The section always waits: when the next line runs, the connection is either live or failed. `and wait`
  is neither needed nor accepted here.
- **It is refused inside a `database transaction`.** The section reports
  `A connection cannot be created inside a database transaction. Roll it back first.` and connects
  nothing, because replacing the connection would take the transaction with it.

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
if last database error is set:
    send "Connection failed: %last database error%" to console
    stop
```

This connection has no name. It becomes the **default** connection, which is the one a statement uses
when nothing else says otherwise, so a script with a single database never has to think about any of
this.

## The implementation name

The quoted word is a **type name**, not the name of a database product: it selects the code that builds
the statements and reads the rows back, and it is matched exactly, case-sensitively. The jar registers
four:

| Type name | What it brings |
| --- | --- |
| `"MySQL"` | MySQL's dialect — backtick identifiers, `ON DUPLICATE KEY UPDATE` for `upsert`, `LIMIT` on updates and deletes, `AUTO_INCREMENT`, `LIMIT` paging — and the MySQL driver, found for the server (`com.mysql.cj.jdbc.Driver`, or the older `com.mysql.jdbc.Driver`). This is what a script almost always wants. |
| `"PostgreSQL"` | PostgreSQL's dialect — `ON CONFLICT`, `EXCLUDED`, `GENERATED … AS IDENTITY`, and a row limit written through `ctid` because PostgreSQL has no `UPDATE ... LIMIT` — and the driver the plugin downloads for it on the first start. |
| `"MongoDB"` | MongoDB, through the blocking MongoDB Java driver the plugin downloads for it. There is no SQL under it, so several statements answer differently on purpose; [Compatibility](compatibility.md#mongodb) lists them, and its properties are below. |
| `"JDBC"` | A driver *you* name in a `driver` property, plus a dialect that writes portable SQL: `"double quoted"` identifiers, `LIMIT 1` to take a single row, and `LIMIT ? OFFSET ?` to page — row limiting in the one form MySQL, MariaDB, SQLite, PostgreSQL and H2 all take. Whatever has no portable form — `insert ... if absent`, `upsert ... by id`, a limit on a write, `auto increment` — is refused rather than guessed at. |

Where the driver comes from is the difference between the types. `"PostgreSQL"` and `"MongoDB"` name
drivers this plugin fetches and keeps ready; `"JDBC"` names a class that has to be on the server's
classpath already, which is what it is for. `"JDBC"` is how SQLite is reached, since Paper carries its
driver:

```sk
create a connection to database "JDBC" with properties:
    driver: "org.sqlite.JDBC"
    url: "jdbc:sqlite:plugins/myplugin/data.db"
```

Only `"JDBC"` asks you for a class name, and the jar bundles no driver for any of the four. What a
`"PostgreSQL"` or `"MongoDB"` connection needs instead of a class name is nothing: both drivers are
fetched on the first start, which [Compatibility](compatibility.md#what-is-inside-the-jar) explains —
including what a server that cannot reach the mirror it comes from has to do about it.

Paper ships two drivers of its own, MySQL Connector/J and SQLite's:

- **MySQL.** The dialect writes `"double quoted"` identifiers, which MySQL reads as string literals
  unless its `ANSI_QUOTES` mode is on, so a MySQL connection written as `"JDBC"` fails on the quoting
  before it reaches anything else. Write `"MySQL"` for MySQL.
- **SQLite.** `"JDBC"` is the way to reach it, and the driver is already there: nothing the dialect
  writes is foreign to SQLite, so a connection to a file works for everything the type supports. What
  the dialect refuses stays refused, which on SQLite means no auto increment, no `insert ... if absent`,
  no `upsert` and no limit on a write, so a table's key is one the script supplies.

So `"mysql"` is refused with `Database 'mysql' is not supported.`, and so are `"MariaDB"` and `"SQLite"`:
those are products, not types. `"MongoDB"` is both, the way `"MySQL"` is. What a connection reaches is a
product; what a script names is one of the four types above, and [Compatibility](compatibility.md) keeps
the two lists apart.

## MongoDB properties

A MongoDB connection takes the same block, with its own meaning for `url`:

```sk
create a connection to database "MongoDB" with properties:
    url: "mongodb://localhost:27017"
    username: "admin"
    password: "p@ss:w/rd"
    database: "logs"
    auth database: "admin"
```

- `url` is either a bare `host:port` or a whole connection string beginning with `mongodb://` or
  `mongodb+srv://`, and a full string keeps its options: `mongodb://host:27017/logs?retryWrites=false` is
  handed over as written.
- `username` and `password` are separate properties, as they are for the SQL implementations, and are
  given to the driver as strings rather than pasted into the url. A password containing `@`, `:`, `/` or
  `%` is therefore just a password, and needs no escaping.
- `database` names the database to use, and a `mongodb://host:27017/mydb` url names one too. The declared
  `database` wins over the one in the url; with neither, `skript-orm` is used.
- `auth database` names the database the credentials belong to, for a server whose users live elsewhere.
  It defaults to the database being used.
- **Transactions are not implemented for MongoDB here.** MongoDB itself has multi-document transactions —
  on a replica set from 4.0, on a sharded cluster from 4.2, while a standalone server refuses them — but this
  connection does not open one yet, so a `database transaction` section fails with
  `This database implementation does not support transactions.`; see
  [Compatibility](compatibility.md#mongodb).

## Naming one

Add `named "..."` to keep a connection under a name:

```sk
create a connection named "logs" to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/logs"
    username: "root"
    password: "123456"
```

- A named connection is registered under that name and **leaves every other connection alone**.
- The first connection to succeed becomes the default, named or not.
- Creating a name that is already in use replaces that connection: the old one is disconnected, and
  the ones under other names are untouched. Running the same `on load` again is how a script
  reconnects.
- Names are what a script can point at. `main`, `logs`, `archive`: anything you would say out loud.

An unnamed connection replaces the default, as it always has. The connection it replaces is
disconnected only if no name keeps it reachable, so a script that created `"logs"` and then creates an
unnamed connection keeps `"logs"` open and merely stops using it for unqualified statements.

## Which connection a statement uses

A statement asks three questions, in this order, and uses the first answer it gets:

| Order | Answer | Written as |
|---|---|---|
| 1 | the innermost scope it is inside | `in connection "logs":` |
| 2 | what this event switched to | `use connection "logs"` |
| 3 | the default connection | the unnamed `create a connection` |

If none of them answers, the statement does nothing and reports `No database connected.` A name that
was never created, or a connection that has been disconnected, is a mistake rather than a fallback:
the statement fails against the connection it was pointed at instead of quietly using another one.

## Switching for a while

`in connection` runs a block against a named connection:

```sk
in connection "logs":
    insert one entity into table "entries" and wait:
        values:
            message: "written to the logs database"
```

The switch lasts for the block and nothing more, which is what makes reading from one database and
writing to another a readable script:

```sk
in connection "archive":
    select many entities from table "entries" and store the results in {_entries::*}

in connection "logs":
    insert many entities into table "entries" from {_entries::*} and wait
```

An unknown name skips the block and reports it, so a typo cannot turn into a write against the default
connection.

## Switching for the rest of the event

`use connection` switches immediately for the rest of the current event:

```sk
command /newlog <text>:
    trigger:
        use connection "logs"
        insert one entity into table "entries" and wait:
            values:
                message: arg-1
```

It does no database work, so unlike every other statement here it does not wait: the very next line
already runs against it. Inside an `in connection` block, the block still wins, and a `use connection`
written inside one is undone when the block ends.

Two handlers listening to the same event share this switch, because it is kept per event. If that
matters, use `in connection` in each handler instead.

## Choosing the default

```sk
make connection "logs" the default
```

Unqualified statements then use `"logs"` until something else becomes the default. The connection the
script is currently in is not affected: a statement that already resolved its connection keeps it.

The connection that had the role **is disconnected when it has no name of its own**: no statement can
resolve to it afterwards, so leaving it running would only hold its pool of ten server connections open
until the server stops. A **named** connection keeps running and merely stops being the default, because a
scope or a `use connection` may still be using it. The statement waits for that close, so the next line
already sees the new default, and it is refused while a `database transaction` is open.

## Disconnecting

```sk
disconnect from the current database        # the connection in effect
disconnect from connection "logs"           # one named connection
disconnect from all connections             # every connection
```

The first form resolves like every other statement: inside `in connection "logs":` it closes `"logs"`,
after `use connection "logs"` it closes `"logs"`, and with neither it closes the default. It runs
asynchronously and the following line waits for it, so a script can disconnect and then do something
else.

Disconnecting a connection that a scope still names is not an error. Statements inside that scope fail
from then on, because the connection they resolve to is closed.

- The tables registered for a connection belong to that connection. Another connection starts with
  none registered, so registering the same table name on two connections is not a conflict. See
  [Tables](tables.md).
- The plugin closes every connection when the server disables the plugin, and `disconnect from all
  connections` closes every one of them: a connection that lost the default role either has a name and is
  still registered, or was closed when it lost the role. See [Choosing the default](#choosing-the-default).

## Operations without a connection

Every section checks the connection first. With no connection in effect, an operation does nothing and
reports `No database connected.` in `last database error`. Once named connections exist but none is the
default, the message says so and lists the names. See [Errors and waiting](errors-and-waiting.md).

## Credentials

The properties are written in the script, which means the account and its password are readable by
anyone who can read the script file or run `/sk` commands that print syntax. Two habits help:

- Give the database account only the privileges the scripts need.
- Keep the connections in their own script, so the file that holds the credentials is the file you
  think about when you set permissions.

## Statement timeout

Every statement gets **30 seconds**. Change it per connection:

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
    statement timeout: 15
```

- `0` means no limit, which is what the driver does by default: wait as long as it takes.
- The timeout exists because one statement that never finishes holds one of the pool's connections until
  the server is restarted, and there is nothing else that would end it.
- What it guarantees is that the script stops waiting, not that the server stopped working: the driver
  cancels the statement, and MySQL does that by killing the query from another connection.
- It covers one statement, and only after that statement has a connection to run on. Waiting for a free
  one is bounded by the pool instead: **30 seconds**, after which the statement fails with
  `HikariPool-1 - Connection is not available, request timed out after 30000ms`. `statement timeout: 0`
  removes the limit on the statement, not that wait. A `commit` or `rollback` waiting on a lock is bounded
  by the server's own limits and by `socketTimeout` in the url.
- On MongoDB the same property becomes the driver's socket read timeout, and the connection's
  `closeWaitTimeout` is that timeout plus five seconds, exactly as it is on the SQL side. What differs is
  what the two sides do when it expires: MySQL cancels the statement from another connection, while
  MongoDB's driver only stops waiting for the response — the server finishes the statement it was sent.
  Either way the property bounds how long a script waits, not what the server does. It is applied over the
  url, so a `socketTimeoutMS` written into a connection string is replaced by this property and by its
  30-second default; set the value here instead, and `statement timeout: 0` leaves the url's own value
  alone.
- MySQL's `innodb_lock_wait_timeout` defaults to 50 seconds, longer than this, so a statement waiting on
  a lock is cancelled by this timeout first and reports a timeout rather than a lock wait. Raise this
  value past 50 if you would rather read MySQL's own message.
- The value must be a whole number of seconds. Anything else is refused with `Connection property
  'statement timeout' must be a whole number of seconds, but was 'x'.`, and a negative value with
  `Connection property 'statement timeout' must not be negative, but was -1.`

Inside a transaction, a statement gets what is left of the transaction's own timeout instead; see
[Transactions](transactions.md).

## Several operations at once

A connection keeps a pool of **ten** database connections of its own, so operations do not have to queue
behind each other; an eleventh at the same time waits, for up to the 30 seconds above. The size is not
something a script can set: no connection property reaches the pool.

That is also why a write is visible to a read only after it has finished: two operations may run on
different pooled connections. What orders them from a script's point of view is the wait every statement
performs.

Each connection has a pool of its own, so a script with three connections can hold thirty server
connections open — worth counting against the database's own `max_connections`.
