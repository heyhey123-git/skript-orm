# skript-orm 文档

**简体中文** | [English](README.md)

这里每页都备了两种语言：`name.md` 是英文，`name.zh-CN.md` 是中文。从 [README](../README.zh-CN.md) 过来也一样，那里有同一份目录。

## 第一次用，先读这篇

[快速上手](getting-started.zh-CN.md)。它的终点是一段能存下一行、再读回来的脚本。其余页面都是参考，等你真用到某一块时再翻。

## 参考

| 页面 | 内容 |
| --- | --- |
| [连接](connections.zh-CN.md) | 建立连接、单连接规则、断开连接。 |
| [表](tables.zh-CN.md) | 列语法、全部类型、主键与修饰符，以及建表**不会**做的事。 |
| [写入行](writing.zh-CN.md) | 插入一行或多行、从变量插入、upsert，以及 `values` 块的写法。 |
| [读取行](reading.zh-CN.md) | 查一行、多行、分页、按 id，`where` 块，以及结果的形状。 |
| [更新与删除](updating-and-deleting.zh-CN.md) | 按条件或按 id 更新、删除，以及 limit。 |
| [影响行数](affected-rows.zh-CN.md) | `store affected rows` 子句，以及不用事务的条件写入。 |
| [错误与等待](errors-and-waiting.zh-CN.md) | `and wait`、`last database error`，以及哪些部分在后台跑。 |
| [事务](transactions.zh-CN.md) | 全做或全不做：事务怎样结束、失败会怎样、超时。 |
| [类型](types.zh-CN.md) | 每种列类型接受什么、怎么存。 |

## 出了问题

| 页面 | 内容 |
| --- | --- |
| [排雷](troubleshooting.zh-CN.md) | 那些看似成功的沉默：被忽略的改表、读不到的 NULL 列、没有 SkBee 时的 NBT。 |
| [菜谱](cookbook.zh-CN.md) | 整段可抄的写法：upsert、分页、批量插入、存物品 NBT、读改写。 |
| [兼容性](compatibility.zh-CN.md) | 版本、脚本能写的类型名、jar 里打包了什么、不支持什么。 |

## GitHub wiki

中文页会镜像到仓库的 wiki，作为只读的阅读副本。`.github/workflows/wiki.yml` 在 `docs/` 变动后调用
`scripts/publish-wiki.ps1` 生成页面，所以要以仓库为准，不要在 wiki 上直接改。页面名以及页与页之间的每条链接
来自 `scripts/wiki-pages.tsv` 与 `scripts/wiki-sidebar.md`，两者就在 `scripts/publish-wiki.ps1` 旁边。
