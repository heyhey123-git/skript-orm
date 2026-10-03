# 类型

**简体中文** | [English](types.md)

注册表时需要声明列类型。类型决定可以写入哪些值，以及读回时得到什么值。

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
    joined: date, nullable
```

## 类型名

| 类型 | 脚本中的值 | MySQL/JDBC 存储 | 大小 |
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

`string` 可用括号指定长度，例如 `string(64)`。UUID 固定占 16 字节；坐标经过序列化后存储，默认长度为 2048 字节。PostgreSQL 不接受为 `uuid` 或 `location` 指定大小，见[表](tables.zh-CN.md)。

### PostgreSQL 与 MongoDB

上表的存储类型适用于 `"MySQL"` 和 `"JDBC"`。PostgreSQL 将 `tinyint` 存为 `SMALLINT`，`float` 存为 `REAL`，`double` 存为 `DOUBLE PRECISION`。二进制类型（`uuid`、`itemstack`、`location`、`bukkitserializable`、`nbtcompound`）都存为 `BYTEA`，不接受大小参数。因此，`uuid(16)` 和 `location(2048)` 会被拒绝。

MongoDB 将数据存为 BSON 文档，不使用 SQL 行列。类型映射如下；语句行为的差异见[兼容性](compatibility.zh-CN.md#mongodb)。

| 类型 | BSON |
| --- | --- |
| `boolean` | boolean |
| `tinyint` | int32（由单字节整数扩宽） |
| `int` | int32 |
| `bigint` | int64 |
| `double` | double |
| `float` | double（由 float 扩宽） |
| `string` | string |
| `uuid` | binary（16 字节） |
| `itemstack` | binary（与 SQL 实现相同的序列化数据） |
| `location` | binary（与 SQL 实现相同的序列化数据） |
| `bukkitserializable` | binary（与 SQL 实现相同的序列化数据） |
| `nbtcompound` | binary（与 SQL 实现相同的序列化数据） |
| `date` | int64（epoch 毫秒） |
| `time` | int32（ticks，0…24000） |
| `timespan` | int64（毫秒） |

MongoDB 不会强制执行声明的列大小和可空性。注册表时会创建主键索引，并在需要时准备自增计数器。集合可能随索引一起创建，也可能在首次写入时创建。

**MongoDB 的 `date` 列会保留具体时间**，以 Unix 时间戳的毫秒值存储。SQL `date` 列的行为不同，见下节。

## 日期与时间

**SQL 的 `date` 列只保留年月日。** 它存为 SQL `DATE`，时分秒会丢失。要保存精确时刻，可以用 `bigint` 存 Unix 时间戳（统一使用秒或毫秒），或用 `string` 存带时区的时间文本。MongoDB 的 `date` 列则会保留具体时间，见[上文](#postgresql-与-mongodb)。

**`time` 列表示 Minecraft 的日内时间**，即 Skript 的 `time` 值（0 到 24000 tick），不是现实世界的钟表时间。`timespan` 表示时长。

## NULL

写入和读取 SQL NULL 时，处理方式不同：

- **写入：** `values` 块中的字面量 `null` 会存为 SQL NULL。插入时省略某列，则使用数据库默认值；更新或 `upsert by id` 时，已有行中未指定的列保留原值。
- **读取：** NULL 列在结果变量中没有对应的键，因为 Skript 会删除值为 null 的列表变量键。因此，`{_user::age} is not set` 可能表示 NULL，也可能表示该列不存在；它不表示 0。

## NBT compound

`nbtcompound` 是唯一依赖其他插件实现的类型。SkBee 提供在脚本中构造 compound 的语法，本插件也通过 SkBee 的类读写它。

- **没有 SkBee 时**，注册含 `nbtcompound` 列的表会被拒绝，错误信息会明确提到 SkBee。插件的其他功能不受影响。
- **存储的是 NBT，不是文本。** compound 以 NBT 字节写入 `BLOB`，读回后仍是 compound，无需再从字符串解析。
- **存储的是快照。** SkBee 返回的 compound 可能是物品、实体或方块的实时视图。插件取值时会保存当时的内容，此后修改原对象不会改变已保存的数据。
- **SkBee 可以把 compound 显示为 SNBT。** 例如，可用 `"%{_row::data}%" contains "某个标签"` 检查读回的内容。

## 类型不匹配会怎样

值无法转换为列类型时，操作会失败，`last database error` 会指出列名和预期类型。数字转换还有以下规则：

- **整数越界会在发送语句前被检查。** `tinyint` 的范围是 −128…127，`int` 是 −2147483648…2147483647，`bigint` 是 64 位整数。超出范围会报错，不会强行截成可容纳的值。**小数写入整型列仍会向零截断**：例如将 `1.7` 写入 `int` 列，结果是 `1`，与 Skript 的整数转换一致。
- **数字写入 `string` 列时会转成文本。** `42` 和 `42.0` 都存为 `"42"`，`1.5` 存为 `"1.5"`。插件会完成转换，因为 Skript 不为这类列值提供数字到文本的转换器；原本就是文本的值不受影响。
- **`float` 占 4 字节，`double` 能精确表示到 2^53 的整数。** 将 `0.1` 写入 `float` 列，读回会得到 `0.10000000149011612`；将 `bigint` 量级的数写入 `double` 列，可能丢失低位。这是类型本身的精度限制，插件无法消除。

见 [错误与等待](errors-and-waiting.zh-CN.md)。
