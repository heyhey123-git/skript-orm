# skript-orm

<!-- Use an absolute image URL because this page also serves as the wiki home,
     where the repository's relative image path is unavailable. -->
![skript-orm: an ORM for Skript](https://raw.githubusercontent.com/heyhey123-git/skript-orm/master/docs/assets/banner.png)

An ORM for Skript: describe a table once, then read and write rows with Skript syntax instead of
writing SQL.

[简体中文](README.zh-CN.md) | **English**

## What a script looks like

```sk
on load:
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
command /adduser <text> <integer>:
    trigger:
        insert one entity into table "users" and wait:
            values:
                name: arg-1
                age: arg-2
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
| **PostgreSQL** | Also shipped, and tested in CI. Its driver is downloaded on the first start into the server's `libraries/`; see [Compatibility](docs/compatibility.md#what-is-inside-the-jar). |
| **MongoDB** | Also shipped, and tested in CI against MongoDB 8. Its driver is downloaded on the first start, the same way PostgreSQL's is. |
| **SkBee** | Optional; required only for `nbtcompound` columns. SkBee also provides the NBT compound objects used in scripts. |

## Install

1. Download `skriptorm-<version>.jar` from the releases page, or build it yourself
   ([CONTRIBUTION.md](CONTRIBUTION.md)).
2. Put the jar in `plugins/`, next to Skript.
3. Start the server once, then add a connection and table definition to a script and load it with
   `/sk reload`. Both are defined in Skript; no configuration file changes are needed.

## How it works

- **Multiple connections.** `create a connection` sets up the default connection; `named "logs"`
  creates a named connection. Use `in connection "logs":` or `use connection "logs"` to select one.
- **Skript syntax for each data operation.** Inserts, queries, updates and deletes have dedicated syntax.
  Forms with a `values` or `where` body use sections. All statements wait for completion.
- **One section per transaction.** `database transaction:` commits when its body ends, rolls back if
  a statement fails, and holds one connection throughout.
- **Affected-row counts.** `and store affected rows in {_rows}` saves the count. Its meaning depends
  on the operation and database; conditional updates can use it to detect concurrent changes.
  See [Affected rows](docs/affected-rows.md).
- **Errors available to scripts.** Every statement waits, so `last database error` contains any error
  from that operation before the next line runs. Successful statements leave it unset.

## Documentation

| Page | What is in it |
| --- | --- |
| [Getting started](docs/getting-started.md) | The shortest path from an empty script to a stored row. |
| [Connections](docs/connections.md) | Connection properties, named connections, switching, disconnecting. |
| [Tables](docs/tables.md) | Column syntax, every type, keys and modifiers, and what registering does not do. |
| [Writing rows](docs/writing.md) | Insert one, insert many, insert from a variable, upsert, `values` blocks. |
| [Reading rows](docs/reading.md) | Select one, many, page and by id, `where` blocks, and the shape of a result. |
| [Updating and deleting](docs/updating-and-deleting.md) | Update and delete by condition or by id, and limits. |
| [Affected rows](docs/affected-rows.md) | The `store affected rows` clause, and a conditional write without transactions. |
| [Errors and waiting](docs/errors-and-waiting.md) | What waits, `last database error`, and what a failure does. |
| [Transactions](docs/transactions.md) | All-or-nothing groups of statements, and what ends them. |
| [Types](docs/types.md) | What each column type accepts and how it is stored. |
| [Troubleshooting](docs/troubleshooting.md) | Schema changes that do not take effect, missing NULL values, and NBT without SkBee. |
| [Cookbook](docs/cookbook.md) | Recipes for the things scripts usually need. |
| [Compatibility](docs/compatibility.md) | Versions, the type names a script can write, what ships in the jar, and what is not supported. |
| [Changelog](CHANGELOG.md) | What each release changed, which is also what its release page says. |

## Building from source

`./gradlew build` produces the shaded jar in `build/dist/`. `./gradlew serverTest` boots a real Paper
server and runs the plugin against it. Both are described in [CONTRIBUTION.md](CONTRIBUTION.md).

## License

MIT. See [LICENSE](LICENSE).

