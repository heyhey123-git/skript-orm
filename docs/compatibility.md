# Compatibility

[简体中文](compatibility.zh-CN.md) | **English**

## Versions

| | |
| --- | --- |
| **Paper** | 26.2 or newer. The plugin declares `api-version: '26.2'` and is compiled against that server line. |
| **Skript** | 2.16.2 or newer. With an older version, the plugin logs the reason and disables itself. |
| **Java** | The version required by Paper 26.2. The plugin targets the same version. |
| **MySQL** | Supported through `"MySQL"`, with the MySQL driver the server already has. Tested against MySQL 8 in CI. |
| **MariaDB** | Supported through `"MariaDB"`, which downloads MariaDB Connector/J on the first start. Tested against MariaDB 11 in CI. |
| **PostgreSQL** | Supported through `"PostgreSQL"`. CI tests the implementation and runs the addon against a PostgreSQL server. |
| **MongoDB** | Supported through `"MongoDB"`, and tested against MongoDB 8 in CI. Its driver is downloaded on the first start, the same way PostgreSQL's is. |
| **SkBee** | Optional. Only `nbtcompound` columns need it. |

## The type names a script can write

`create a connection to database "..."` takes one of five implementation names. Names are case-sensitive:

| Connection type | Driver and behavior |
| --- | --- |
| `"MySQL"` | MySQL SQL syntax, using the server's existing MySQL driver. |
| `"MariaDB"` | MySQL-family SQL syntax with the downloaded MariaDB driver. Use a `jdbc:mariadb://` URL. |
| `"PostgreSQL"` | PostgreSQL-specific SQL syntax, with a driver downloaded by the plugin. See [What is inside the jar](#what-is-inside-the-jar). |
| `"MongoDB"` | Access through the downloaded blocking MongoDB Java driver, without SQL. Transaction and affected-row behavior differs from SQL backends; see [MongoDB](#mongodb). |
| `"JDBC"` | A driver specified in the `driver` property. It uses general SQL syntax and rejects operations that need database-specific SQL. |

Any other name is rejected when the section runs, with `Database '<name>' is not supported.`
SQLite is available through `"JDBC"`; `"SQLite"` is not a registered type name.

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and all implementation
modules in this repository: the JDBC module that registers MySQL, MariaDB and the generic type,
PostgreSQL, and MongoDB.
**Database drivers are not bundled.** They are provided as follows:

- MySQL uses Paper's MySQL Connector/J. For `"JDBC"`, the driver specified in the script must already
  be on the server's Java classpath, meaning its library must be available when the server starts.
  Paper also includes SQLite's driver.
- MariaDB, PostgreSQL and MongoDB have their drivers declared in `plugin.yml` as `libraries` entries and
  downloaded by Paper once, on the first start, into the server's `libraries/` directory, where they become
  available to the plugin. The list is generated from the bundled modules. The default build
  includes `org.mariadb.jdbc:mariadb-java-client:3.4.4`,
  `org.postgresql:postgresql:42.7.11` and
  `org.mongodb:mongodb-driver-sync:5.6.1`, and a build of modules that need no driver writes
  `libraries: []`. Paper downloads these drivers from a server that mirrors Maven Central. The download
  server can be changed with `PAPER_DEFAULT_CENTRAL_REPOSITORY` or the
  `org.bukkit.plugin.java.LibraryLoader.centralURL` system property. By default, Paper uses Google's
  mirror of Central. Downloaded drivers are reused from `libraries/` on later starts.

Paper treats an unresolved library as a fatal error and will not load the plugin. If the server cannot
reach that download server, change one of those settings. The chosen server affects library downloads
for **all plugins** on that server. Alternatively, prepare the dependencies manually by copying the entire `libraries/`
directory from a server that has already started successfully.

Drivers are kept outside the jar so Paper can load their published versions with their service files
and verify their checksums. MongoDB uses the synchronous `mongodb-driver-sync` rather than its Kotlin
coroutine driver, since the plugin relocates its own kotlinx.coroutines runtime.

For development builds, `-PbundleModules=` selects which modules to package. The generated `libraries`
list then includes only the drivers required by the selected modules. Custom module combinations are not officially supported.

## NBT compounds and SkBee

An NBT compound is a group of named NBT values. `nbtcompound` is the one column type whose implementation needs another plugin. SkBee is the only one
supported, for two reasons:

- SkBee is what gives scripts a way to build a compound (`nbt compound from "{...}"`), so a compound in a
  script is always one of SkBee's objects.
- SkBee bundles the NBT library under its own package rather than providing the standalone NBT API's
  classes, so the two cannot be mixed.

The plugin has no compile-time or mandatory runtime dependency on an NBT implementation.
During startup, it looks up and links SkBee's classes by name. As a result:

- **The standalone NBT API plugin cannot replace SkBee.** It does not conflict with this plugin.
- **No SkBee version is pinned.** The plugin looks for `NBTCompound`, the `NBTContainer(String)` and
  `NBTContainer(InputStream)` constructors, and the static method `NBTReflectionUtil.writeApiNBT` under
  `com.shanebeestudios.skbee.api.nbt`. If SkBee moves or changes these APIs, NBT columns become unavailable
  and the plugin reports the cause.
- Without SkBee, tables containing `nbtcompound` columns cannot be registered. Other features still work.
  See [Types](types.md).

`softdepend: [SkBee]` in the plugin description makes SkBee load first. It is a load-order hint, not a
requirement: the plugin enables with or without it.

## Database products

This table lists supported database products. Use the corresponding type name from
[The type names a script can write](#the-type-names-a-script-can-write) in a connection declaration.

| | |
| --- | --- |
| MySQL | Supported. Write `"MySQL"`. Tested against MySQL 8 in CI. |
| MariaDB | Supported. Write `"MariaDB"`; the driver is downloaded on the first start. The addon generates MySQL-family SQL that MariaDB also accepts, such as `ON DUPLICATE KEY UPDATE` and `LIMIT` on updates and deletes. Tested in CI against MariaDB 11. |
| PostgreSQL | Supported. Write `"PostgreSQL"`; the driver is downloaded on first startup. CI tests it with PostgreSQL and Paper. |
| MongoDB | Supported. Write `"MongoDB"`; the driver is downloaded on the first start. Its transactions are not implemented here, so a `database transaction` section reports the refusal. Tested in CI against MongoDB 8. |
| SQLite and others | SQLite works through `"JDBC"` and Paper's included driver. Inserts, reads, paging, updates, and deletes are tested. The generic SQL generator does not support auto increment, `insert ... if absent`, `upsert`, or write limits. Other products need a driver available on the server's Java classpath. |

## MongoDB

Like `"MySQL"`, `"MongoDB"` is both a product name and a connection type name. Scripts still use
`register a database table`, `insert`, `select`, `update`, and `delete` with a `"MongoDB"` connection.
The MongoDB implementation differs from SQL backends in the following ways.

- **All declared column types are supported.** UUIDs, item stacks, locations, Bukkit-serializable
  values, and NBT compounds are stored as binary fields in MongoDB documents; dates, times, and timespans use numbers.
- **Auto increment is a counter document**, one per auto-increment key, kept in a collection named
  `skript_orm_sequences`. That name is therefore reserved: a table may not be called that. The counter is
  created when the table is registered, and raised to the highest key the collection already holds, so a
  key that was stored before the table was registered is never handed out a second time.
- **Explicit keys also advance the counter.** Inserts and `upsert by id` with a supplied key ensure that
  later generated keys exceed it. Scripts can therefore mix explicit and generated keys. Documents
  written directly by other programs after registration can still cause a collision; the insert then
  fails with a duplicate-key error rather than overwriting an existing document.
- **`_id` is MongoDB's own field**, reserved for the identity of the document itself, so a table may not
  declare a column named `_id`. Registering one is refused with an explanation rather than failing later.
- **`select page` requires a primary key** and orders by it. MongoDB's natural document order is not
  stable enough for pagination.
- **`update`, `update by id` and `upsert by id` report what the filter matched**, not what the server
  changed. Writing a column the value it already holds still counts that row.
- **`insert if absent` cannot overwrite an existing key.** MongoDB checks its unique index as part of
  the insert, so concurrent attempts cannot both create a document with the same key. If the key already
  exists, the addon catches the duplicate-key error and leaves the existing document unchanged.
- **`delete ... with limit n` really deletes at most n rows.** MongoDB has no delete limit, so the plugin
  selects that many identifiers first and deletes those.
- **A filter comparing against null follows MongoDB.** `column = null` matches a row where the column is
  null or absent, and `column != null` matches one where it is present and not null.
- **Nullability and size are declarations, not server-enforced constraints.** Registration creates the
  primary-key unique index and the auto-increment counter. MongoDB creates collections as needed;
  creating an index can create a collection before its first document is written.
- **Transactions are not implemented.** `database transaction` reports
  `This database implementation does not support transactions.` MongoDB supports multi-document
  transactions on replica sets and sharded clusters, but this addon does not expose them. See
  [Connections](connections.md#mongodb-properties).

## Behaviours that depend on the implementation

Conflict handling in `upsert` and `insert ... if absent`, write limits, and missing-row behavior depend
on the connection type. MySQL and MariaDB share SQL syntax such as `ON DUPLICATE KEY UPDATE` and
`LIMIT` on updates and deletes, but use different drivers and `insert many` execution paths.
MongoDB behavior is described in the [MongoDB section](#mongodb).
