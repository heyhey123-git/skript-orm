# 故障排查

**简体中文** | [English](troubleshooting.md)

按报错或现象查找原因和解决方法。

## “我加了一列，什么都没变”

建表使用 `CREATE TABLE IF NOT EXISTS`，注册不会修改已有的表。新列只会加入插件保存的表定义，不会出现在数据库中，也不会触发警告。

之后，使用新列的操作会失败；读取整张表也可能报错，因为数据库结果中没有这一列。

需要自行迁移数据库：

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

如果开发库的数据可以丢弃，也可以删表后让插件重建。线上库请使用原有的迁移工具，插件不负责迁移。见 [表](tables.zh-CN.md)。

## “Table 'users' not found.”

这张表尚未**在当前连接上**注册。常见原因有三种：负责连接与注册的脚本没有运行；脚本在此前失败，此时应查看失败时的 `last database error`；连接后来被替换，新连接中还没有注册任何表。

## “No database connected.”

当前没有可用连接：可能尚未连接、连接失败，或连接已被断开。连接失败时旧连接也已关闭，因此错误的账号或密码可能影响共用这条连接的所有脚本。见 [连接](connections.zh-CN.md)。

如果已有具名连接，只是没有默认连接，错误信息会说明这一点并列出连接名。此时使用 `use connection "logs"` 或 `make connection "logs" the default` 即可，不必重新建立连接。

## 行明明在，某列却是未设置

Skript 会删除值为 null 的列表变量键，所以 NULL 列没有对应的键。`{_user::age} is not set` 可能表示“年龄为 NULL”，也可能表示“没有这一行”，不表示“年龄为 0”。检查一个不可能为 NULL 的列，即可区分：

```sk
if {_user::id} is not set:
    send "没有这一行。"
else if {_user::age} is not set:
    send "行在，年龄是 NULL。"
```

见 [类型](types.zh-CN.md)。

## `select many` 之后 `{_users::name}` 是空的

`select many` 的结果先按行号组织，行号从 1 开始，例如 `{_users::1::name}`。即使只匹配一行，也需要这个行号。目前没有结果行数表达式；`size of {_users::*}` 只统计第一层的值，而每行都是子列表，不能用它统计行数。请按行号遍历或自行计数。见 [读取行](reading.zh-CN.md)。

