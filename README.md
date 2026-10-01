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

## Performance and limits

Plainly: ordinary reads and writes do not lag a server, and a request too large to store is refused rather
than run. Refusing is protection, not a defect of the addon — players notice a stutter, not an error message.

Every number below was measured on the machine named beside it, one server at a time, and where it is written
as a range the spread was real. A number without its machine is not a number: two runs of the same test on the
same CI label can land on different processors.

| What was measured | Result | Machine |
| --- | --- | --- |
| Writing 5000 rows, the size both limits land on | 70 to 90 ms past a tick cold; warm, about 30 ms in one run and no overhang at all in another | disposable Paper 26.2 server, AMD Ryzen 5 5600X, JDK 25 |
| Writing 100 rows, and 10 000 rows | about 10 ms, and 100 to 110 ms, past a tick | the same server |
| Reading 100 to 5000 rows | no read lengthened a tick, and every row asked for was stored | the same server |
| Reading 10 000 rows | refused: the result variable is cleared and `last database error` names the ceiling | the same server |
| A 5000-row `insert many` through the generic JDBC path | 9.960 ± 0.384 ms, and 6.1 to 10.0 ms across three CI hosts | Ryzen 5 5600X; AMD EPYC 9V74; Intel Xeon Platinum 8573C |
| What a real server received for those 5000 rows | MySQL 5038 statements, one per row; MariaDB 2, one multi-row insert; PostgreSQL cannot count them | one CI run, Intel Xeon Platinum 8370C for MySQL and MariaDB, AMD EPYC 7763 for PostgreSQL |

The last row is the driver's doing rather than the plugin's, on the same SQL and the same server. What the
plugin sends and what the server receives are different questions, which is why this project counts both and
trusts the count: wall clock moves by ten to twenty percent between identical runs on a shared machine, and a
count does not. [Benchmarking and stress testing](docs/benchmarking.md) records how each number was taken and
that policy — **counts and allocations are findings that may gate a build, while timing is reported and never
gates it**. Nothing here is a promise about your machine.

**The limits, in the same plain terms.** A read stores at most 5000 rows; past that nothing is stored and
`last database error` says so. A write binds at most 30 000 values, which is 5000 rows of six columns, and a
batch past that is not refused: it is sent as several statements and every row is written. Absurdly large
requests are refused rather than run, and that is deliberate — a server that stutters is what players notice.

### What is deliberately not here

- **No tick percentiles.** The observer is script-side: it can report the tick a statement ran on and the
  longest tick in that window, but its clock resolves to 10 milliseconds, it is quantized by the tick a
  parked trigger resumes on, and its floor is one tick. Percentiles over tick durations need an observer
  inside the tick loop, which is a plugin in the server rather than a script, so MSPT percentiles are absent
  rather than invented.
- **No PostgreSQL statement counts.** Where a database itself counts the statements it received is where the
  difference between drivers lives. The MySQL family reports them, and does so in the rows above; PostgreSQL's
  own views count rows and transactions and cannot attribute either to statements, so that half is
  unavailable rather than guessed.

**One trap worth meeting here instead of in production.** With Connector/J's default settings a batched
insert leaves as one statement per row, so the same insert is much slower there than on PostgreSQL or MariaDB
with the same SQL. The `insert many` benchmark — 5000 rows of two columns, twenty times, the same script and
table definition on every backend — measured 17.04 seconds against MySQL and 1.99 seconds against PostgreSQL
and MariaDB, and the 5000 statements per batch came from the driver rather than from this plugin. The option
that rewrites the batch is not free either: it makes the insert faster and takes the affected-row count away.
[Troubleshooting](docs/troubleshooting.md#insert-many-got-faster-and-affected-rows-stopped-storing) has the
details.

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

