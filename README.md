# xiaojie-orm

An ORM for Skript: describe a table once, then read and write rows with Skript syntax instead of
writing SQL.

[简体中文](README.zh-CN.md) | **English**

## What a script looks like

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"

register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable

command /whois <text>:
    trigger:
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        if last database error is set:
            send "Lookup failed: %last database error%" to sender
            stop
        send "name: %{_user::name}%, age: %{_user::age}%" to sender
```

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "Insert failed: %last database error%" to console
```

There is no SQL in either block. The plugin builds the statements, runs them off the server thread,
and hands results back as ordinary Skript variables and values.

## Requirements

| | |
| --- | --- |
| **Paper** | 26.2 or newer, the server line this build compiles against. |
| **Skript** | 2.16.2 or newer. On anything older the plugin disables itself and says why in the console. |
| **MySQL** | The shipped implementation, tested against MySQL 8. Paper already ships the driver, so there is nothing to install for it. |
| **SkBee** | Optional, and only for `nbtcompound` columns. Scripts can only build an NBT compound when SkBee is installed in any case. |

## Install

1. Download `xiaojieorm-<version>.jar` from the releases page, or build it yourself
   ([CONTRIBUTION.md](CONTRIBUTION.md)).
2. Put the jar in `plugins/`, next to Skript.
3. Start the server once, then write the connection and the table into a script and `/sk reload` it.
   Both are things a script does, so no config file is involved.

## How it works

- **As many connections as the script wants.** `create a connection` makes that database the default
  one, `named "logs"` keeps another one alongside it, and `in connection "logs":` or
  `use connection "logs"` says which one a statement uses.
- **Everything that touches rows is a section.** Writing, reading, updating and deleting are separate
  syntaxes with their own bodies, and a read always waits for its result.
- **A transaction is one section.** `database transaction:` commits when its body ends, rolls back when
  a statement in it fails, and holds one connection for as long as it runs.
- **A failure is a value.** After an operation written with `and wait`, `last database error` holds
  what went wrong, and an operation that worked leaves it unset.

## Documentation

| Page | What is in it |
| --- | --- |
| [Getting started](docs/getting-started.md) | The shortest path from an empty script to a stored row. |
| [Connections](docs/connections.md) | Connection properties, named connections, switching, disconnecting. |
| [Tables](docs/tables.md) | Column syntax, every type, keys and modifiers, and what registering does not do. |
| [Writing rows](docs/writing.md) | Insert one, insert many, insert from a variable, upsert, `values` blocks. |
| [Reading rows](docs/reading.md) | Select one, many, page and by id, `where` blocks, and the shape of a result. |
| [Updating and deleting](docs/updating-and-deleting.md) | Update and delete by condition or by id, and limits. |
| [Errors and waiting](docs/errors-and-waiting.md) | `and wait`, `last database error`, and what runs in the background. |
| [Transactions](docs/transactions.md) | All-or-nothing groups of statements, and what ends them. |
| [Types](docs/types.md) | What each column type accepts and how it is stored. |
| [Troubleshooting](docs/troubleshooting.md) | The traps: silent schema changes, invisible NULLs, NBT without SkBee. |
| [Cookbook](docs/cookbook.md) | Recipes for the things scripts usually need. |
| [Compatibility](docs/compatibility.md) | Versions, what ships in the jar, and what is not supported. |

## Building from source

`./gradlew build` produces the shaded jar in `build/dist/`. `./gradlew serverTest` boots a real Paper
server and runs the plugin against it. Both are described in [CONTRIBUTION.md](CONTRIBUTION.md).
