# 类型

**简体中文** | [English](types.md)

列的类型写在建表里，它决定脚本能放什么进去、什么能拿出来。

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
    joined: date, nullable
```

## 类型名

| 类型 | 脚本里 | MySQL 存储 | 大小 |
| --- | --- | --- | --- |
| `boolean` | `true` / `false` | `BOOLEAN` | |
| `tinyint` | 一个字节放得下的整数（−128…127） | `TINYINT` | |
| `int` | 整数 | `INT` | |
| `bigint` | 整数，包含超出 `int` 范围的 | `BIGINT` | |
| `double` | 小数 | `DOUBLE` | |
| `float` | 小数 | `FLOAT` | |
| `string` | 文本 | `VARCHAR` | `string(64)`，默认 255 |
| `uuid` | 一个 UUID | `BINARY(16)` | 固定 16 |
| `itemstack` | 物品（含 meta 与 NBT） | `BLOB` | |
| `location` | 坐标（含世界） | `VARBINARY` | 默认 2048 |
| `bukkitserializable` | 任何 Bukkit 能序列化的对象 | `BLOB` | |
| `nbtcompound` | NBT compound（需要 SkBee） | `BLOB` | |
| `date` | Skript 的 date | `DATE` | |
| `time` | Skript 的 time | `INT` | |
| `timespan` | Skript 的 timespan | `BIGINT` | |

括号里的大小只对本身有大小概念的类型有意义：`string`、`uuid`、`location`。后两者的默认值本来就和内容匹配，
所以通常只给 `string` 写大小。

### MongoDB

上面那张表的“MySQL 存储”是 SQL 类型，`"MySQL"`、`"JDBC"` 与 `"PostgreSQL"` 建出来的就是它。`"MongoDB"`
是底下没有 SQL 的那个类型名：文档里存的是 BSON，没有任何东西会变成 SQL 列，同一批逻辑类型的对应关系如下。
这一实现其它不同之处在 [兼容性](compatibility.zh-CN.md#mongodb)。

| 类型 | BSON |
| --- | --- |
| `boolean` | boolean |
| `tinyint` | int32——一个字节加宽成整数 |
| `int` | int32 |
| `bigint` | int64 |
| `double` | double |
| `float` | double——float 加宽而来 |
| `string` | string |
| `uuid` | binary——它两半的 16 个字节 |
| `itemstack` | binary——与 SQL 实现存进 `BLOB` 的序列化内容相同 |
| `location` | binary——与 SQL 实现存进 `VARBINARY` 的序列化内容相同 |
| `bukkitserializable` | binary——与 SQL 实现存进 `BLOB` 的序列化内容相同 |
| `nbtcompound` | binary——与 SQL 实现存进 `BLOB` 的序列化内容相同 |
| `date` | int64——epoch 毫秒 |
| `time` | int32——ticks（0…24000） |
| `timespan` | int64——毫秒 |

`size` 与 `nullable` 在这里只是声明：集合没有 schema，没有任何东西强制它们。注册真正建立的是主键上的唯一索引
与 `auto increment` 背后的计数器，而集合由 MongoDB 在写入第一份文档时自行创建。

**在 MongoDB 上，`date` 列保留的是精确时刻。** BSON 没有“只有日期”的类型，所以这一列就是上面的 epoch 毫秒，
时分秒**不会**被丢掉。它是唯一一个在各实现之间含义不同的类型；语句层面的差异都写在
[兼容性](compatibility.zh-CN.md#mongodb) 里。下一节讲的是 SQL 实现。

## 两个容易意外的点

**`date` 列只保留日期，不保留时间——这是 SQL 实现的行为。** 在它们那里，这一列存为 SQL `DATE`，时分秒会被
丢掉；需要精确时刻的脚本应该存 `bigint`（epoch 毫秒）或者 `timespan`。MongoDB 是例外，原因见上面的
[MongoDB](#mongodb) 一节。

**`time` 列是“Minecraft 一天中的时刻”**，也就是 Skript `time` 类型携带的那个数字（0…24000），不是墙上时间。
日历值用 `date`，时长用 `timespan`。

## NULL

SQL NULL 是能表达的，而且有两条彼此独立的规则：

- **写入**：`values` 块里字面量 `null` 会存成 SQL NULL。把列从块里省略掉不是同一件事：插入时，语句没有提到的列用数据库默认值，也就是没写 `not null` 的列为 NULL、自增主键为下一个 id；而在 `update` 与 `upsert by id` 里，没提到的列保留它原来的值。
- **读取**：NULL 列不会在结果变量里写下自己的键。Skript 的列表变量会把值为 null 的键删掉，所以对脚本来说，“该列是 NULL”和“没有这一列”长得一模一样。也就是说 `{_user::age} is not set` 表示“NULL 或不存在”，而不是 0。

## NBT compound

`nbtcompound` 是唯一一种实现位于别的插件里的类型：能给脚本提供构造方式的是 SkBee，本插件也通过 SkBee 的类
读写 compound。

- **没有 SkBee 时**，注册带 `nbtcompound` 列的表会被明确拒绝，信息里点名 SkBee。服务器上插件其余部分照常工作，
  只有 NBT 这一块需要它。
- **存储形式是 NBT 本身**，不是文本：compound 以 NBT 字节写进 `BLOB`，所以读回来的是 compound，而不是还需要
  再解析的字符串。
- **值是脚本命名它的那一刻的快照。** SkBee 为物品/实体/方块给出的 compound 是那个活对象上的视图：改 compound
  就是改对象。而数据库里存的是操作写下那一刻的 compound，之后物品再改也不会改写已经存下的历史。
- **比较时用文本最省事。** SkBee 会把 compound 渲染成 SNBT，所以脚本可以用
  `"%{_row::data}%" contains "某个标签"` 检查读回来的内容，而不必逐层遍历。

## 类型不匹配会怎样

列放不下的值会让这次操作失败，而不是被近似地存进去。带 `and wait` 时，`last database error` 会说明是哪一列、
期望什么；不带时失败只会写进日志。见 [错误与等待](errors-and-waiting.zh-CN.md)。
