# Compatibility

[简体中文](compatibility.zh-CN.md) | **English**

## Versions

| | |
| --- | --- |
| **Paper** | 26.2 or newer. The plugin declares `api-version: '26.2'` and is compiled against that server line. |
| **Skript** | 2.16.2 or newer. Below that the plugin logs why and disables itself, so an old Skript shows up as a plugin that is present but not enabled. |
| **Java** | Whatever the server runs; the plugin is built for the Java version Paper 26.2 uses. |
| **MySQL** | The shipped implementation targets MySQL, and is tested against MySQL 8 in CI. MariaDB and other MySQL-compatible servers have not been tested. |
| **PostgreSQL** | Supported through `"PostgreSQL"`, and tested against PostgreSQL in CI, both the implementation and a full run of the addon on a real server. |
| **MongoDB** | Supported through `"MongoDB"`, and tested against MongoDB 8 in CI. Its driver is downloaded on the first start, the same way PostgreSQL's is. |
| **SkBee** | Optional. Only `nbtcompound` columns need it. |

## The type names a script can write

`create a connection to database "..."` takes the type name of an implementation, matched exactly and
case-sensitively, and the released jar registers four:

| Type name | What it is |
| --- | --- |
| `"MySQL"` | MySQL's dialect with the server's existing MySQL driver. The product and type share a name, but serve different purposes in this documentation. |
| `"PostgreSQL"` | PostgreSQL's dialect, with the driver the plugin downloads for it. See [What is inside the jar](#what-is-inside-the-jar). |
| `"MongoDB"` | Access through the downloaded blocking MongoDB Java driver, without SQL. Some statements behave differently; see [MongoDB](#mongodb). |
| `"JDBC"` | A driver you name yourself in a `driver` property, and a dialect that writes portable SQL (`"double quoted"` identifiers, `LIMIT 1` for one row, `LIMIT ? OFFSET ?` for paging), refusing whatever has no portable form. |

Any other name is refused when the section runs, with `Database '<name>' is not supported.` There is no
type called `"MariaDB"` or `"SQLite"`; those are products, and the table under "Database products" is
about them. `"MongoDB"` is both, the way `"MySQL"` is.

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and all implementation
modules in this repository: MySQL, generic JDBC, PostgreSQL, and MongoDB.
**Database drivers are not bundled.** They are provided as follows:

- MySQL and the generic type use the driver the server already has. Paper ships MySQL Connector/J and
  makes it visible to plugins, so there is nothing to install for it, and `"JDBC"` is for a driver the
  server carries that this jar knows nothing about.
- PostgreSQL and MongoDB have their drivers declared in `plugin.yml` as `libraries` entries and
  downloaded by Paper once, on the first start, into the server's `libraries/` directory, where they are
  loaded onto the plugin's classpath. That list is generated from the modules the jar bundles: in the
  default build it is `org.postgresql:postgresql:42.7.11` and
  `org.mongodb:mongodb-driver-sync:5.6.1`, and a build of modules that need no driver writes
  `libraries: []`. The source is the server's mirror of Maven Central:
  `PAPER_DEFAULT_CENTRAL_REPOSITORY`, or the `org.bukkit.plugin.java.LibraryLoader.centralURL` system
  property, or Google's mirror of Central by default. After that first start the drivers are in
  `libraries/` and are used from there, so the download happens once per server.

Paper treats an unresolved library as a fatal error and will not load the plugin. If the server cannot
reach the mirror, change the setting above; note that it affects library downloads for **all plugins**
on that server. Alternatively, prepare the dependencies manually by copying the entire `libraries/`
directory from a server that has already started successfully.

This is also why no driver is shaded into the jar. A rewritten copy and a downloaded one would be two
drivers, and the downloaded one is the library its authors published: it keeps its own service files, its
version can be bumped in one place, and Paper checks its checksum. The MongoDB driver the plugin uses is
the blocking one, `mongodb-driver-sync`, rather than the Kotlin coroutine driver: this jar shades and
relocates its own kotlinx.coroutines, so a driver that expected the unrelocated one would be a second
copy of the same runtime.

`-PbundleModules=` builds a jar for any combination of the modules, including modules that are not
released yet, which is a developer concern rather than a supported configuration. The `libraries` list
follows the modules that combination bundles, so each jar asks only for the drivers it uses.

## NBT compounds and SkBee

`nbtcompound` is the one column type whose implementation lives in another plugin. SkBee is the only one
supported, for two reasons:

- SkBee is what gives scripts a way to build a compound (`nbt compound from "{...}"`), so a compound in a
  script is always one of SkBee's objects.
- SkBee bundles the NBT library under its own package rather than providing the standalone NBT API's
  classes, so the two cannot be mixed.

The plugin has no compile-time or mandatory runtime dependency on an NBT implementation.
During startup, it looks up and links SkBee's classes by name. As a result:

- **The standalone NBT API plugin is not supported**, and installing it neither helps nor conflicts.
- **No SkBee version is pinned, but NBT is linked against a fixed set of names.** A version that still
  has `NBTCompound`, a `NBTContainer(String)` and `NBTContainer(InputStream)` constructor, and the static
  `NBTReflectionUtil.writeApiNBT`, under `com.shanebeestudios.skbee.api.nbt`, works; a version that moved
  or reshaped any of them makes NBT unavailable, which is reported rather than crashing.
- **A server without SkBee is fully usable**, except for NBT columns: registering a table that declares
  one is refused with a message naming SkBee. See [Types](types.md).

`softdepend: [SkBee]` in the plugin description makes SkBee load first. It is a load-order hint, not a
requirement: the plugin enables with or without it.

## Database products

This table lists database products a connection can access, rather than accepted type names.
Use one of the four types listed under "The type names a script can write", even when the product
and type happen to share a name.

| | |
| --- | --- |
| MySQL | Supported. Write `"MySQL"`. Tested against MySQL 8 in CI. |
| MariaDB | Untested. Try `"MySQL"`, which generates MySQL syntax such as `ON DUPLICATE KEY UPDATE` and `LIMIT` on updates and deletes. It may work, but this has not been verified. |
| PostgreSQL | Supported. Write `"PostgreSQL"`; the driver is downloaded on the first start. Tested in CI against a real server, and the addon is driven against it on a real Paper server as well. |
| MongoDB | Supported. Write `"MongoDB"`; the driver is downloaded on the first start. Its transactions are not implemented here, so a `database transaction` section reports the refusal. Tested in CI against MongoDB 8. |
| SQLite and others | SQLite works through `"JDBC"` and Paper's included driver. It accepts the SQL this dialect generates; server tests cover inserts, reads, paging, updates and deletes in both test modes. The dialect still excludes auto increment, `insert ... if absent`, `upsert`, and write limits. Other products require a driver not included with the server. |

## MongoDB

Like `"MySQL"`, `"MongoDB"` is both a product name and a type name. The syntax documented elsewhere
also applies, but some SQL-specific assumptions do not. The main differences are listed below.

- **Every declared column type works.** A uuid is stored as BSON binary, date, time and timespan as
  numbers, and itemstack, location, bukkit-serializable and nbt as BSON binary. No type is dropped for
  lack of a store.
- **Auto increment is a counter document**, one per auto-increment key, kept in a collection named
  `skript_orm_sequences`. That name is therefore reserved: a table may not be called that. The counter is
  created when the table is registered, and raised to the highest key the collection already holds, so a
  key that was stored before the table was registered is never handed out a second time.
- **Explicit keys also advance the counter.** Inserts with an explicit key and `upsert by id` update
  the counter so later generated keys exceed that value. This matches MySQL's `AUTO_INCREMENT` behavior;
  PostgreSQL sequences require a manual `setval` for the same effect. Mixing explicit and generated keys
  through the plugin therefore does not allocate a key twice. Documents written by other programs after
  registration can still cause a collision, in which case the insert fails with a duplicate-key error
  rather than overwriting an existing document.
- **`_id` is MongoDB's own field**, reserved for the identity of the document itself, so a table may not
  declare a column named `_id`. Registering one is refused with an explanation rather than failing later.
- **`select page` requires a primary key**, and always orders by it, exactly as the JDBC and PostgreSQL
  implementations do: MongoDB's natural order is not stable, so "page 2" means nothing without one.
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
- **Transactions are not implemented.** `supportsTransactions` stays false for this implementation, so a
  `database transaction` section fails with `This database implementation does not support transactions.` —
  the same wording any implementation without transactions reports. MongoDB itself has multi-document
  transactions on a replica set or a sharded cluster, and a standalone server refuses them; opening one from
  here is not done yet. See [Connections](connections.md#mongodb-properties).

## Behaviours that depend on the implementation

Shared syntax does not imply identical behavior. Conflict handling in `upsert` and `insert ... if absent`,
limits on updates and deletes, and the meaning of an absent row depend on the implementation.
The relevant pages identify MySQL-specific behavior rather than presenting it as universal.
MongoDB differences are collected under [MongoDB](#mongodb) above.
