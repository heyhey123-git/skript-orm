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
- Other literal properties are passed to the implementation. **Unknown properties are silently ignored**:
  misspelling `statement timeout` as `statment timeout` has no effect and produces no error.
  Supported extra properties are `statement timeout`, `driver` for `"JDBC"`, and MongoDB's
  `database` and `auth database`.
- The section always waits: when the next line runs, the connection is either live or failed. `and wait`
  is neither needed nor accepted here.
- **It is refused inside a `database transaction`.** The section reports
  `A connection cannot be created inside a database transaction. Roll it back first.` and connects
  nothing, because replacing the connection would disrupt the active transaction.

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
if last database error is set:
    send "Connection failed: %last database error%" to console
    stop
```

This unnamed connection becomes the **default**. Statements use it unless another connection is
specified, so a script with one database usually needs no connection-switching syntax.

## The implementation name

The quoted value is an implementation **type name**, not an arbitrary database product name.
It selects the code used to build statements and read results. Matching is exact and case-sensitive.
The jar registers four types:

| Type name | What it brings |
| --- | --- |
| `"MySQL"` | MySQL's dialect: backtick identifiers, `ON DUPLICATE KEY UPDATE` for `upsert`, `LIMIT` on updates and deletes, `AUTO_INCREMENT`, and `LIMIT` paging. The plugin locates the server's MySQL driver (`com.mysql.cj.jdbc.Driver` or the older `com.mysql.jdbc.Driver`). Use this type for MySQL. |
| `"PostgreSQL"` | PostgreSQL's dialect: `ON CONFLICT`, `EXCLUDED`, and `GENERATED … AS IDENTITY`. Row limits use `ctid` because PostgreSQL has no `UPDATE ... LIMIT`. The driver is downloaded on first startup. |
| `"MongoDB"` | Access through the downloaded blocking MongoDB Java driver, without SQL. Some statements behave differently; see [Compatibility](compatibility.md#mongodb). Connection properties are listed below. |
| `"JDBC"` | A driver specified through the `driver` property, with portable SQL: `"double quoted"` identifiers, `LIMIT 1` for a single row, and `LIMIT ? OFFSET ?` for paging. This row-limit syntax works in MySQL, MariaDB, SQLite, PostgreSQL and H2. Operations without a portable form are rejected: `insert ... if absent`, `upsert ... by id`, write limits, and `auto increment`. |

The types also differ in how drivers are provided. PostgreSQL and MongoDB drivers are downloaded
for the plugin; a `"JDBC"` driver class must already be on the server's classpath.
Use `"JDBC"` for SQLite, whose driver is included with Paper:

```sk
create a connection to database "JDBC" with properties:
    driver: "org.sqlite.JDBC"
    url: "jdbc:sqlite:plugins/myplugin/data.db"
```

Only `"JDBC"` requires a driver class name. None of the four drivers is bundled in this jar.
PostgreSQL and MongoDB drivers are downloaded on first startup, with no class name needed.
See [Compatibility](compatibility.md#what-is-inside-the-jar) for details and options if the server
cannot reach the download mirror.

Paper ships two drivers of its own, MySQL Connector/J and SQLite's:

- **MySQL.** The `"JDBC"` dialect uses `"double quoted"` identifiers. MySQL treats these as string
  literals unless `ANSI_QUOTES` is enabled, causing statements to fail. Use `"MySQL"` instead.
- **SQLite.** Use `"JDBC"`; no additional driver is needed. SQLite accepts the SQL this dialect
  generates, so a file connection supports all of the type's operations. Its limitations still apply:
  no auto increment, `insert ... if absent`, `upsert`, or write limits. The script must supply the key.

`"mysql"` is rejected with `Database 'mysql' is not supported.`. `"MariaDB"` and `"SQLite"` are also
rejected because they are not registered type names. `"MongoDB"` and `"MySQL"` are both product names
and type names, but the distinction matters: scripts must use one of the four types above.
[Compatibility](compatibility.md) lists types and database products separately.

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
- Choose a recognizable name that scripts can refer to, such as `main`, `logs`, or `archive`.

An unnamed connection replaces the default, as it always has. The connection it replaces is
disconnected only if no name keeps it reachable, so a script that created `"logs"` and then creates an
unnamed connection keeps `"logs"` open and merely stops using it for unqualified statements.

## Which connection a statement uses

Statements select a connection in the following order of precedence:

| Order | Answer | Written as |
|---|---|---|
| 1 | the innermost scope it is inside | `in connection "logs":` |
| 2 | what this event switched to | `use connection "logs"` |
| 3 | the default connection | the unnamed `create a connection` |

If none is set, the statement does nothing and reports `No database connected.`. If the selected name
does not exist or its connection is closed, the statement fails rather than falling back to another
connection.

## Switching for a while

`in connection` runs a block against a named connection:

```sk
in connection "logs":
    insert one entity into table "entries" and wait:
        values:
            message: "written to the logs database"
```

The switch applies only within the block, making cross-database reads and writes explicit:

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

The previous default **is disconnected if it has no registered name**, since no future statement can
select it. This releases its resources, including up to ten pooled connections on SQL backends. A **named** connection
stays open because a scope or `use connection` may still refer to it. The statement waits for closure
before continuing, so subsequent lines use the new default. It is rejected during a `database transaction`.

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
- The timeout prevents an unfinished statement from holding a pooled connection indefinitely,
  potentially until the server restarts.
- What it guarantees is that the script stops waiting, not that the server stopped working: the driver
  cancels the statement, and MySQL does that by killing the query from another connection.
- On SQL backends, it covers one statement after that statement has acquired a connection. Waiting for a free
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

Each SQL connection has a Hikari pool of up to **ten** database connections, allowing concurrent
operations. An eleventh concurrent operation waits for up to the 30 seconds described above.
The pool size cannot be changed through script connection properties.

Different operations may use different pooled connections. Within one trigger, a read waits for the
preceding write to complete because each statement waits. Visibility between concurrent operations
depends on transaction isolation.

Each SQL connection has its own pool, so three connections can use up to thirty database connections.
Account for this when planning around the database's `max_connections` limit.
