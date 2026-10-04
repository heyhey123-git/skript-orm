# 写入行

**简体中文** | [English](writing.md)

新增行可以用 `insert one` 或 `insert many`。要保留已有行，用 `insert entity if absent`；要按主键插入或更新，用 `upsert one entity`；只修改已有行则用 `update`。这几种语句的列值写法相同。

## values 块

在插入、更新或 upsert 语句的缩进正文中，用 `列名: 表达式` 指定要写入的值。这些列值可以直接写在正文中，也可以放在 `values:` 块里：

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

`insert one {_user::*} into table "archived_users"` 可以从列表变量中取出一行并插入表中。单行数据以列名作为键，例如 `{_user::name}` 保存 `name` 列，`{_user::age}` 保存 `age` 列。`select one` 的查询结果正是这种格式，也可以用 `set` 自行构造这些键：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users"
```

`insert one` 一次只接受一行。变量中包含多行时会报错，请改用 `insert many`。

示例中的 `insert one {_user::*} into table "archived_users"` 已在语句本身指定了数据来源，没有缩进的正文，因此不需要冒号；加上冒号会触发“代码块为空”（empty section）的警告。冒号用于引出 `values:`、`where all:` 等缩进正文，见[读取行](reading.zh-CN.md)与[更新与删除](updating-and-deleting.zh-CN.md)。

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

`insert many {_rows::*}` 从列表变量读取多行。每行用一层行号或其他行键区分，行内再以列名作为键，例如 `{_rows::1::name}` 和 `{_rows::1::age}` 属于第一行，`{_rows::2::name}` 属于第二行。`select many` 和 `select page` 会生成带有行号的这种结构：

```sk
insert many {_rows::*} into table "archived_users" and wait
```

各行的列集合需要遵循以下规则：

- **`values` 块**中的每一行必须包含相同的列。一条批量插入语句使用同一组列名；如果某行缺少其他行包含的列，运行时会报错：`Batch row 2 does not contain the same columns as the first row.`
- 包含多行的**列表变量**会按所有行的列集合补齐，缺少的列写入 NULL，因此查询结果可以直接用于插入。MongoDB 对这两种写法都允许各行包含不同的列。

变量未设置或为空时，语句会报 `{_rows::*} is not set.`，而不是成功写入 0 行。`select many` 没有匹配结果时也会留下空变量，因此将结果传给 `insert many` 前，请先检查变量是否有值。见 [读取行](reading.zh-CN.md)。

### 一次写入最多能发多少行

一条写入语句最多绑定 **30000 个值**。以六列表为例，每条语句可写入 5000 行。更大的批次会拆成多条语句，不会悄悄丢行。列越多，每条语句能容纳的行就越少；即使单行超过上限，也会单独发送。

数据库能提供精确计数时，`and store affected rows in {_rows}` 会汇总拆分后各条语句的影响行数。如果后面的语句失败，前面已执行的语句不会自动撤销；需要一并撤销时，请使用 `database transaction`，见[事务](transactions.zh-CN.md)。

`insert many` 从列表变量读取数据时，处理方式取决于数据存放在哪里：

- **局部变量**，例如 `{_rows::*}`：当前脚本暂停后，插件在后台读取和检查数据，并在操作结束前独占这份局部变量上下文。普通数字、文本等值也可以在后台转换。如果取到的是脚本共享的默认变量，则改用主线程处理，不把它当作当前脚本独占的数据。
- **全局变量**，例如 `{rows::*}`：仍在主线程分段读取，每段最多处理 4096 个步骤，并尽量控制在约 2 毫秒内。一行可能需要多个步骤；未完成的部分留到后续 tick。
- **转换时需要服务器 API 的值**，例如物品、位置：转换在主线程执行。所有请求共用一个转换队列，每 tick 的目标总预算为 2 毫秒。队列在两个值之间检查预算，无法中断正在执行的单次转换；因此单个慢转换仍可能超过预算。

**所有行都通过输入检查和转换后，才向数据库发送写入请求。** 未知列或无法转换的值会让整批输入失败。数据库执行阶段仍可能失败；若已有部分语句执行成功，只有事务才能将这些改动一并撤销。写入完成后才会继续执行当前脚本，局部变量也会恢复。`values:` 块中的表达式仍在主线程求值，不使用这种分片变量读取方式。

转换器分别声明读取和写入需要在哪个线程执行。未知转换器或自定义转换器默认使用主线程，只有明确声明某个转换方向可在后台执行时，插件才会这样调度。对象放在局部变量里，并不意味着对象本身可以安全地被其他线程访问。

写入完成前不要修改全局源变量。读取器发现变化时会报错，但不能检测全部修改，尤其是已经读完的行被改动，或现有键的值被覆盖。局部源变量随当前脚本一起暂停，但其中的可变对象仍可能被其他代码共享。在事务中，准备输入和等待转换的时间也计入事务超时。分片限制单个 tick 尝试处理的工作量，无法保证固定的 tick 耗时。处理大量数据时，也可以按 `select page` 的页大小分批写入，以减少内存占用。另见[读取行](reading.zh-CN.md#一次读取最多能存多少行)。

MySQL 会将多行合并到一条插入语句中，列值仍通过参数传入，并按参数数量上限和服务端的 `max_allowed_packet`（允许接收的最大数据包大小）拆分。NBT、物品等值转换为存储格式后的大小难以可靠预估，因此会逐行发送。单行本身仍须满足服务端的包大小限制。其他通过 JDBC 连接的数据库沿用各自现有的批量插入方式。


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

上面介绍的是 MySQL 如何处理 `upsert` 的重复键和 `insert entity if absent` 的重复键错误。`"MariaDB"` 使用相同的 SQL 写法，也按这些规则处理。其他数据库的影响行数见[影响行数](affected-rows.zh-CN.md)。

## 等待

写入完成后，后续语句才会执行；失败原因可通过 `last database error` 读取。等待数据库返回只暂停当前这次脚本执行，不会占用服务器主线程，其他玩家和脚本仍可正常运行。

`insert one`、`insert many`、`insert entity if absent`、`upsert` 和 `update` 仍接受 `and wait`，但有无这个子句都会等待操作完成。`and wait` 曾用于要求写入等待，本页示例为兼容旧写法而保留。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 写入了多少行

`insert one`、`insert many`、`insert entity if absent`、`upsert` 和 `update` 都支持 `and store affected rows in {_rows}`，将影响行数存入变量：

```sk
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

对 `insert entity if absent`，`1` 表示已插入，`0` 表示键已存在。影响行数也可用于条件更新，判断写入时数据是否仍与先前读取的一致。其他语句的计数规则因后端而异：例如，MySQL 的 `upsert` 插入记 `1`、更新记 `2`，而 PostgreSQL 与 MongoDB 两种情况都记 `1`。用这个数字判断下一步操作前，请先阅读[影响行数](affected-rows.zh-CN.md)。
