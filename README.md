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

This is a high-performance library, and its limits are deliberate. When a result is too large to store
without spending the server's own tick on it, the plugin refuses the read and says so rather than storing
part of it, because a server that stutters is what players notice. Refusing is protection, not a failure.

Every number below was measured on the machine and in the period it names, one server at a time, and where
it is written as a range the spread was real. [Benchmarking and stress testing](docs/benchmarking.md) records
how they were taken and the policy they follow: **counts and allocations are findings that may gate a build,
while timing is reported and never gates it**, because wall clock moves by ten to twenty percent between
identical runs on a shared machine and a count does not. Nothing here is a promise about another machine.

**Writing, on a real Paper server.** A statement is timed inside the script, on the tick the effect ran on,
so what is read is the overhang past a normal tick. On a six-column table that overhang grew with the batch:
about 10 milliseconds for 100 rows, and 100 to 110 milliseconds for 10 000. 5000 rows — the size both limits
land on — overran by 70 to 90 milliseconds cold; warm, on the JVM's second pass, one run overran by about
30 milliseconds while another kept the whole write inside a single tick and so reported no overhang at all.
Cold and warm are different measurements, and a cost under the 50 milliseconds a tick lasts has nothing to
lengthen.

**Reading.** Up to 5000 rows are stored, and no read in that range was seen to lengthen a tick. On the
benchmark server — the generic JDBC connection, which on the machine that ran it is SQLite — reads of 100,
500, 1000, 2500 and 5000 rows of a six-column table each stored every row they asked for, and in none of them
did the longest tick inside the statement exceed a tick's own length. A read of 10 000 rows stores nothing:
the result variable is cleared, and `last database error` names the ceiling and what to do about it. That
boundary is a measurement rather than a guess — a hundred thousand rows of a six-column table written into a
variable one index at a time is about a second of the server not ticking through.

**The 30 000-value budget is a splitting line, not a cliff.** One write binds at most 30 000 values, which is
5000 rows of six columns and the same batch a read is held to. A batch past it is not refused: it is sent as
several statements, and every row is written. The curve is flat there — a batch of 30 000 values was written
in one call with the count it reported matching what the table gained, and so was a batch of 60 000, 10 000
rows of six columns, with no step between the two.

**The driver, counted rather than timed.** Through the generic JDBC connection on an in-memory database, a
call of 5000 rows of six columns was handed to the driver as a **single statement**, the plugin reported
5000 affected rows with an exact count, the table held 5000 rows once the call returned, and the call
measured **9.960 ± 0.384 milliseconds**. That is the reference run in `benchmarks/baseline.json`.

### What is deliberately not here

- **No tick percentiles.** The observer is script-side: it can report the tick a statement ran on and the
  longest tick in that window, but its clock resolves to 10 milliseconds, it is quantized by the tick a
  parked trigger resumes on, and its floor is one tick. Percentiles over tick durations need an observer
  inside the tick loop, which is a plugin in the server rather than a script, so MSPT percentiles are absent
  rather than invented.
- **No per-backend statement counts on the server side.** The count a database itself receives is where the
  difference between drivers lives, and it needs a real instance of each backend. It has not been measured.

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

