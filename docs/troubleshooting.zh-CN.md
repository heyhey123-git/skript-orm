# 排雷

**简体中文** | [English](troubleshooting.md)

有些问题不容易察觉：操作执行了却没生效，列已经声明却读不到，表迟迟没有建好。下面按现象说明原因和处理办法，方便你逐项排查。

## “我加了一列，什么都没变”

建表使用 `CREATE TABLE IF NOT EXISTS`，注册不会修改已有的表。新列只会加入插件保存的表定义，不会出现在数据库中，也不会触发警告。

之后，使用新列的操作会失败；读取整张表也可能报错，因为数据库结果中没有这一列。

需要自行迁移数据库：

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

如果开发库的数据可以丢弃，也可以删表后让插件重建。线上库请使用原有的迁移工具，插件不负责迁移。见 [表](tables.zh-CN.md)。

## “Table 'users' is already registered.”

表注册信息属于**连接**，而 `create a connection` 每次都会建立新连接。这个错误表示你在同一条连接上重复注册了同名表，例如第二个脚本重复注册，或脚本 reload 后直接注册，没有重新连接。将连接和注册放在同一个 `on load` 中，可在 reload 时替换连接，避免这类重复注册：

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
```

见 [表](tables.zh-CN.md)。

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

插件不返回自动生成的 id。可以由脚本分配 id 并使用 `upsert`，也可以在插入后按其他列查回该行。见 [菜谱](cookbook.zh-CN.md)。

## `date` 列的时间没了

在 SQL 实现中，`date` 列存为 SQL `DATE`，不保留时分秒。需要精确时刻时，请用 `bigint` 保存 Unix 时间戳（明确使用秒还是毫秒），或用 `string` 保存带时区的时间文本。`timespan` 表示时长，不是时间点。MongoDB 的 `date` 列则保留 epoch 毫秒。`time` 列表示 Minecraft 一天中的时刻，也不是现实世界的钟表时间。见 [类型](types.zh-CN.md)。

## 分页会漏行或重复

分页取的是主键顺序上的**偏移，不是快照**。翻页期间插入或删除行，可能使后续行的位置发生变化，导致重复或遗漏。主键游标（`id > {_last}`）需要配合稳定的主键排序；`select many` 不提供 `ORDER BY`，因此仅加这个条件并不能完整替代 `select page`。

分页按已注册的主键排序，没有主键的表会被拒绝。页码从 1 开始，每页内部的行号也从 1 开始，所以 `{_page::1::name}` 是当前页的第一行，不是整张表的第一行。见 [读取行](reading.zh-CN.md)。

## Skript 提示 “Empty configuration section!”

section 的冒号下没有缩进内容时，Skript 就会发出警告，与该 section 属于哪个插件无关。这条信息来自 Skript 解析器，控制它的开关是内部实现，无法通过配置文件或脚本关闭。

从变量取值或按 id 操作的写法不需要正文，因此不带冒号。这样的一行是 effect，不是 section，也就不会触发空 section 警告：

```sk
insert one {_user::*} into table "archived_users"
delete one entity from table "users" by id {_id} and wait
select entity from table "users" by id {_id} and store the result in {_user::*}
```

只有需要缩进正文的语句才使用冒号。如果只想读取一行，可以写一个覆盖目标行的条件；下面适用于 id 从 1 开始的表：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        id >= 1
```

有 `where` 块、`values` 块或其他正文的 section 无需改动。旧脚本中的空 section 仍能运行，但会继续警告；对于上面的一行写法，去掉冒号即可。

## 表名在一台服务器能用，另一台不行

Linux 上的 MySQL 表名通常区分大小写，具体取决于服务器配置。`register a database table "..."` 和后续每个 `table "..."` 都按原样使用表名，请始终保持相同的拼写和大小写。
