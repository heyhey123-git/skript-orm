# Connections

[简体中文](connections.zh-CN.md) | **English**

A connection is made by a script and belongs to the whole server. There is one current database at a
time, and every other operation uses it.

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

## One at a time

Creating another connection **disconnects the current one first**, and then connects the new one:

- While a connection is being made, the previous one is already gone.
- If the new one fails, the server is left with **no** current database, not with the old one. Every
  operation after that reports `No database connected.` until a connection succeeds.
- The tables registered for a connection belong to that connection. A new connection starts with none
  registered, so a script that reconnects has to register its tables again. See [Tables](tables.md).
- The plugin closes the current connection when the server disables the plugin.

## Disconnecting

```sk
disconnect from the current database
```

It runs asynchronously and the following line waits for it, so a script can disconnect and then do
something else. `disconnect` on its own line inside a trigger is the whole syntax.

## Operations without a connection

Every section checks the connection first. With no current database, an operation does nothing and
reports `No database connected.`; with `and wait` that lands in `last database error`, and without it
the failure is only logged. See [Errors and waiting](errors-and-waiting.md).

## Credentials

The properties are written in the script, which means the account and its password are readable by
anyone who can read the script file or run `/sk` commands that print syntax. Two habits help:

- Give the database account only the privileges the scripts need.
- Keep the connection in its own script, so the file that holds the credentials is the file you think
  about when you set permissions.

## Several operations at once

A connection keeps a small pool of database connections, so operations do not have to queue behind each
other. That is also why a write is visible to a read only after it has finished: two operations may run
on different pooled connections. `and wait` is what orders them from a script's point of view.
