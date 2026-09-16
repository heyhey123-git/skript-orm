# xiaojie-orm 文档

**简体中文** | [English](README.md)

这里每一页都有两种语言：`name.md` 是英文，`name.zh-CN.md` 是中文。如果你是从
[README](../README.zh-CN.md) 过来的，那里有同样的目录。

## 如果你是第一次用

先读 [快速上手](getting-started.zh-CN.md)。它最后会给出一个能存下一行、再读回来的脚本；之后的内容都是
你暂时还用不到的部分的参考。

## 参考

| 页面 | 内容 |
| --- | --- |
| [连接](connections.zh-CN.md) | 建立连接、单连接规则、断开连接。 |
| [表](tables.zh-CN.md) | 列语法、全部类型、主键与修饰符，以及建表**不会**做的事。 |
| [写入行](writing.zh-CN.md) | 插入一行/多行、从变量插入、upsert，以及 `values` 块的写法。 |
| [读取行](reading.zh-CN.md) | 查一行/多行/分页/按 id、`where` 块，以及结果的形状。 |
| [更新与删除](updating-and-deleting.zh-CN.md) | 按条件或按 id 更新、删除，以及 limit。 |
| [错误与等待](errors-and-waiting.zh-CN.md) | `and wait`、`last database error`，以及哪些部分在后台跑。 |
| [类型](types.zh-CN.md) | 每种列类型接受什么、怎么存。 |

## 出问题时

| 页面 | 内容 |
| --- | --- |
| [排雷](troubleshooting.zh-CN.md) | 看起来像成功、其实不是的沉默：被忽略的改表、消失的 NULL 列、没有 SkBee 时的 NBT。 |
| [菜谱](cookbook.zh-CN.md) | 整段可抄的写法：upsert、分页、批量插入、存物品的 NBT、读改写。 |
| [兼容性](compatibility.zh-CN.md) | 版本、jar 里打包了什么、不支持什么。 |

## 关于 GitHub wiki

如果这些页面要镜像到仓库 wiki，请保留文件名：页面之间以及 README 指向它们的链接都是相对路径，改名就会断。
