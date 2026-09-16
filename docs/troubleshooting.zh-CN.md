# 排雷

**简体中文** | [English](troubleshooting.md)

这里的坑多半不声不响：操作看似做了其实没做，列明明在却读不到，失败只进了日志。下面每条都按“现象、原因、怎么办”来写，遇上问题按图索骥即可。

## “我加了一列，什么都没变”

建表用的是 `CREATE TABLE IF NOT EXISTS`，注册从不改动已经存在的表。插件把新列记进了自己的描述，数据库里并没有多出来，全程没有一句提醒。

随后你会看到：提到新列的操作用不了，读这张表也可能报错，因为结果里压根没有这一列。

亡羊补牢，自己迁移数据库即可：

```sql
ALTER TABLE users ADD COLUMN joined DATE NULL;
```

开发库图省事，直接删表让插件重建就行。线上库还是交给原有的迁移工具，这个插件不干这件事。见 [表](tables.zh-CN.md)。

## “Table 'users' is already registered.”

注册是记在连接上的，所以 reload 之后又注册一遍，必然被拒。先连接就好，新连接里一张表都没注册：

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

这张表**在这个连接上**从未注册过。常见三种情形：负责连接与建表的脚本没跑；它更早就失败了，这时去看那一刻的 `last database error`；或者之后连接被顶替过，而新连接里空空如也。

## “No database connected.”

当前没有数据库：可能还没连，可能连失败了，也可能被断开了。连接一旦失败，旧连接已经关掉，所以一次写错的账号密码，就能让服上所有脚本一起陷入这个状态。见 [连接](connections.zh-CN.md)。

## 行明明在，某列却是未设置

NULL 列不会写下自己的键，因为 Skript 会删掉值为 null 的列表变量键。所以 `{_user::age} is not set` 的意思是“NULL 或没有这一行”，而不是“等于 0”。要分清楚，就找一个不可能为 NULL 的列来问：

```sk
if {_user::id} is not set:
    send "没有这一行。"
else if {_user::age} is not set:
    send "行在，年龄是 NULL。"
```

见 [类型](types.zh-CN.md)。

## `select many` 之后 `{_users::name}` 是空的

`select many` 的键先有行号，从 1 起：`{_users::1::name}`。只匹配到一行，行号也还是 1。要行数就用 `size of {_users::*}`。见 [读取行](reading.zh-CN.md)。

## 写完之后读到的还是旧数据

那次写入没写 `and wait`，读取开始时它还没结束。读取会等，不等待的写入不会。给写入补上 `and wait` 即可。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 失败压根没被报告

`last database error` 里空空如也，控制台里却写着失败。这是不等待的写入遇上数据库层面失败时的老毛病：只写日志，不报告。只有数据库能判断的问题，比如某些 `where` 取值、列类型不合，也是同样下场。讳疾忌医不如直面，要么加 `and wait`，要么去看控制台。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 一次删除或更新动了整张表

`delete entities` 和 `update entities` 允许不写 `where`，此时会作用于实现允许的所有行，漏写也不算错。补上条件，或者改用 `by id` 的写法。见 [更新与删除](updating-and-deleting.zh-CN.md)。

## “Data type 'nbtcompound' needs SkBee, which is not installed.”

这个列类型依赖 SkBee，因此建表时就被拒，不会拖到第一行数据才失败。装上 SkBee，或者换个类型。再者，没有 SkBee 时脚本本来也构造不出 NBT 数据，所以这条只在“这个列本当派上用场”的服务器上出现。见 [类型](types.zh-CN.md)。

## “Auto-increment column 'id' must also be a primary key.”

`auto increment` 需要同一列上带 `primary key`，因为那个值要用来认行：

```sk
    id: bigint, primary key, auto increment, not null
```

## 插入之后不知道 id 是多少

插件不会把生成的 id 交回脚本。要么自己写这个值并用 `upsert`，要么事后按别的列把那一行找回来。见 [菜谱](cookbook.zh-CN.md)。

## `date` 列的时间没了

`date` 列是 SQL `DATE`，不存时分秒。要精确时刻，请用 `bigint`（epoch 毫秒）或 `timespan`。顺带一提，`time` 列存的是 Minecraft 一天中的时刻，同样不是墙上时间。见 [类型](types.zh-CN.md)。

## 分页会漏行或重复

分页按已注册的主键排序，没有主键的表会被拒；页码从 1 起。页内的键又从 1 起（`{_page::1::name}`），很容易误当成整张表的第一行。见 [读取行](reading.zh-CN.md)。

## Skript 提示 “Empty configuration section!”

只要 section 的冒号下面没有缩进内容，Skript 就会警告一次，跟这个 section 属于哪个插件无关。这句话是 Skript
自己的解析器打的，而它背后的开关是 Skript 内部的，配置文件与脚本都够不着，关不掉。

用变量给值的写法没有正文可写，按 id 工作的写法也一样，所以这些写法都不带冒号。不带冒号的一行是 effect，不是
section，Skript 也就没什么可警告的了：

```sk
insert one {_user::*} into table "archived_users"
delete one entity from table "users" by id {_id} and wait
select entity from table "users" by id {_id} and store the result in {_user::*}
```

冒号是留给有东西可缩进的语句的。读取只想取一行时，一个匹配所有行的条件就能办到：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        id >= 1
```

带 `where` 块、`values` 块或任何其它正文的 section，都保持原样。在这些一行写法出现之前写好的脚本依然能跑，也依然会挨这条警告；去掉冒号，就安静了。

## 表名在一台服务器能用，另一台不行

Linux 上的 MySQL 表名区分大小写，差之毫厘，谬以千里。`register a database table "..."` 里的名字按原样使用，之后每个 `table "..."` 也一样。保持同一种拼写。
