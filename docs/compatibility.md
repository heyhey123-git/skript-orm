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

## What is inside the jar

The released jar bundles the plugin, its Kotlin runtime, the connection pool, and **one** database
implementation: the JDBC one, configured for MySQL. It does **not** bundle a JDBC driver, because the
server already has one: Paper ships MySQL Connector/J and makes it visible to plugins, so there is
nothing to install alongside.

The repository also contains a PostgreSQL implementation and a MongoDB implementation. They are not part
of a default build, are not covered by CI, and are not documented here; a build can include them with
`-PbundleModules=`, which is a developer concern rather than a supported configuration.

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
- **No SkBee version is pinned.** Any version that still exposes the NBT classes under
  `com.shanebeestudios.skbee.api.nbt` works; a version that moved them would make NBT unavailable, which
  is reported rather than crashing.
- **A server without SkBee is fully usable**, except for NBT columns: registering a table that declares
  one is refused with a message naming SkBee. See [Types](types.md).

`softdepend: [SkBee]` in the plugin description makes SkBee load first. It is a load-order hint, not a
requirement: the plugin enables with or without it.

## Databases

| | |
| --- | --- |
| MySQL | Supported, and the only implementation in the released jar. |
| MariaDB | Untested. The statement shapes are MySQL's (`INSERT IGNORE`, `ON DUPLICATE KEY UPDATE`, `LIMIT` on updates and deletes), so it may work, but nothing checks it. |
| PostgreSQL | Not supported in this release. |
| MongoDB | Not supported in this release. |
| SQLite and others | Not supported. |

## Behaviours that depend on the implementation

The syntax is the same everywhere, but a few things are the implementation's decision and the
documentation says so where it matters: conflict handling in `upsert` and `insert ... if absent`, whether
a limit can be applied to an update or a delete, and what "an absent row" means. The pages name MySQL's
behaviour explicitly rather than implying it is universal.
