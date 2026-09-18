# skript-orm documentation

[简体中文](README.zh-CN.md) | **English**

Every page here exists in both languages: `name.md` is English and `name.zh-CN.md` is Chinese. The
[same list is in the README](../README.md) if you came from there.

## If you are new

Read [Getting started](getting-started.md) first. It ends with a script that stores a row and reads it
back, and everything after that is reference for the parts you did not need yet.

## Reference

| Page | What is in it |
| --- | --- |
| [Connections](connections.md) | Connecting, named connections, switching, disconnecting. |
| [Tables](tables.md) | Column syntax, every type, keys and modifiers, and what registering does not do. |
| [Writing rows](writing.md) | Insert one, insert many, insert from a variable, upsert, and `values` blocks. |
| [Reading rows](reading.md) | Select one, many, page and by id, `where` blocks, and the shape of a result. |
| [Updating and deleting](updating-and-deleting.md) | Update and delete by condition or by id, and limits. |
| [Affected rows](affected-rows.md) | The `store affected rows` clause, and a conditional write without transactions. |
| [Errors and waiting](errors-and-waiting.md) | What waits, `last database error`, and what a failure does. |
| [Transactions](transactions.md) | All-or-nothing groups: how a transaction ends, what a failure does, timeouts. |
| [Types](types.md) | What each column type accepts and how it is stored. |

## When something goes wrong

| Page | What is in it |
| --- | --- |
| [Troubleshooting](troubleshooting.md) | Silence that is not success: schema changes that are ignored, NULL columns that vanish, NBT without SkBee. |
| [Cookbook](cookbook.md) | Whole recipes: upsert, paging, bulk insert, storing an item's NBT, read-modify-write. |
| [Compatibility](compatibility.md) | Versions, the type names a script can write, what ships inside the jar, and what is not supported. |

## For the wiki on GitHub

The Chinese pages are mirrored to the repository wiki as a read-only copy. `.github/workflows/wiki.yml`
renders them with `scripts/publish-wiki.ps1` after a change under `docs/`, so the repository is the one
to edit: whatever is on the wiki is replaced by the next run. Page names, and every link between pages,
come from `scripts/wiki-pages.tsv` (with the sidebar in `scripts/wiki-sidebar.md`), which sit beside
`scripts/publish-wiki.ps1`.
