# Compatibility

[简体中文](compatibility.zh-CN.md) | **English**

## Versions

| | |
| --- | --- |
| **Paper** | 26.2 or newer. The plugin declares `api-version: '26.2'` and is compiled against that server line. |
| **Skript** | 2.16.2 or newer. Below that the plugin logs why and disables itself, so an old Skript shows up as a plugin that is present but not enabled. |
| **Java** | Whatever the server runs; the plugin is built for the Java version Paper 26.2 uses. |
| **MySQL** | The shipped implementation targets MySQL, and is tested against MySQL 8 in CI. MariaDB and other MySQL-compatible servers have not been tested. |
| **SkBee** | Optional. Only `nbtcompound` columns need it. |

## The type names a script can write

`create a connection to database "..."` takes the type name of an implementation, matched exactly and
case-sensitively, and the released jar registers two:

| Type name | What it is |
| --- | --- |
| `"MySQL"` | MySQL's dialect, with the MySQL driver found for the server. The product and the type name happen to be the same word here, which is the only reason the two lists on this page can be confused. |
| `"JDBC"` | A driver you name yourself in a `driver` property, and a dialect that writes portable SQL (`"double quoted"` identifiers, `LIMIT 1` for one row, `LIMIT ? OFFSET ?` for paging), refusing whatever has no portable form. |

Any other name is refused when the section runs, with `Database '<name>' is not supported.` There is no
type called `"MariaDB"`, `"PostgreSQL"`, `"MongoDB"` or `"SQLite"`; those are products, and the table
under "Database products" is about them.

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and those two type names —
both of which are the same JDBC implementation, one adding MySQL's dialect and one taking a driver from
the script. It does **not** bundle a JDBC driver for MySQL, because the server already has one: Paper
ships MySQL Connector/J and makes it visible to plugins, so there is nothing to install alongside.
`"JDBC"` uses whatever driver the server carries for the database it points at, which is the only reason
to reach for it.

The repository also contains a PostgreSQL implementation and a MongoDB implementation. They are not part
of a default build, are not covered by CI, and are not documented here; a build can include them with
`-PbundleModules=`, which is a developer concern rather than a supported configuration — and they are
why this repository mentions more database types than the released jar registers.

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
are the two under "The type names a script can write".

| | |
| --- | --- |
| MySQL | Supported. Write `"MySQL"`. Tested against MySQL 8 in CI. |
| MariaDB | Untested. Write `"MySQL"` — the statement shapes are MySQL's (`INSERT IGNORE`, `ON DUPLICATE KEY UPDATE`, `LIMIT` on updates and deletes) — and it may well work, but nothing checks it. |
| PostgreSQL | Not supported in this release. The repository has an implementation; the released jar does not carry it. |
| MongoDB | Not supported in this release. The implementation exists in the repository, not in the jar. |
| SQLite and others | Not supported. Paper does ship SQLite's driver, and the `"JDBC"` dialect writes nothing SQLite objects to, but that driver does not implement the `setObject` overload this implementation binds its values with, so every statement carrying a value fails with `setObject not implemented`. The others need a driver the server does not carry. |

## Behaviours that depend on the implementation

The syntax is the same everywhere, but a few things are the implementation's decision and the
documentation says so where it matters: conflict handling in `upsert` and `insert ... if absent`, whether
a limit can be applied to an update or a delete, and what "an absent row" means. The pages name MySQL's
behaviour explicitly rather than implying it is universal.
