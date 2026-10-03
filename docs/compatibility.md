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

| Type name | What it is |
| --- | --- |
| `"MySQL"` | MySQL SQL syntax, using the server's existing MySQL driver. |
| `"MariaDB"` | The same SQL syntax, using the downloaded MariaDB driver. Use a `jdbc:mariadb://` URL. |
| `"PostgreSQL"` | PostgreSQL's dialect, with the driver the plugin downloads for it. See [What is inside the jar](#what-is-inside-the-jar). |
| `"MongoDB"` | Access through the downloaded blocking MongoDB Java driver, without SQL. Some statements behave differently; see [MongoDB](#mongodb). |
| `"JDBC"` | A driver specified in the `driver` property. It uses general SQL syntax and rejects operations without a supported form. |

Any other name is rejected when the section runs, with `Database '<name>' is not supported.`
SQLite is available through `"JDBC"`; `"SQLite"` is not a registered type name.

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and all implementation
modules in this repository: the JDBC module that registers MySQL, MariaDB and the generic type,
PostgreSQL, and MongoDB.
**Database drivers are not bundled.** They are provided as follows:

- MySQL uses Paper's MySQL Connector/J. For `"JDBC"`, the driver specified in the script must already
  be on the server's classpath. Paper also includes SQLite's driver.
- MariaDB, PostgreSQL and MongoDB have their drivers declared in `plugin.yml` as `libraries` entries and
  downloaded by Paper once, on the first start, into the server's `libraries/` directory, where they are
  loaded onto the plugin's classpath. The list is generated from the bundled modules. The default build
  includes `org.mariadb.jdbc:mariadb-java-client:3.4.4`,
  `org.postgresql:postgresql:42.7.11` and
  `org.mongodb:mongodb-driver-sync:5.6.1`, and a build of modules that need no driver writes
  `libraries: []`. The source is the server's mirror of Maven Central:
  `PAPER_DEFAULT_CENTRAL_REPOSITORY`, or the `org.bukkit.plugin.java.LibraryLoader.centralURL` system
  property. By default, Paper uses Google's mirror of Central. Downloaded drivers are reused from
  `libraries/` on later starts.

Paper treats an unresolved library as a fatal error and will not load the plugin. If the server cannot
reach the mirror, change the setting above; note that it affects library downloads for **all plugins**
on that server. Alternatively, prepare the dependencies manually by copying the entire `libraries/`
directory from a server that has already started successfully.

Drivers are kept outside the jar so Paper can load their published versions with their service files
and verify their checksums. MongoDB uses the synchronous `mongodb-driver-sync` rather than its Kotlin
coroutine driver, since the plugin relocates its own kotlinx.coroutines runtime.

For development builds, `-PbundleModules=` selects which modules to package. The generated `libraries`
list then includes only their required drivers. Custom module combinations are not supported releases.

## NBT compounds and SkBee

`nbtcompound` is the one column type whose implementation lives in another plugin. SkBee is the only one
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
| MariaDB | Supported. Write `"MariaDB"`; the driver is downloaded on the first start. The dialect generates the MySQL syntax MariaDB also accepts, such as `ON DUPLICATE KEY UPDATE` and `LIMIT` on updates and deletes. Tested in CI against MariaDB 11. |
| PostgreSQL | Supported. Write `"PostgreSQL"`; the driver is downloaded on first startup. CI tests it with PostgreSQL and Paper. |
| MongoDB | Supported. Write `"MongoDB"`; the driver is downloaded on the first start. Its transactions are not implemented here, so a `database transaction` section reports the refusal. Tested in CI against MongoDB 8. |
| SQLite and others | SQLite works through `"JDBC"` and Paper's included driver. Inserts, reads, paging, updates, and deletes are tested. The generic dialect does not support auto increment, `insert ... if absent`, `upsert`, or write limits. Other products need a driver available on the server's classpath. |

## MongoDB

Like `"MySQL"`, `"MongoDB"` is both a product name and a type name. The syntax documented elsewhere
also applies, but some SQL-specific assumptions do not. The main differences are listed below.

- **All declared column types are supported.** UUIDs, item stacks, locations, Bukkit-serializable
  values, and NBT compounds use BSON binary; dates, times, and timespans use numbers.
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
- **`insert if absent` is key-based and atomic.** A write a unique index already covers is ignored rather
  than reported as an error; on MongoDB that is a duplicate key the plugin catches, the same way the MySQL
  dialect catches one.
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

Some behavior depends on the implementation: conflict handling in `upsert` and `insert ... if absent`,
write limits, and how missing rows are handled. MySQL and MariaDB use the same SQL dialect, so MySQL
notes elsewhere in this guide apply to both. MongoDB differences are listed [above](#mongodb).
