# Connections

[简体中文](connections.zh-CN.md) | **English**

Scripts create connections that remain available across the server. You can keep several connections open and choose which one each statement uses.

## Creating one

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
```

- `"MySQL"` names the implementation. The jar registers `"MySQL"`, `"MariaDB"`, `"PostgreSQL"`,
  `"MongoDB"` and `"JDBC"`; see [The implementation name](#the-implementation-name) below.
- `url` is required. `username` and `password` may be empty strings.
- Unknown properties cause the connection to fail instead of being ignored. For example, MySQL rejects
  `database: "mydb"` with `Connection property 'database' is not read by database 'MySQL'. It reads: password, statement timeout, url, username.`
  This also catches misspellings such as `statment timeout`. All types accept `url`, `username`, `password`,
  and `statement timeout`. `"JDBC"` also accepts `driver`; MongoDB accepts `database` and `auth database`.
- For `"MySQL"`, `"MariaDB"`, and `"PostgreSQL"`, put the database name in the URL path,
  as in `jdbc:mysql://localhost:3306/mydb`. The `database` property belongs to MongoDB.
- The section finishes connecting or reports an error before the next line runs. It does not accept `and wait`.
- You cannot create a connection inside `database transaction`; doing so reports
  `A connection cannot be created inside a database transaction. Roll it back first.`

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

The quoted value selects the database implementation. It must match one of these five names exactly, including case:

| Type name | Implementation |
| --- | --- |
| `"MySQL"` | MySQL's dialect: backtick identifiers, `ON DUPLICATE KEY UPDATE` for `upsert`, `LIMIT` on updates and deletes, `AUTO_INCREMENT`, and `LIMIT` paging. The plugin locates the server's MySQL driver (`com.mysql.cj.jdbc.Driver` or the older `com.mysql.jdbc.Driver`). Use this type for MySQL. |
| `"MariaDB"` | The same dialect as `"MySQL"`, since MariaDB accepts that SQL, with MariaDB Connector/J downloaded on first startup. The url has to be a `jdbc:mariadb://` one: the connector refuses a `jdbc:mysql://` url, and the MySQL driver refuses a `jdbc:mariadb://` one. Use this type for MariaDB. |
| `"PostgreSQL"` | PostgreSQL's dialect: `ON CONFLICT`, `EXCLUDED`, and `GENERATED … AS IDENTITY`. Row limits use `ctid` because PostgreSQL has no `UPDATE ... LIMIT`. The driver is downloaded on first startup. |
| `"MongoDB"` | Access through the downloaded blocking MongoDB Java driver, without SQL. Some statements behave differently; see [Compatibility](compatibility.md#mongodb). Connection properties are listed below. |
| `"JDBC"` | A driver specified through the `driver` property, with portable SQL: `"double quoted"` identifiers, `LIMIT 1` for a single row, and `LIMIT ? OFFSET ?` for paging. This row-limit syntax works in MySQL, MariaDB, SQLite, PostgreSQL and H2. Operations without a portable form are rejected: `insert ... if absent`, `upsert ... by id`, write limits, and `auto increment`. |

Use `"JDBC"` for SQLite. Paper includes its driver:

```sk
create a connection to database "JDBC" with properties:
    driver: "org.sqlite.JDBC"
    url: "jdbc:sqlite:plugins/myplugin/data.db"
```

Only `"JDBC"` needs a `driver` property. MySQL uses the server's driver; Paper downloads the MariaDB,
PostgreSQL, and MongoDB drivers on first startup. See [Compatibility](compatibility.md#what-is-inside-the-jar)
if the server cannot reach the download mirror.

Paper ships two drivers of its own, MySQL Connector/J and SQLite's:

- **MySQL.** The `"JDBC"` dialect uses `"double quoted"` identifiers. MySQL treats these as string
  literals unless `ANSI_QUOTES` is enabled, causing statements to fail. Use `"MySQL"` instead.
- **SQLite.** Use `"JDBC"`; no additional driver is needed. SQLite accepts the SQL this dialect
  generates, so a file connection supports all of the type's operations. Its limitations still apply:
  no auto increment, `insert ... if absent`, `upsert`, or write limits. The script must supply the key.

Names are case-sensitive: `"mysql"` reports `Database 'mysql' is not supported.` `"SQLite"` is not a
registered name either; use `"JDBC"`. See [Compatibility](compatibility.md) for supported products.

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
- **This addon does not support MongoDB transactions yet.** `database transaction` reports
  `This database implementation does not support transactions.` See [Compatibility](compatibility.md#mongodb).

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

| Priority | Connection | Syntax |
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

If the previous default has no registered name, it is closed because scripts can no longer select it.
A named connection remains open. The statement waits for any closure to finish before continuing and
cannot run during a `database transaction`.

## Disconnecting

```sk
disconnect from the current database        # the connection in effect
disconnect from connection "logs"           # one named connection
disconnect from all connections             # every connection
```

The first form follows the usual connection priority: it closes the connection selected by `in connection`
or `use connection`, or the default when neither applies. The next line waits for disconnection to finish.

Disconnecting a connection that a scope still names is not an error. Statements inside that scope fail
from then on, because the connection they resolve to is closed.

- The tables registered for a connection belong to that connection. Another connection starts with
  none registered, so registering the same table name on two connections is not a conflict. See
  [Tables](tables.md).
- A connection also accepts [raw statements](raw-statements.md) — SQL it sends as written, with none of
  the checks the statements above get. They run as this connection's account, with its privileges.
- The plugin closes all remaining connections when it is disabled. `disconnect from all connections`
  closes every registered connection.

## Operations without a connection

Every section checks the connection first. With no connection in effect, an operation does nothing and
reports `No database connected.` in `last database error`. Once named connections exist but none is the
default, the message says so and lists the names. See [Errors and waiting](errors-and-waiting.md).

## Credentials

The properties are written in the script, which means the account and its password are readable by
anyone who can read the script file or run `/sk` commands that print syntax. Two habits help:

- Give the database account only the privileges the scripts need.
- Keep connection definitions in one script so you can manage access to its credentials in one place.

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
  cancels the statement, and MySQL does that by killing the query from another connection. MariaDB needs
  no second connection — its connector asks the server to limit the statement, and the server aborts it.
- On SQL backends, it covers one statement after that statement has acquired a connection. Waiting for a free
  one is bounded by the pool instead: **30 seconds**, after which the statement fails with
  `HikariPool-1 - Connection is not available, request timed out after 30000ms`. `statement timeout: 0`
  removes the limit on the statement, not that wait. A `commit` or `rollback` waiting on a lock is bounded
  by the server's own limits and by `socketTimeout` in the url.
- On MongoDB the same property becomes the driver's socket read timeout, and the connection's
  `closeWaitTimeout` is that timeout plus five seconds, exactly as it is on the SQL side. What differs is
  what the two sides do when it expires: on SQL the driver cancels the statement, while MongoDB's driver
  only stops waiting for the response — the server finishes the statement it was sent.
  Either way the property bounds how long a script waits, not what the server does. It is applied over the
  url, so a `socketTimeoutMS` written into a connection string is replaced by this property and by its
  30-second default; set the value here instead, and `statement timeout: 0` leaves the url's own value
  alone.
- MySQL's `innodb_lock_wait_timeout` defaults to 50 seconds, longer than this, so a statement waiting on
  a lock is cancelled by this timeout first and reports a timeout rather than a lock wait. Raise this
  value past 50 if you would rather read MySQL's own message. MariaDB's server aborts the statement the
  same way, and its `innodb_lock_wait_timeout` also defaults to 50 seconds.
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
