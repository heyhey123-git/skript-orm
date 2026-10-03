# skript-orm documentation

[简体中文](README.zh-CN.md) | **English**

Each page has an English (`name.md`) and Simplified Chinese (`name.zh-CN.md`) version.
The [project README](../README.md) also links to these guides.

## Getting started

Start with [Getting started](getting-started.md) for a script that writes and reads a row. Use the other pages when you need a specific operation or detail.

## Reference

| Page | Contents |
| --- | --- |
| [Connections](connections.md) | Connecting, named connections, switching, and disconnecting. |
| [Tables](tables.md) | Column syntax, types, keys, modifiers, and the limits of table registration. |
| [Raw statements](raw-statements.md) | Send SQL or MongoDB commands directly; understand parameters and skipped checks. |
| [Writing rows](writing.md) | Single and bulk inserts, inserting from variables, upsert, and `values` blocks. |
| [Reading rows](reading.md) | Single-row, multi-row, paginated, and primary-key queries; `where` blocks and result structure. |
| [Updating and deleting](updating-and-deleting.md) | Updates and deletes by condition or primary key, and row limits. |
| [Affected rows](affected-rows.md) | The `store affected rows` clause and conditional writes without transactions. |
| [Errors and waiting](errors-and-waiting.md) | Waiting for operations, `last database error`, and failure handling. |
| [Transactions](transactions.md) | Committing and rolling back groups of statements, failure handling, and timeouts. |
| [Types](types.md) | Accepted values and storage formats for each column type. |

## Troubleshooting and examples

| Page | Contents |
| --- | --- |
| [Troubleshooting](troubleshooting.md) | Common issues with schema changes, NULL columns, and NBT without SkBee. |
| [Examples](cookbook.md) | Upsert, pagination, bulk inserts, item NBT, and read-modify-write. |
| [Compatibility](compatibility.md) | Versions, connection type names, jar contents, and unsupported features. |

## GitHub wiki

The GitHub wiki publishes both languages, with Chinese pages listed first. Changes to documentation or
publishing files trigger `.github/workflows/wiki.yml`, which runs `scripts/publish-wiki.ps1`.
Edit the source files in this repository; the next sync overwrites edits made directly in the wiki.

`scripts/wiki-pages.tsv` maps source files to page names, and `scripts/wiki-sidebar.md` defines the sidebar.
Chinese pages retain their existing addresses; English page names have an `-EN` suffix.
