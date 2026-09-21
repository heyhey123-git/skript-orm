# 写入行

**简体中文** | [English](writing.md)

写入行有五条语句：`insert one`、`insert many`、`insert entity if absent`、`upsert one entity`，以及用于更新已有行的 `update`。它们使用相同的方式指定行数据。

## values 块

值写成 `column: expression`，可以直接放在 section 主体里，也可以放在 `values:` 块里：

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 1 + 24
        joined: now
```

- 右侧可以是任意 Skript 表达式，包括变量、参数和函数。
- 每个 `column: expression` 必须写在同一行。只有接受多行数据的位置才允许嵌套块，见 `insert many`。
- **省略的列不会出现在语句里**。插入时由数据库提供默认值，自增主键也因此能自动生成；在 `update` 与 `upsert by id` 更新已有行时，省略的列保留原值。要存 SQL NULL，请写 `null`，见 [类型](types.zh-CN.md)。
- **列名已写出，但表达式求值为空时，会写入 SQL NULL。** `{_nick}` 未设置时，`name: {_nick}` 并不等于省略 `name`：前者会写入 NULL，若该列为 `not null` 则写入失败。更新时，只有省略整行才能保留该列原值。
- **列表变量不能保存 SQL NULL。** Skript 会删除被设为 null 的键，查询结果中的 NULL 列也没有对应的键。因此，从查询结果复制一行再通过变量写回时，这些列会被省略：`insert` 使用数据库默认值（`not null` 且没有默认值时会失败），`update` 则保留原值。要明确写入 NULL，请使用带有字面量 `null` 的 `values` 块。
- 列名不存在时，语句会在发送到数据库之前失败。

## 插入一行

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "写入失败: %last database error%" to console
```

同一个 section 也可以从符合查询结果结构的变量中读取行数据：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users"
```

变量必须恰好包含一行；多行数据会被拒绝，请改用 `insert many`。

最后一条语句没有需要缩进的正文，因此不加冒号，Skript 会将它解析为 effect。带冒号却没有正文会触发空 section 警告。两种形式执行相同的操作：有正文时加冒号，没有正文时省略。[读取行](reading.zh-CN.md) 与 [更新与删除](updating-and-deleting.zh-CN.md) 中的示例也遵循这个规则。

## 插入多行

`values:` 下的每个嵌套块代表一行：

```sk
insert many entities into table "users" and wait:
    values:
        1:
            name: "Alice"
            age: 25
        2:
            name: "Bob"
            age: 30
```

也可以从变量中读取多行：

```sk
insert many {_rows::*} into table "archived_users" and wait
```

各行的列集合需要遵循以下规则：

- **`values` 块**中的每一行必须包含相同的列。一条语句只能绑定一组列；如果某行缺少其他行包含的列，运行时会报错：`Batch row 2 does not contain the same columns as the first row.`
- 包含多行的**列表变量**会按所有行的列集合补齐，缺少的列写入 NULL，因此查询结果可以直接用于插入。MongoDB 对这两种写法都允许各行包含不同的列。

变量未设置或为空时，语句会报 `{_rows::*} is not set.`，而不是成功写入 0 行。`select many` 没有匹配结果时也会留下空变量，因此将结果传给 `insert many` 前，请先检查变量是否有值。见 [读取行](reading.zh-CN.md)。

## 有则更新、无则插入

```sk
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

`upsert` 写入指定主键的行，已有该行则更新。主键应写在 `by id` 中，**不能**放进 `values` 块，否则会报 `The primary key must not be included in upsert values.`。主键用于确定插入还是更新；MySQL 使用 `INSERT ... ON DUPLICATE KEY UPDATE` 实现这一操作。

```sk
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
```

`if absent` 只在数据库判定该行不存在时插入，已有行则保留原值，不像 `upsert` 那样覆盖。在 MySQL 上，它会尝试正常插入，仅将重复键错误视为“行已存在”；值超出列长度、向 `not null` 列写入 `null` 等其他错误仍会导致语句失败。按需求选择即可：

| 想要 | 用 |
| --- | --- |
| 不存在则创建，存在则用给定值更新 | `upsert` |
| 仅在不存在时创建，保留已有行 | `insert entity if absent` |
| 判断是否创建了新行 | `insert entity if absent ... and store affected rows in {_rows}`：`1` 表示已插入，`0` 表示键已存在。也可以读回数据比较，但只有 `if absent` 会保留已有行供比较 |

两者都遵循具体实现的冲突规则，语法说明中也有注明。以上介绍的是 MySQL 的行为。

## 等待

写入完成后，后续语句才会执行；失败原因可通过 `last database error` 读取。等待只暂停当前 trigger，不阻塞服务器主线程，其他玩家和脚本仍可正常运行。

这些语句仍接受 `and wait`，但它不再改变行为：现在所有语句都会等待完成，包括写入。这个子句曾用于要求写入等待，本页示例为兼容旧写法而保留。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 写入了多少行

这些语句都支持 `and store affected rows in {_rows}`，将影响行数存入变量：

```sk
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

对 `insert entity if absent`，`1` 表示已插入，`0` 表示键已存在。影响行数也可用于条件更新，判断写入时数据是否仍与先前读取的一致。其他语句的计数规则因后端而异：例如，MySQL 的 `upsert` 插入记 `1`、更新记 `2`，而 PostgreSQL 与 MongoDB 两种情况都记 `1`。用这个数字判断下一步操作前，请先阅读[影响行数](affected-rows.zh-CN.md)。
