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
| `"MySQL"` | MySQL's dialect, with the MySQL driver found for the server. The product and the type name happen to be the same word here, which is the only reason the two lists on this page can be confused. |
| `"PostgreSQL"` | PostgreSQL's dialect, with the driver the plugin downloads for it. See [What is inside the jar](#what-is-inside-the-jar). |
| `"MongoDB"` | MongoDB, reached through the blocking MongoDB Java driver the plugin downloads for it. There is no SQL under this one, so a few statements behave differently on purpose — see [MongoDB](#mongodb). |
| `"JDBC"` | A driver you name yourself in a `driver` property, and a dialect that writes portable SQL (`"double quoted"` identifiers, `LIMIT 1` for one row, `LIMIT ? OFFSET ?` for paging), refusing whatever has no portable form. |

Any other name is refused when the section runs, with `Database '<name>' is not supported.` There is no
type called `"MariaDB"` or `"SQLite"`; those are products, and the table under "Database products" is
about them. `"MongoDB"` is both, the way `"MySQL"` is.

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and every implementation
module of this repository — the MySQL dialect and the generic JDBC one, the PostgreSQL module, and the
MongoDB one. It carries **no database driver at all**, and that is deliberate:

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

That entry is a promise: Paper treats a library it cannot resolve as fatal, so a server that cannot reach
the mirror does not load this plugin at all. The way out is the mirror setting above — worth knowing that
it applies to every plugin's downloads on that server, not only this one — or putting the directory in
place by hand, where copying the whole `libraries/` directory from a server that has started once is the
reliable form of that.

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

The plugin therefore does not compile against, or depend on, any NBT implementation. It looks SkBee's
classes up by name when the first NBT value is handled and links them, which is why:

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

None of these names is a type name: this table is about what a connection can reach, and the type names
are the four under "The type names a script can write".

| | |
| --- | --- |
| MySQL | Supported. Write `"MySQL"`. Tested against MySQL 8 in CI. |
| MariaDB | Untested. Write `"MySQL"` — the statement shapes are MySQL's (`INSERT IGNORE`, `ON DUPLICATE KEY UPDATE`, `LIMIT` on updates and deletes) — and it may well work, but nothing checks it. |
| PostgreSQL | Supported. Write `"PostgreSQL"`; the driver is downloaded on the first start. Tested in CI against a real server, and the addon is driven against it on a real Paper server as well. |
| MongoDB | Supported. Write `"MongoDB"`; the driver is downloaded on the first start, and the implementation has no transactions. Tested in CI against MongoDB 8. |
| SQLite and others | SQLite works through `"JDBC"` and the driver Paper already carries: nothing the dialect writes is foreign to it, and the server test runs a round trip of inserts, reads, paging, an update and a delete against it in both of its modes. Everything that dialect refuses stays refused — no auto increment, no `insert ... if absent`, no `upsert`, no limit on a write — and the others need a driver the server does not carry. |

## MongoDB

`"MongoDB"` is a product name and a type name at once, the way `"MySQL"` is, and the syntax on the other
pages holds for it too. The SQL implementations' assumptions are what do not survive the move, so what
this implementation does with each statement is worth stating in one place.

- **Every declared column type works.** A uuid is stored as BSON binary, date, time and timespan as
  numbers, and itemstack, location, bukkit-serializable and nbt as BSON binary. No type is dropped for
  lack of a store.
- **Auto increment is a counter document**, one per auto-increment key, kept in a collection named
  `skript_orm_sequences`. That name is therefore reserved: a table may not be called that. The counter is
  created when the table is registered, and raised to the highest key the collection already holds, so a
  key that was stored before the table was registered is never handed out a second time.
- **A statement that brings its own key raises the counter past it.** An insert with an explicit key and an
  `upsert by id` both tell the counter what they wrote, which is what MySQL's `AUTO_INCREMENT` does with an
  explicit insert, and what a PostgreSQL sequence needs a manual `setval` for. A key is therefore never
  issued twice, whatever mix of explicit and generated keys a script uses. Only a document written into the
  collection by something that is not this plugin, after the table was registered, can still collide — and
  the insert then fails with the server's duplicate-key error rather than overwriting it.
- **`_id` is MongoDB's own field**, reserved for the identity of the document itself, so a table may not
  declare a column named `_id`. Registering one is refused with an explanation rather than failing later.
- **`select page` requires a primary key**, and always orders by it, exactly as the JDBC and PostgreSQL
  implementations do: MongoDB's natural order is not stable, so "page 2" means nothing without one.
- **`update`, `update by id` and `upsert by id` report what the filter matched**, not what the server
  changed. Writing a column the value it already holds still counts that row.
- **`insert if absent` is key-based and atomic.** A write a unique index already covers is ignored, the
  way SQL's `INSERT IGNORE` is, rather than reported as an error.
- **`delete ... with limit n` really deletes at most n rows.** MongoDB has no delete limit, so the plugin
  selects that many identifiers first and deletes those.
- **A filter comparing against null follows MongoDB.** `column = null` matches a row where the column is
  null or absent, and `column != null` matches one where it is present and not null.
- **Nothing else is enforced by the server.** Nullability and size are declarations, not constraints.
  Registration creates the unique index on the primary key and the auto-increment counter, and MongoDB
  creates the collection itself with the first document written.
- **No transactions.** `supportsTransactions` stays false for this implementation, so a `database
  transaction` section fails with `This database implementation does not support transactions.` — the
  same wording any implementation without transactions reports. See
  [Connections](connections.md#mongodb-properties).

## Behaviours that depend on the implementation

The syntax is the same everywhere, but a few things are the implementation's decision and the
documentation says so where it matters: conflict handling in `upsert` and `insert ... if absent`, whether
a limit can be applied to an update or a delete, and what "an absent row" means. The pages name MySQL's
behaviour explicitly rather than implying it is universal, and MongoDB's own answers are under
[MongoDB](#mongodb) above.
