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

The plugin generates and runs the database statements off the server thread. Results are available
through ordinary Skript variables.

## Requirements

| | |
| --- | --- |
| **Paper** | 26.2 or newer. This build targets the 26.2 release series. |
| **Skript** | 2.16.2 or newer. With an older version, the addon disables itself and explains why in the console. |
| **MySQL** | Support is included and tested with MySQL 8. Paper includes the driver. |
| **PostgreSQL** | Support is included and tested in CI. The driver is downloaded to the server's `libraries/` directory on first startup; see [Compatibility](docs/compatibility.md#what-is-inside-the-jar). |
| **MongoDB** | Support is included and tested with MongoDB 8 in CI. Its driver is also downloaded on first startup. |
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

## Performance and limits

Reads of up to 5000 rows did not lengthen a tick in the measurements below. Large writes did, so avoid
running them during busy periods. Reads above the limit fail without storing a partial result; large
writes are split into smaller statements. These measurements describe the listed machines, not a
guaranteed result on every server. CI jobs with the same label may run on different processors.

| What was measured | Result | Machine |
| --- | --- | --- |
| Writing 5000 rows | The longest tick exceeded 50 ms by 70 to 90 ms on the first run; later runs varied from no overrun to about 30 ms | disposable Paper 26.2 server, AMD Ryzen 5 5600X, JDK 25 |
| Writing 100 rows, and 10 000 rows | about 10 ms, and 100 to 110 ms, past a tick | the same server |
| Reading 100 to 5000 rows | no read lengthened a tick, and every row asked for was stored | the same server |
| Reading 10 000 rows | refused: the result variable is cleared and `last database error` names the ceiling | the same server |
| A 5000-row `insert many` through the generic JDBC path | 9.960 ± 0.384 ms, and 6.1 to 10.0 ms across three CI hosts | Ryzen 5 5600X; AMD EPYC 9V74; Intel Xeon Platinum 8573C |
| Statements received by the database for 5000 inserted rows | MySQL: 5038, including 5000 inserts; MariaDB: 2, including one multi-row insert; PostgreSQL: unavailable | one CI run, Intel Xeon Platinum 8370C for MySQL and MariaDB, AMD EPYC 7763 for PostgreSQL |

The plugin submits a batch to the driver, but the driver decides how many statements the server
receives. That explains the difference between MySQL and MariaDB in the last row. Statement counts
are repeatable; timings on shared CI machines fluctuate. [Benchmarking and stress testing](docs/benchmarking.md)
explains the measurements and which metrics can fail a build. Timings are reported but do not fail builds.

**Limits.** A read stores at most 5000 rows. If the result exceeds that limit, nothing is stored and
`last database error` explains why. A write statement binds at most 30 000 values (for example,
5000 rows of six columns); larger batches are split across statements. Other resource limits may
still reject excessively large requests.

### What is deliberately not here

- **No tick percentiles.** The script records the longest tick during each operation, with a 10 ms clock
  resolution. Measuring MSPT percentiles requires sampling the server's tick loop directly.
- **No PostgreSQL statement counts.** The available PostgreSQL statistics count rows and transactions,
  but cannot attribute them to individual statements.

**MySQL batch inserts need attention.** With the tested Connector/J defaults, the driver sent one
insert per row. In a benchmark of twenty 5000-row inserts, MySQL took 17.04 seconds, compared with
1.99 seconds for PostgreSQL and MariaDB. Enabling batch rewriting can improve throughput, but the
driver then stops reporting a usable affected-row count. See [Troubleshooting](docs/troubleshooting.md#insert-many-got-faster-and-affected-rows-stopped-storing).

## Documentation

| Page | What is in it |
| --- | --- |
| [Getting started](docs/getting-started.md) | The shortest path from an empty script to a stored row. |
| [Connections](docs/connections.md) | Connection properties, named connections, switching, disconnecting. |
| [Tables](docs/tables.md) | Column syntax, every type, keys and modifiers, and what registering does not do. |
| [Raw statements](docs/raw-statements.md) | Statements you write yourself: what they skip, what they still owe, and why they are unsafe. |
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
