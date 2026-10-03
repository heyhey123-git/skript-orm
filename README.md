# skript-orm

<!-- Use an absolute image URL because this page also serves as the wiki home. -->
![skript-orm: an ORM for Skript](https://raw.githubusercontent.com/heyhey123-git/skript-orm/master/docs/assets/banner.png)

**Store your Minecraft server's data with Skript.** Define a table once, then save player
records, rewards or logs using Skript syntax. You do not need to learn SQL to get started.

[简体中文](README.zh-CN.md) | **English**

[Download](https://github.com/heyhey123-git/skript-orm/releases) · [Getting started](docs/getting-started.md) · [Examples](docs/cookbook.md) · [Benchmarks](docs/benchmarking.md)

## Why use it?

- **Write scripts, not SQL strings.** Dedicated syntax for reading, inserting, updating and
  deleting, with results in normal Skript variables.
- **Store Minecraft values directly.** UUIDs, item stacks, locations, dates and timespans have
  column types. NBT compounds are supported with SkBee.
- **Keep player input separate from SQL.** Structured statements bind values as parameters
  and check column names and value types. A player's name or message is data, not part of a
  query. Handwritten raw statements need their own precautions.
- **Handle batches without a query per script row.** `insert many` reads its input in portions
  across ticks, then submits database work in the background. MySQL inserts use parameterized
  multi-row statements.
- **Know when a save finishes.** The current trigger resumes after the operation completes;
  the server can keep ticking while it waits. Check `last database error` before telling a
  player their data was saved.
- **Use the database that fits your server.** MySQL, MariaDB, PostgreSQL, SQLite and MongoDB
  are covered by real database and Paper tests. Named connections let one script use more
  than one database.

## Requirements

| Component | Requirement |
| --- | --- |
| Paper | 26.2 or newer; the plugin targets the 26.2 server series. |
| Skript | 2.16.2 or newer. |
| Java | 25 or newer. |
| SkBee | Optional; needed only for `nbtcompound` columns. |

### Choose a database

These are the database products tested by the project and the names to use in your connection
declaration. SQLite uses the general `"JDBC"` connection type.

| Database | Connection type | What you need |
| --- | --- | --- |
| MySQL | `"MySQL"` | A MySQL server and account; Paper supplies the driver. |
| MariaDB | `"MariaDB"` | A MariaDB server and account. |
| PostgreSQL | `"PostgreSQL"` | A PostgreSQL server and account. |
| SQLite | `"JDBC"` | A database file on your server; no separate database service. Paper supplies the driver. |
| MongoDB | `"MongoDB"` | A MongoDB server and account; the same structured read/write syntax is available. |

Paper downloads the MariaDB, PostgreSQL and MongoDB drivers on first startup. If a download
fails, see [Compatibility](docs/compatibility.md#what-is-inside-the-jar).
MongoDB transactions are not exposed by this addon; SQLite's general JDBC path also has
[some operation limits](docs/compatibility.md#database-products).

## Install

1. Download `skriptorm-<version>.jar` from [Releases](https://github.com/heyhey123-git/skript-orm/releases).
2. Put the jar in `plugins/` with Skript, then restart the server.
3. Add a script in `plugins/Skript/scripts/`, set your database credentials, and load it with
   `/sk reload <script>`.

Connections and tables are defined in your script. There is no separate addon configuration
file to edit. For a complete walkthrough, see [Getting started](docs/getting-started.md).

## Example: save and read a player record

This MySQL example keeps one row per player. Joining updates their name and latest join
date; `/myrecord` reads the saved data. Create the `minecraft` database and its account
before loading the script, and replace the example credentials.

```sk
on load:
    set {playerdb::ready} to false
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/minecraft"
        username: "minecraft"
        password: "change-me"
    if last database error is set:
        send "Database connection failed: %last database error%" to console
        stop

    register a database table "players":
        uuid: uuid, primary key, not null
        name: string(64), not null
        last_join: date, not null
    if last database error is set:
        send "Player table failed: %last database error%" to console
        stop
    set {playerdb::ready} to true

on join:
    if {playerdb::ready} is not true:
        stop
    upsert one entity in table "players" by id uuid of player and wait:
        values:
            name: name of player
            last_join: now
    if last database error is set:
        send "Could not save player data: %last database error%" to console

command /myrecord:
    executable by: players
    trigger:
        if {playerdb::ready} is not true:
            send "The database is not ready."
            stop
        select one entity from table "players" and store the result in {_row::*}:
            where all:
                uuid = uuid of player
        if last database error is set:
            send "Could not read your data. Please try again later."
            send "Player lookup failed: %last database error%" to console
            stop
        if {_row::uuid} is not set:
            send "No saved record yet."
            stop
        send "Saved name: %{_row::name}%"
        send "Latest join date: %{_row::last_join}%"
```

`by id` refers to the table's primary key: `uuid` here. `upsert` creates a missing row or
updates the existing one. A SQL `date` column stores the calendar date, without the time of day.

Keep connection creation and table registration in one script; other scripts can reuse them.
Registration creates a missing table but does not change an existing table's columns.
See [Tables](docs/tables.md) before changing a deployed table.

## Performance on a real server

The addon is designed for batch work: database operations run in the background, and large
input lists are read in portions across ticks. This helps avoid one long scan of a Skript
variable on the server thread. It does not guarantee zero tick delay: storing query results
and processing individual values still involve the server thread.

The following test saves or reads **5000 rows in one operation**. Keeping the row count fixed
shows how each supported database handled the same workload in this run. All writes finished
in under 750 ms and reads in under 105 ms.

| Database | Write: total time | Write: largest tick gap | Read: total time | Read: largest tick gap |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 735.828866 ms | 65.767643 ms | 103.186998 ms | 103.066121 ms |
| MySQL | 462.517747 ms | 50.395059 ms | 78.302884 ms | 78.218777 ms |
| MariaDB | 685.122311 ms | 66.066614 ms | 95.789059 ms | 95.813175 ms |
| PostgreSQL | 585.103286 ms | 66.926809 ms | 94.172914 ms | 94.082624 ms |
| MongoDB | 717.433104 ms | 50.968606 ms | 78.761947 ms | 78.579400 ms |

**How to read this table:**

- **Database** is the database used for that row.
- **Total time** is how long the trigger waits for the entire operation, including variable
  processing, database work and resuming the script. It is not continuous server-thread blocking.
- **Largest tick gap** is the longest interval between two executions of the tick observer,
  measured from the operation's start until one tick after it returns. A normal gap is about
  50 ms at 20 TPS. It indicates observed delay, not the plugin's CPU time.

For example, the MySQL write took `462.517747 ms`, but its largest observed tick gap was
`50.395059 ms`, close to the normal 50 ms interval. The read measurements also show why
total time and tick delay should be reported separately.

These are [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500)
measurements from 2026-10-03 at commit `6476c9b`: Paper 26.2 build 124, Skript 2.16.2,
Java 25.0.4.1, four visible CPUs, and input rows containing only an `id`.
SQLite, MariaDB and PostgreSQL ran on AMD EPYC 7763; MySQL and MongoDB ran on Intel Xeon
6973P-C. Shared runners and different CPUs prevent a general ranking of databases.
The run predates the MySQL packet-limit fix in `21898af`; it is not a measurement of that fix.

Timing uses `System.nanoTime()`; six-decimal milliseconds retain the recorded nanoseconds.
See [Benchmarking](docs/benchmarking.md) for the 100–10000-row cases, repeated runs,
raw SQL comparisons and full measurement limits.

### Practical limits

- A read returns at most **5000 rows**. A larger result is rejected rather than partly saved;
  use [pagination](docs/reading.md) for larger datasets.
- Large writes are split into database statements with at most **30000 bound values** each.
  Keep an `insert many` source variable unchanged until the operation finishes.
- SQL transactions can commit a group of writes together or roll them back on failure.
  Splitting a bulk write into statements does not by itself make it atomic; use a
  [transaction](docs/transactions.md) when all rows must succeed together.

## How does it compare with skript-db?

`skript-db` names several projects. The comparison below uses the
[btk5h original](https://github.com/btk5h/skript-db) and
[Limework fork](https://github.com/Limework/skript-db) as SQL-oriented examples.
Check the exact fork you install: features and compatibility differ, and the name alone does
not mean that a project is abandoned.
The [4w3 fork](https://hangar.papermc.io/4w3/skript-db) is maintained and also offers parameter
binding, batches and transactions; these features are not exclusive to skript-orm.

The comparison is about how you write and maintain a script. There is no same-server
performance benchmark against these addons, so the figures above do not establish a speed
advantage over them.

| What matters to your script | skript-orm | SQL-oriented skript-db addons |
| --- | --- | --- |
| Getting started | Declare columns, then use `insert`, `select`, `update` and `delete`. | Write SQL queries and manage the table definition in SQL. |
| Player input | Structured statements bind values and validate declared columns and value types. | The original and Limework projects provide SQL injection protection; safe use still depends on the query API and how the script supplies values. |
| Minecraft data | Built-in types for UUIDs, items and locations; NBT with SkBee. | Scripts need to arrange the database representation and conversion of these values. |
| Database choice | Structured operations for MySQL, MariaDB, PostgreSQL, SQLite and MongoDB, with documented differences. | The compared projects use JDBC for SQL databases; MongoDB is outside that API. |
| Bulk writing | `insert many`, input reading across ticks, and parameterized multi-row MySQL inserts. | Batch behavior depends on the fork, query construction and database driver. |
| Query control | Common tasks have dedicated syntax; handwritten statements are also available. | Direct SQL offers fine control over joins, aggregates and database-specific features. |

Choose skript-orm when you want database storage to fit the way you already write Skript,
especially when storing Minecraft values or importing lists of rows. A SQL addon can suit
scripts that already rely on complex SQL. The main advantage here is less query construction
and conversion code to maintain, with explicit errors and tested database behavior.

## Documentation

| You want to… | Read |
| --- | --- |
| Save your first row | [Getting started](docs/getting-started.md) |
| Connect a database or use several connections | [Connections](docs/connections.md) |
| Define columns and keys | [Tables](docs/tables.md) |
| Save one row, import a list, or update-or-insert | [Writing rows](docs/writing.md) |
| Filter results or read pages | [Reading rows](docs/reading.md) |
| Change or remove data | [Updating and deleting](docs/updating-and-deleting.md) |
| Check errors or affected-row counts | [Errors and waiting](docs/errors-and-waiting.md), [Affected rows](docs/affected-rows.md) |
| Keep several writes together | [Transactions](docs/transactions.md) |
| Store Minecraft values | [Types](docs/types.md) |
| Adapt a complete example | [Cookbook](docs/cookbook.md) |
| Solve a problem or check compatibility | [Troubleshooting](docs/troubleshooting.md), [Compatibility](docs/compatibility.md) |
| Understand the performance tests | [Benchmarking](docs/benchmarking.md) |
| See release changes | [Changelog](CHANGELOG.md) |

## Building and license

To build from source or run tests, see [CONTRIBUTION.md](CONTRIBUTION.md).
Released jars are built in `build/dist/`. MIT license; see [LICENSE](LICENSE).