这是 Skript 对嵌套列表的处理方式，脚本自行建立的同类列表也一样。要统计行数，请按行号遍历，见[示例](cookbook.zh-CN.md#逐页遍历)。

## `loop 1 to 20` 一次都不会执行

Skript 不支持 `loop 1 to 20`。加载脚本时会报解析错误并跳过这一行，循环体也不会运行。请改用 `loop 20 times` 和 `loop-number`：

```sk
loop 20 times:
    if {_users::%loop-number%::id} is not set:
        stop loop
    # 这里 loop-number 就是行号，从 1 到 20
```

```sk
loop {_indices::*}:
    # 这里 loop-value 是本次迭代的行号
```

如果已有行号列表，也可以直接遍历。`loop 1, 2, 3` 这样的逗号分隔列表同样有效。

## 一次删除或更新动了整张表

`delete entities` 和 `update entities` 允许省略 `where`，此时会作用于实现允许的所有行，漏写条件也不会报错。请补上条件，或改用 `by id`。见 [更新与删除](updating-and-deleting.zh-CN.md)。

## “Data type 'nbtcompound' cannot be used: SkBee is not installed, and it is what provides NBT compounds.”

`nbtcompound` 依赖 SkBee，缺少它时注册表就会失败，不会等到首次写入才报错。请安装 SkBee，或改用其他类型。脚本也需要 SkBee 才能构造 NBT compound；不使用这个类型的服务器不受影响。见 [类型](types.zh-CN.md)。

## “Auto-increment column 'id' must also be a primary key.”

`auto increment` 必须与 `primary key` 声明在同一列，因为这个值需要用来标识行：

```sk
    id: bigint, primary key, auto increment, not null
```

## 插入之后不知道 id 是多少

插件不返回自动生成的 id。可以由脚本分配 id 并使用 `upsert`，也可以在插入后按其他列查回该行。见[示例](cookbook.zh-CN.md)。

## `date` 列的时间没了

在 SQL 实现中，`date` 列存为 SQL `DATE`，不保留时分秒。需要精确时刻时，请用 `bigint` 保存 Unix 时间戳（明确使用秒还是毫秒），或用 `string` 保存带时区的时间文本。`timespan` 表示时长，不是时间点。MongoDB 的 `date` 列则保留 epoch 毫秒。`time` 列表示 Minecraft 一天中的时刻，也不是现实世界的钟表时间。见 [类型](types.zh-CN.md)。

## 分页会漏行或重复

分页取的是主键顺序上的**偏移，不是快照**。翻页期间插入或删除行，可能使后续行的位置发生变化，导致重复或遗漏。主键游标（`id > {_last}`）需要配合稳定的主键排序；`select many` 不提供 `ORDER BY`，因此仅加这个条件并不能完整替代 `select page`。

分页按已注册的主键排序，没有主键的表会被拒绝。页码从 1 开始，每页内部的行号也从 1 开始，所以 `{_page::1::name}` 是当前页的第一行，不是整张表的第一行。见 [读取行](reading.zh-CN.md)。

## “select many read more than 5000 rows ... and stored nothing.”

一条语句最多移动 5000 行，读取超过这个数量时不会存储任何内容：结果变量被清空，原因记录在 `last database error` 中。`select page` 每页超过 5000 行时会在查询发送前被拒绝。这个上限针对的是服务端线程而不是内存——结果是在该线程上逐下标写入列表变量的。

请把读取收窄，或者一页一页地遍历：

```sk
loop 100 times:
    select page loop-number with size 1000 from table "users" and store the results in {_rows::*}:
        where all:
            active = true
    if last database error is set:
        send "读取失败: %last database error%" to console
        stop loop
    if {_rows::1::id} is not set:
        stop loop
    # ... 使用这一页 ...
```

多行写入的处理方式不同：`insert many` 超过一条语句的预算时会被拆成多条语句发送，所有行都会写入，而不是被截断或拒绝。见 [读取行](reading.zh-CN.md#一次读取最多能存多少行) 与 [写入行](writing.zh-CN.md#一次写入最多能发多少行)。

## 查询很快，服务端还是卡了一下

查询在后台执行，但 Skript 会在服务端主线程上保存结果。开销取决于列值数量：一次基准测试中，存储六列的 5000 行约需 47 毫秒，两列的 5000 行约需 15 毫秒。尽量缩小查询范围，或逐页读取。见[存储结果的开销](reading.zh-CN.md#存储结果的开销)。

## 用 `insert many` 写入一个大列表会让服务端停顿

插件在发送数据前，会在服务端主线程上读取列表变量。一次基准测试中，读取 30000 个值约需 10 毫秒。即使插件会拆分数据库写入，更大的列表仍会带来更长的停顿。处理大量数据时，可用 `select page` 读取并逐页插入。见[一次写入最多能发多少行](writing.zh-CN.md#一次写入最多能发多少行)。

## 被拒绝的读取不是免费的

上限是在数据库返回之后才生效的。语句会多要一行（5001），以便把“5001 行”和“正好 5000 行”区分开，看到那一行就拒绝：什么都没存，但查询已经发出、数据已经读回。反复请求超过上限的脚本，每次都要付这个代价。`select page` 是例外，因为页大小是事先知道的，超出上限的页在发送之前就会被拒绝。

## 把 `insert many` 改快之后，`affected rows` 就不存了

如果在 MySQL URL 中设置了 `rewriteBatchedStatements=true`，Connector/J 可能为重写后的批次逐行返回 `SUCCESS_NO_INFO`。插件无法得到准确的影响行数，因此不会设置目标变量。若脚本需要这个数字，请从 URL 中移除该选项。见[影响行数](affected-rows.zh-CN.md)。

## 通用 JDBC 类型不支持 “auto increment”

通用 `"JDBC"` 类型不知道目标数据库的自增列语法，因此注册表会在发送建表语句前失败。请从列声明中去掉 `auto increment`，并在插入时自行提供该列的值。要支持某种数据库的自增语法，需要为对应方言添加实现。

## Skript 提示 “Empty configuration section!”

section 的冒号后没有缩进的正文时，Skript 会显示这条警告。

从变量取值或按 id 操作的写法不需要正文，因此不带冒号。这样的一行是 effect，不是 section，也就不会触发空 section 警告：

```sk
insert one {_user::*} into table "archived_users"
delete one entity from table "users" by id {_id} and wait
select entity from table "users" by id {_id} and store the result in {_user::*}
```

只有带缩进的 `where`、`values` 等正文时才需要冒号。单行语句去掉冒号即可消除警告。

## “Limited delete is not supported by this JDBC dialect.”

在通用 `"JDBC"` 连接上，`delete entities` 无论有没有写 `with limit` 或 `where`，都会报这个错误。`update entities` 同样会被拒绝，并报 `Limited update is not supported by this JDBC dialect.`。这些操作不会改动任何行。

要操作单个主键，可用 `delete one entity ... by id` 或 `update one entity ... by id`。若需一条语句更新或删除多行，请使用 `"MySQL"`、`"MariaDB"`、`"PostgreSQL"`、`"MongoDB"` 等支持的连接类型。

## 嵌套循环里的循环值要用后缀

嵌套循环中，`loop-number` 或 `loop-value` 没有后缀时，Skript 无法判断指的是哪一层，使用它的那一行会解析失败。编号从外层往里数：`loop-number-1` 指外层，`loop-number-2` 指内层；`loop-value-1` 和 `loop-value-2` 同理。

## 表名在一台服务器能用，另一台不行

Linux 上的 MySQL 表名通常区分大小写，具体取决于服务器配置。`register a database table "..."` 和后续每个 `table "..."` 都按原样使用表名，请始终保持相同的拼写和大小写。
