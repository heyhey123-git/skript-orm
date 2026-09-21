# skript-orm documentation

[简体中文](README.zh-CN.md) | **English**

Every page is available in English (`name.md`) and Chinese (`name.zh-CN.md`). The
[same list is in the README](../README.md).

## If you are new

Start with [Getting started](getting-started.md) to write a script that stores and retrieves a row.
Use the remaining pages as a reference when you need them.

## Reference

| Page | Contents |
| --- | --- |
| [Connections](connections.md) | Connecting, named connections, switching, and disconnecting. |
| [Tables](tables.md) | Column syntax, types, keys, modifiers, and the limits of table registration. |
| [Writing rows](writing.md) | Single and bulk inserts, inserting from variables, upsert, and `values` blocks. |
| [Reading rows](reading.md) | Single-row, multi-row, paginated, and primary-key queries; `where` blocks and result structure. |
| [Updating and deleting](updating-and-deleting.md) | Updates and deletes by condition or primary key, and row limits. |
| [Affected rows](affected-rows.md) | The `store affected rows` clause and conditional writes without transactions. |
| [Errors and waiting](errors-and-waiting.md) | Waiting for operations, `last database error`, and failure handling. |
| [Transactions](transactions.md) | Committing and rolling back groups of statements, failure handling, and timeouts. |
| [Types](types.md) | Accepted values and storage formats for each column type. |

## When something goes wrong

| Page | Contents |
| --- | --- |
| [Troubleshooting](troubleshooting.md) | Common issues with schema changes, NULL columns, and NBT without SkBee. |
| [Cookbook](cookbook.md) | Practical examples: upsert, pagination, bulk inserts, item NBT, and read-modify-write. |
| [Compatibility](compatibility.md) | Versions, connection type names, jar contents, and unsupported features. |

## For the wiki on GitHub

The wiki publishes both languages, with Chinese first. When documentation or publishing files change,
`.github/workflows/wiki.yml` runs `scripts/publish-wiki.ps1` to generate the pages. Edit the source files
in this repository; direct wiki edits are overwritten on the next sync.

`scripts/wiki-pages.tsv` maps source files to page names, and `scripts/wiki-sidebar.md` defines the sidebar.
Chinese pages retain their existing addresses; English page names have an `-EN` suffix.
