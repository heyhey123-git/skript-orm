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
| **SkBee** | Optional. Only `nbtcompound` columns need it. |

## The type names a script can write

`create a connection to database "..."` takes the type name of an implementation, matched exactly and
case-sensitively, and the released jar registers three:

| Type name | What it is |
| --- | --- |
| `"MySQL"` | MySQL's dialect, with the MySQL driver found for the server. The product and the type name happen to be the same word here, which is the only reason the two lists on this page can be confused. |
| `"PostgreSQL"` | PostgreSQL's dialect, with the driver the plugin downloads for it. See [What is inside the jar](#what-is-inside-the-jar). |
| `"JDBC"` | A driver you name yourself in a `driver` property, and a dialect that writes portable SQL (`"double quoted"` identifiers, `LIMIT 1` for one row, `LIMIT ? OFFSET ?` for paging), refusing whatever has no portable form. |

Any other name is refused when the section runs, with `Database '<name>' is not supported.` There is no
type called `"MariaDB"`, `"MongoDB"` or `"SQLite"`; those are products, and the table under "Database
products" is about them.

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and every implementation
module of this repository — the MySQL dialect and the generic JDBC one, and the PostgreSQL module. It
carries **no JDBC driver at all**, and that is deliberate:

- MySQL and the generic type use the driver the server already has. Paper ships MySQL Connector/J and
  makes it visible to plugins, so there is nothing to install for it, and `"JDBC"` is for a driver the
  server carries that this jar knows nothing about.
- PostgreSQL's driver is declared in `plugin.yml` as a `libraries` entry and downloaded by Paper once, on
  the first start, into the server's `libraries/` directory, where it is loaded onto the plugin's
  classpath. The source is the server's mirror of Maven Central: `PAPER_DEFAULT_CENTRAL_REPOSITORY`, or the
  `org.bukkit.plugin.java.LibraryLoader.centralURL` system property, or Google's mirror of Central by
  default. After that first start the driver is in `libraries/` and is used from there, so the download
  happens once per server.

That entry is a promise: Paper treats a library it cannot resolve as fatal, so a server that cannot reach
the mirror does not load this plugin at all. The way out is the mirror setting above — worth knowing that
it applies to every plugin's downloads on that server, not only this one — or putting the directory in
place by hand, where copying the whole `libraries/` directory from a server that has started once is the
reliable form of that.

This is also why no driver is shaded into the jar. A rewritten copy and a downloaded one would be two
drivers, and the downloaded one is the library its authors published: it keeps its own service files, its
version can be bumped in one place, and Paper checks its checksum.

The MongoDB implementation is in the repository but not in the jar, is not covered by CI, and is not
documented here. `-PbundleModules=` builds a jar for any combination of the modules, including modules
that are not released yet, which is a developer concern rather than a supported configuration.

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
are the three under "The type names a script can write".

| | |
| --- | --- |
| MySQL | Supported. Write `"MySQL"`. Tested against MySQL 8 in CI. |
| MariaDB | Untested. Write `"MySQL"` — the statement shapes are MySQL's (`INSERT IGNORE`, `ON DUPLICATE KEY UPDATE`, `LIMIT` on updates and deletes) — and it may well work, but nothing checks it. |
| PostgreSQL | Supported. Write `"PostgreSQL"`; the driver is downloaded on the first start. Tested in CI against a real server, and the addon is driven against it on a real Paper server as well. |
| MongoDB | Not supported in this release. The implementation exists in the repository, not in the jar. |
| SQLite and others | SQLite works through `"JDBC"` and the driver Paper already carries: nothing the dialect writes is foreign to it, and the server test runs a round trip of inserts, reads, paging, an update and a delete against it in both of its modes. Everything that dialect refuses stays refused — no auto increment, no `insert ... if absent`, no `upsert`, no limit on a write — and the others need a driver the server does not carry. |

## Behaviours that depend on the implementation

The syntax is the same everywhere, but a few things are the implementation's decision and the
documentation says so where it matters: conflict handling in `upsert` and `insert ... if absent`, whether
a limit can be applied to an update or a delete, and what "an absent row" means. The pages name MySQL's
behaviour explicitly rather than implying it is universal.
