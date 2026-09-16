# 排雷

**简体中文** | [English](troubleshooting.md)

这个插件里的坑大多是“安静”的：什么都没做的操作、明明在却读不到的列、只写进日志的失败。下面每一条都是
“现象 → 原因 → 怎么办”。

## “我加了一列，什么都没变”

表是用 `CREATE TABLE IF NOT EXISTS` 建的，注册永远不会修改已经存在的表。插件把新列记进了自己的描述，数据库里
并没有多出来，而且不会有任何警告。

之后你会看到：提到新列的操作失败，读取这张表也可能失败，因为结果里没有这一列。

自己迁移数据库即可：

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

开发库直接删表让插件重建更快。线上库请继续用它现有的迁移工具管表结构，这个插件不是迁移工具。见 [表](tables.zh-CN.md)。

## “Table 'users' is already registered.”

注册是按连接记的，所以 reload 之后又注册一遍的脚本会被拒绝。先连接，就会得到一个什么都没注册的新连接：

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

## “Table 'users' not found.”

这张表**在这个连接上**从未注册过。常见原因：负责连接和注册的脚本没跑、它更早就失败了（去看那一刻的
`last database error`）、或者之后连接被替换过 —— 新连接一开始没有任何表。

## “No database connected.”

当前没有数据库：还没连、连接失败，或者被断开了。由于连接失败后旧连接已经被关掉，一次错误的账号密码就能让服务端上
所有脚本都进入这个状态。见 [连接](connections.zh-CN.md)。

## 行明明在，某列却是未设置

NULL 列不会写下它的键，因为 Skript 会删除值为 null 的列表变量键。所以 `{_user::age} is not set` 的意思是
“NULL 或没有这一行”，而不是“等于 0”。用一个不可能为 NULL 的列来区分：

```sk
if {_user::id} is not set:
    send "没有这一行。"
else if {_user::age} is not set:
    send "行在，但年龄是 NULL。"
```

见 [类型](types.zh-CN.md)。

## `select many` 之后 `{_users::name}` 是空的

`select many` 的键先有从 1 开始的行号：`{_users::1::name}`。只匹配到一行时行号同样是 1。行数用
`size of {_users::*}`。见 [读取行](reading.zh-CN.md)。

## 写完之后读到的还是旧数据

那次写入没写 `and wait`，读取开始时它还没结束。读取会等，而不等待的写入不会。给写入加 `and wait`。见
[错误与等待](errors-and-waiting.zh-CN.md)。

## 失败完全没有被报告

`last database error` 里什么都没有，但控制台里有失败信息。这就是“不等待的写入”遇到数据库层面失败时的行为：只写日志、
不报告。只有数据库能判断的问题（某些 `where` 值、列类型问题）也一样。要么加 `and wait`，要么看控制台；见
[错误与等待](errors-and-waiting.zh-CN.md)。

## 一次删除/更新动了整张表

`delete entities` 和 `update entities` 允许不写 `where`，此时会作用于实现允许的所有行 —— 漏写 `where` 不算错误。
补上条件，或者用 `by id` 的写法。见 [更新与删除](updating-and-deleting.zh-CN.md)。

## “Data type 'nbtcompound' needs SkBee, which is not installed.”

这个列类型依赖 SkBee，所以建表时就被拒绝，而不是等到第一行数据才失败。装 SkBee，或者换一个类型。注意没有 SkBee 时
脚本本来也构造不出 NBT compound，所以这条只会在“这个列本来是要用的”服务器上出现。见 [类型](types.zh-CN.md)。

## “Auto-increment column 'id' must also be a primary key.”

`auto increment` 需要同一列上有 `primary key`，因为那个值要用来标识行：

```sk
    id: bigint, primary key, auto increment, not null
```

## 插入之后不知道 id 是多少

插件不会把生成的 id 交回脚本。要么自己写这个值并用 `upsert`，要么之后按别的列把那一行找回来。见
[菜谱](cookbook.zh-CN.md)。

## `date` 列的时间没了

`date` 列是 SQL `DATE`，不存时分秒。需要精确时刻时用 `bigint`（epoch 毫秒）或 `timespan`。`time` 列存的是
Minecraft 一天中的时刻，也不是墙上时间。见 [类型](types.zh-CN.md)。

## 分页会漏行或重复

分页按已注册的主键排序，所以没有主键的表会被拒绝；页码从 1 开始。页内的键又从 1 开始（`{_page::1::name}`），很容易
误以为是整张表的第一行。见 [读取行](reading.zh-CN.md)。

## 表名在一台服务器能用、另一台不能

在 Linux 上 MySQL 的表名区分大小写。`register a database table "..."` 里的名字按原样使用，之后每个
`table "..."` 也一样。拼写保持一致。
