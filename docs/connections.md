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

- `"MySQL"` names the implementation. It is the only one this jar ships; see
  [Compatibility](compatibility.md).
- `url` is required. `username` and `password` may be empty strings.
- Any other literal property in the block is handed to the implementation untouched, so a future
  implementation can ask for more without new syntax.
- The section always waits: when the next line runs, the connection is either live or failed. `and wait`
  is neither needed nor accepted here.

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
- The plugin closes every connection when the server disables the plugin.

## Operations without a connection

Every section checks the connection first. With no connection in effect, an operation does nothing and
reports `No database connected.`; with `and wait` that lands in `last database error`, and without it
the failure is only logged. Once named connections exist but none is the default, the message says so
and lists the names. See [Errors and waiting](errors-and-waiting.md).

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
- It covers one statement. Waiting for a free connection, and `commit`/`rollback` waiting on a lock, are
  not covered by it; those are bounded by the server's own limits and by `socketTimeout` in the url.
- MySQL's `innodb_lock_wait_timeout` defaults to 50 seconds, longer than this, so a statement waiting on
  a lock is cancelled by this timeout first and reports a timeout rather than a lock wait. Raise this
  value past 50 if you would rather read MySQL's own message.

Inside a transaction, a statement gets what is left of the transaction's own timeout instead; see
[Transactions](transactions.md).

## Several operations at once

A connection keeps a small pool of database connections, so operations do not have to queue behind each
other. That is also why a write is visible to a read only after it has finished: two operations may run
on different pooled connections. `and wait` is what orders them from a script's point of view.

Each connection has a pool of its own, so the number of connections a script keeps open is the number
of pools the server runs.
