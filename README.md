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

Database calls run in the background. Lists in local variables such as `{_rows::*}` are also processed there while the current script waits; large global inputs are read in portions across ticks. Conversions that use server APIs, including items and locations, share a 2 ms target budget per tick. A single slow conversion can exceed that target.

[Run #7](https://github.com/heyhey123-git/skript-orm/actions/runs/37205411783) read **5000 rows with six populated numeric columns** on each supported database. The table compares local and global result variables for the same workload. Every value is a median of ten samples after three warmups.

- **Database** identifies the backend used in that row.
- **Operation** identifies whether the same result is stored in local or global variables.
- **Total time** is the time from the read statement to script resumption, including background work and waiting.
- **Server-thread processing time** is elapsed time in the recorded server-thread processing intervals. It is neither total server CPU time nor the complete tick duration.

| Database | Operation | Total time | Server-thread processing time |
| --- | --- | ---: | ---: |
| SQLite | Read into local variables | 49.925225 ms | 0.018713 ms |
| SQLite | Read into global variables | 65.414039 ms | 15.483043 ms |
| MySQL | Read into local variables | 49.923762 ms | 0.015720 ms |
| MySQL | Read into global variables | 86.510248 ms | 36.381696 ms |
| MariaDB | Read into local variables | 50.005949 ms | 0.017637 ms |
| MariaDB | Read into global variables | 66.299079 ms | 15.888968 ms |
| PostgreSQL | Read into local variables | 49.942947 ms | 0.018167 ms |
| PostgreSQL | Read into global variables | 69.211086 ms | 19.242586 ms |
| MongoDB | Read into local variables | 49.950872 ms | 0.014090 ms |
| MongoDB | Read into global variables | 71.939851 ms | 21.958078 ms |

The local path keeps ordinary result storage off the server thread. Its complete read still takes around one tick because Skript resumes on the server thread. These are comparisons between variable scopes in the current implementation, not a before/after test or a benchmark against another addon.

The run tested commit `76dcc7e` on 2026-10-04 with Paper 26.2 build 124, Skript 2.16.2 and Java 25. Different jobs used different CPUs, so the table does not rank databases. Global object results can still take a long server tick to assign; use local results and smaller pages for large workloads.

Timing uses `System.nanoTime()`; six-decimal milliseconds retain the recorded nanoseconds. See [Benchmarking](docs/benchmarking.md#measured-results-run-7) for CPU models, write curves, ItemStack/Location tests, raw reports and measurement limits.

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
