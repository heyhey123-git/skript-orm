# skript-orm 文档

**简体中文** | [English](README.md)

每篇文档都有英文版（`name.md`）和简体中文版（`name.zh-CN.md`）。[项目 README](../README.zh-CN.md) 也有这些页面的链接。

## 入门

先看[快速上手](getting-started.zh-CN.md)，用一段脚本写入并读取数据。需要具体操作或细节时，再查阅其他页面。

## 参考

| 页面 | 内容 |
| --- | --- |
| [连接](connections.zh-CN.md) | 建立连接、具名连接、切换与断开连接。 |
| [表](tables.zh-CN.md) | 列语法、类型、主键与修饰符，以及注册表的限制。 |
| [原始语句](raw-statements.zh-CN.md) | 直接发送 SQL 或 MongoDB 命令，了解参数写法和不适用的检查。 |
| [写入行](writing.zh-CN.md) | 插入一行或多行、从变量插入、upsert，以及 `values` 块的写法。 |
| [读取行](reading.zh-CN.md) | 单行、多行、分页与按 id 查询，`where` 块及结果结构。 |
| [更新与删除](updating-and-deleting.zh-CN.md) | 按条件或 ID 更新、删除，以及限制操作行数。 |
| [影响行数](affected-rows.zh-CN.md) | `store affected rows` 子句，以及不使用事务的条件写入。 |
| [错误与等待](errors-and-waiting.zh-CN.md) | 等待机制、`last database error` 及失败后的行为。 |
| [事务](transactions.zh-CN.md) | 一组语句的提交与回滚、失败处理和超时。 |
| [类型](types.zh-CN.md) | 每种列类型接受的值与存储方式。 |

## 故障排查与示例

| 页面 | 内容 |
| --- | --- |
| [故障排查](troubleshooting.zh-CN.md) | 常见问题：改表未生效、NULL 不显示、缺少 SkBee 时的 NBT。 |
| [示例](cookbook.zh-CN.md) | upsert、分页、批量插入、保存物品 NBT、读取后修改。 |
| [兼容性](compatibility.zh-CN.md) | 版本、可用类型名、jar 内容及不支持的功能。 |

## GitHub wiki

GitHub wiki 同时提供中英文页面，中文页面排在前面。文档或发布文件更新后，
`.github/workflows/wiki.yml` 会运行 `scripts/publish-wiki.ps1` 更新页面。
请修改仓库中的源文件；直接在 wiki 中编辑的内容会在下次同步时被覆盖。

`scripts/wiki-pages.tsv` 定义源文件与页面名的对应关系，`scripts/wiki-sidebar.md` 定义侧栏。
中文页保留原有地址，英文页使用带 `-EN` 后缀的名称。
