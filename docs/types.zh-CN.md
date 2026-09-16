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

## 两个容易意外的点

**`date` 列只保留日期，不保留时间。** 它存为 SQL `DATE`，时分秒会被丢掉。需要精确时刻的脚本应该存
`bigint`（epoch 毫秒）或者 `timespan`。

**`time` 列是“Minecraft 一天中的时刻”**，也就是 Skript `time` 类型携带的那个数字（0…24000），不是墙上时间。
日历值用 `date`，时长用 `timespan`。

## NULL

SQL NULL 是能表达的，而且有两条彼此独立的规则：

- **写入**：`values` 块里字面量 `null` 会存成 SQL NULL。把列从块里省略掉不是同一件事：语句没有提到的列会保留
  数据库默认值 —— 没写 `not null` 的列默认是 NULL，自增主键则是下一个 id。
- **读取**：NULL 列不会在结果变量里写下它的键。Skript 的列表变量会把值为 null 的键删除，所以对脚本来说
  “该列是 NULL”和“没有这一列”长得一样。也就是说 `{_user::age} is not set` 表示“NULL 或不存在”，而不是 0。

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
