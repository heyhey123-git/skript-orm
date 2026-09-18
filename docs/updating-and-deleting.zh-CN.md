# 更新与删除

**简体中文** | [English](updating-and-deleting.md)

两者各有两种形态：按 `where` 块（可以匹配多行）和按主键（只匹配一行）。

## 按条件更新

```sk
update entities in table "users" with limit 10 and wait:
    values:
        active: false
    where all:
        last_seen < {_cutoff}
        active = true
if last database error is set:
    send "更新失败: %last database error%" to console
```

主体里是 `values` 块和一个可选的 `where` 块，顺序随意；值的写法和插入完全一致：没写的列不会被碰，字面量 `null`
才存 SQL NULL。见 [写入行](writing.zh-CN.md)。

## 按 id 更新

```sk
update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

它不接受 `where` 块：更新的是“已注册主键等于该值”的那一行。

**表里没有这个键不是错误。** 语句什么都不动，`last database error` 保持为空，`store affected rows` 报 `0`——想知道那一行到底在不在，看的是这个数。`delete one entity ... by id` 同理。

## 按条件删除

```sk
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
```

删除没有 values 块，能写的只有可选的 `where` 与可选的 limit。

## 按 id 删除

```sk
delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "删除失败: %last database error%" to console
```

这个检查回答的是"语句跑没跑"，不是"有没有那一行"：没有键匹配时删除什么都不做，也不报错，所以要说清到底删没删，得看 `and store affected rows in {_rows}` 给的数。

冒号标出的是有正文的语句。这条没有正文；从变量取值的 `update`、`upsert` 与 `insert` 也没有，它们都不写冒号，见 [写入行](writing.zh-CN.md)。

## 不写 where 会怎样

**没有 `where` 块的 `update entities` 和 `delete entities` 会作用于实现允许它作用的所有行。** 两个 section 都
接受这种写法，所以漏写 `where` 不会报错：

```sk
# 表里的每一行
delete entities from table "users" and wait
```

而 `where` 块里条件列表为空，几乎总是笔误：这样的块在脚本解析时就被拒绝，`delete` 和 `update` 一样。真要对所有行动手，就整个不写 `where` 块，写成 `delete entities from table "users"`，并且别忘了 `with limit`。

## limit

`with limit N` 表示最多 N 行。MySQL 写成 `... LIMIT N`，PostgreSQL 则先用 `ctid` 选出这些行，好让 `LIMIT` 仍然生效，MongoDB 也是先选出 id；只有通用 `"JDBC"` 连接无法表达，它会明确报错而不是悄悄忽略。

limit 必须解析成**单个正数**。零或负数会在运行时被拒绝，报 `Update limit must be positive.`（或 `Delete limit must be positive.`）；但表达式什么都解析不出来时——变量没设置，或者装着好几个值——语句会**完全不限量**，因为没有东西可应用。拿 limit 当保险的话，请在脚本里先检查它的值。

limit 是保险，不是分页手段：留下哪几行由数据库决定，所以让更新/删除可预期的是 `where` 块。

## 等待

两个 section 都会等自己的活儿干完：后续语句在改动结束后执行，失败可在 `last database error` 里读到。等待停住的是这条
trigger，不是服务器主线程。`and wait` 仍然照收、不起作用，因为现在每条语句都会等。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 改动了多少行

两个 section 也都接受 `and store affected rows in {_rows}`，它会留下语句动过的行数：

```sk
delete entities from table "sessions" and store affected rows in {_deleted} and wait:
    where all:
        last_seen < {_cutoff}
```

行数为 `0` 表示语句执行了、只是没匹配到任何行；用读到的值给自己上锁的更新，就是靠这个说“别人先动手了”。见
[影响行数](affected-rows.zh-CN.md)。

## 从变量更新

`update` 与 `upsert` 都接受“形状像查询结果的变量”作为新值，这也是“读出来、改一改、存回去”最短的写法：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
set {_user::age} to {_user::age} + 1
set {_id} to {_user::id}
delete {_user::id}
update one entity {_user::*} in table "users" by id {_id} and wait
```

用 `update` 时只有变量里出现的列会被碰；用 `upsert` 时主键还不存在的话会新建那一行。只是从查询结果拿来的变量不能直接交给 `update by id` 或 `upsert by id`：它一定带着主键，而这两种写法都拒绝含主键的 values。像上面那样先把键取出来再删掉，把它的值单独交给 `by id`。见 [菜谱](cookbook.zh-CN.md)。
