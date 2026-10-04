# 故障排查

**简体中文** | [English](troubleshooting.md)

按报错或现象查找原因和解决方法。

## “我加了一列，什么都没变”

建表使用 `CREATE TABLE IF NOT EXISTS`，注册不会修改已有的表。在脚本中新增列，不会让数据库自动添加该列。注册时，插件会核对已有表，并在 `last database error` 中报告缺少的列。

在数据库中的表结构与新声明一致之前，这份表定义无法注册成功。

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

脚本自行建立的嵌套列表也有这样一层行号。要统计 `select many` 的结果行数，请遍历行号，并逐行检查 `{_users::%loop-number%::id}`，见[示例](cookbook.zh-CN.md#逐页遍历)。

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

`select page` 每次都按主键顺序跳过前面若干行，并不会在第一次读取时固定整批结果。翻页期间如果有其他操作插入或删除行，后续页就可能重复或漏掉记录。记住上一页最后的 `id`，再用 `id > {_last}` 筛选，需要结果始终按该主键排序。`select many` 不提供 `ORDER BY`，因此只加筛选条件不能替代 `select page`。

分页按已注册的主键排序，没有主键的表会被拒绝。页码从 1 开始，每页内部的行号也从 1 开始，所以 `{_page::1::name}` 是当前页的第一行，不是整张表的第一行。见 [读取行](reading.zh-CN.md)。

## “select many read more than 5000 rows ... and stored nothing.”

一次读取最多保存 5000 行。`select many` 找到更多行时会清空结果变量，并在 `last database error` 中说明原因。`select page` 请求超过 5000 行时会在查询发送前被拒绝。这一上限用于控制结果占用的内存和主线程工作量：全局结果和需要服务器 API 的转换仍在主线程处理，局部结果中的普通值则可以在后台保存。

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

`insert many` 使用另一套限制：一条写入语句需要绑定的值超过 30000 个时，插件会拆成多条语句发送，不会丢弃多出的行。见 [读取行](reading.zh-CN.md#一次读取最多能存多少行) 与 [写入行](writing.zh-CN.md#一次写入最多能发多少行)。

## 查询很快，服务端还是卡了一下

查询在后台执行，存进局部变量的普通结果也可以在后台保存；全局结果仍在主线程发布。物品、位置等值的转换共用一个每 tick 目标预算为 2 毫秒的队列，但单个耗时很长的转换仍可能超过预算，最后发布全局结果也不受这个预算约束。处理大量值时，优先使用局部结果变量并减小每页的行数。见[存储结果的开销](reading.zh-CN.md#存储结果的开销)和[当前实测结果](benchmarking.zh-CN.md#新版服务端基准)。

## 用 `insert many` 写入一个大列表会让服务端停顿

插件在发送数据库写入前需要读取源变量。暂停脚本独占的局部输入可以在后台处理；全局输入仍在主线程跨 tick 分段读取，每段最多执行 4096 个处理步骤，读取一行可能需要多个步骤。每段以约 2 毫秒为时间预算，需要服务器 API 的值转换则使用共用的主线程队列。单个耗时很长的处理步骤仍可能超过预算。列表越大，整次全局写入可能经历的 tick 越多；服务端的其他工作也可能拉长 tick。此前“读取 30000 个值约需 10 毫秒”的测量早于分片读取，不能当作当前单个 tick 的开销。处理大量数据时，可使用局部源变量，或用 `select page` 分页读取后逐页插入。见[一次写入最多能发多少行](writing.zh-CN.md#一次写入最多能发多少行)。

## 被拒绝的读取不是免费的

`select many` 的 5000 行上限要在数据库返回后才能判断。查询最多请求 5001 行，以区分“结果超出上限”和“恰好 5000 行”。如果读到第 5001 行，结果变量会被清空，但查询已经执行。脚本反复请求过大的结果，每次都会承担查询成本。`select page` 的页大小事先已知，因此超过 5000 行的页会在查询发送前被拒绝。

## 把 `insert many` 改快之后，`affected rows` 就不存了

这个现象发生在旧版插件的 `"MySQL"` JDBC 批处理路径中。设置 `rewriteBatchedStatements=true` 后，Connector/J 可能返回 `SUCCESS_NO_INFO`，使影响行数变量保持未设置。当前版本的 `"MySQL"` 连接会自行发送参数化多行 `INSERT`，从这条语句取得影响行数；URL 中的选项不控制这条路径。如果仍在使用旧版批处理路径，且脚本需要准确计数，请移除该选项。见[影响行数](affected-rows.zh-CN.md)。

## 通用 JDBC 类型不支持 “auto increment”

通用 `"JDBC"` 连接没有声明自增列的 SQL 写法，因此注册表会在发送 `CREATE TABLE` 前失败。请从列声明中去掉 `auto increment`，并在插入时自行提供该列的值。若要支持其他数据库的自增列，插件需要为该数据库生成相应的 SQL。

## Skript 提示 “Empty configuration section!”

带冒号的语句后没有缩进正文时，Skript 会显示这条警告。

从变量取值或按 id 操作的写法不需要正文，因此不带冒号。Skript 将这种写法视为独立语句（effect），而不是带正文的代码块（section），因此不会触发空 section 警告：

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
