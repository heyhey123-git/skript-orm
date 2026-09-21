# 类型

**简体中文** | [English](types.md)

列类型在建表时声明，决定了脚本能写入什么值，以及读回什么值。

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
| `tinyint` | 单字节整数（−128…127） | `TINYINT` | |
| `int` | 整数 | `INT` | |
| `bigint` | 整数，可超出 `int` 范围 | `BIGINT` | |
| `double` | 小数 | `DOUBLE` | |
| `float` | 小数 | `FLOAT` | |
| `string` | 文本 | `VARCHAR` | `string(64)`，默认 255 |
| `uuid` | UUID | `BINARY(16)` | 固定 16 |
| `itemstack` | 物品（含 meta 与 NBT） | `BLOB` | |
| `location` | 坐标（含世界） | `VARBINARY` | 默认 2048 |
| `bukkitserializable` | Bukkit 可序列化的对象 | `BLOB` | |
| `nbtcompound` | NBT compound（需要 SkBee） | `BLOB` | |
| `date` | Skript 的 date | `DATE` | |
| `time` | Skript 的 time | `INT` | |
| `timespan` | Skript 的 timespan | `BIGINT` | |

`string` 可在括号里指定长度。`uuid` 固定占 16 字节，`location` 则使用自己的序列化格式；PostgreSQL 不允许为后两者指定大小。见 [表](tables.zh-CN.md)。

### MongoDB

上表的“MySQL 存储”列适用于 `"MySQL"` 与 `"JDBC"`。`"PostgreSQL"` 的映射略有不同：`tinyint` 对应 `SMALLINT`，`float` 对应 `REAL`，`double` 对应 `DOUBLE PRECISION`；所有二进制类型（`uuid`、`itemstack`、`location`、`bukkitserializable`、`nbtcompound`）都对应 `BYTEA`，不接受大小参数，因此 `uuid(16)` 和 `location(2048)` 会被拒绝。

`"MongoDB"` 不使用 SQL，而是将数据存为 BSON 文档，没有 SQL 列。同一组逻辑类型的 BSON 映射如下；其他差异见 [兼容性](compatibility.zh-CN.md#mongodb)。

| 类型 | BSON |
| --- | --- |
| `boolean` | boolean |
| `tinyint` | int32（由单字节整数扩宽） |
| `int` | int32 |
| `bigint` | int64 |
| `double` | double |
| `float` | double（由 float 扩宽） |
| `string` | string |
| `uuid` | binary（UUID 的高、低 64 位，共 16 字节） |
| `itemstack` | binary（与 SQL 实现写入 `BLOB` 的序列化内容相同） |
| `location` | binary（与 SQL 实现写入 `VARBINARY` 的序列化内容相同） |
| `bukkitserializable` | binary（与 SQL 实现写入 `BLOB` 的序列化内容相同） |
| `nbtcompound` | binary（与 SQL 实现写入 `BLOB` 的序列化内容相同） |
| `date` | int64（epoch 毫秒） |
| `time` | int32（ticks，0…24000） |
| `timespan` | int64（毫秒） |

`size` 与 `nullable` 在这里只是声明，集合没有强制执行它们的 schema。注册会按表定义建立主键唯一索引，并为 `auto increment` 准备计数器。集合可能在建立索引时创建；若此前尚未创建，则会在首次写入文档时创建。

**MongoDB 的 `date` 列保留精确时刻。** BSON 没有只表示日期的类型，因此这里用 epoch 毫秒存储，**不会**丢掉时分秒。这是唯一一个在不同实现中含义不同的类型；语句行为的差异见 [兼容性](compatibility.zh-CN.md#mongodb)。下一节介绍 SQL 实现。

## 两个容易意外的点

**SQL 实现的 `date` 列只保留日期，不保留时间。** 该列存为 SQL `DATE`，时分秒会被丢掉。需要精确时刻时，请用 `bigint` 保存 Unix 时间戳（明确使用秒还是毫秒），或用 `string` 保存带时区的时间文本。`timespan` 表示时长，不是时间点。MongoDB 是例外，见上面的 [MongoDB](#mongodb) 一节。

**`time` 列表示 Minecraft 一天中的时刻**，即 Skript `time` 类型中的数值（0…24000），不是现实世界的钟表时间。日历值用 `date`，时长用 `timespan`。

## NULL

脚本可以读写 SQL NULL，但写入和读取各有一条规则：

- **写入**：`values` 块中的字面量 `null` 会存为 SQL NULL，省略列则不同。插入时，未指定的列使用数据库默认值：未声明 `not null` 的列为 NULL，自增主键为下一个 id。执行 `update` 或 `upsert by id` 时，已有行中未指定的列保留原值。
- **读取**：NULL 列在结果变量中没有对应的键。Skript 会删除值为 null 的列表变量键，因此脚本无法仅凭这个键区分“列为 NULL”和“列不存在”。`{_user::age} is not set` 表示“NULL 或不存在”，不是 0。

## NBT compound

`nbtcompound` 是唯一依赖其他插件实现的类型。SkBee 提供在脚本中构造 compound 的语法，本插件也通过 SkBee 的类读写它。

- **没有 SkBee 时**，注册含 `nbtcompound` 列的表会被拒绝，错误信息会明确提到 SkBee。插件的其他功能不受影响。
- **存储的是 NBT，不是文本。** compound 以 NBT 字节写入 `BLOB`，读回后仍是 compound，无需再从字符串解析。
- **存储值是脚本提供该值时的快照。** SkBee 返回的物品、实体或方块 compound 是对应对象的实时视图，修改 compound 就会修改对象。数据库保存的是操作取值时的内容，之后再修改物品不会影响已保存的数据。
- **用文本检查内容最方便。** SkBee 会将 compound 转为 SNBT，脚本可以用 `"%{_row::data}%" contains "某个标签"` 检查读回的内容，不必逐层遍历。

## 类型不匹配会怎样

值不符合列类型时，操作会失败，`last database error` 会指出列名和预期类型。不过，范围检查和精度损失需要分开看：

- **整数越界会在发送语句前被检查。** `tinyint` 的范围是 −128…127，`int` 是 −2147483648…2147483647，`bigint` 是 64 位整数。超出范围会报错，不会强行截成可容纳的值。**小数写入整型列仍会向零截断**：例如将 `1.7` 写入 `int` 列，结果是 `1`，与 Skript 的整数转换一致。
- **`float` 占 4 字节，`double` 能精确表示到 2^53 的整数。** 将 `0.1` 写入 `float` 列，读回会得到 `0.10000000149011612`；将 `bigint` 量级的数写入 `double` 列，可能丢失低位。这是类型本身的精度限制，插件无法消除。

见 [错误与等待](errors-and-waiting.zh-CN.md)。
