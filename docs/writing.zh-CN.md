# 写入行

**简体中文** | [English](writing.md)

写入行有五条语句：`insert one`、`insert many`、`insert entity if absent`、`upsert one entity`，以及用于
已有行的 `update`。它们描述行的方式是一样的。

## values 块

值写成 `column: expression` 行，可以直接写在 section 主体里，也可以放在 `values:` 块里：

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 1 + 24
        joined: now
```

- 右边可以是任何 Skript 表达式，变量、参数、函数都行。
- 每行必须在同一行里写成 `column: expression`。只有需要多行的操作（见 `insert many`）才允许嵌套块。
- **没写的列不会出现在语句里**。插入时，数据库默认值就此生效，自增主键正是这样保持自动的；而在 `update` 与 `upsert by id` 里，没写的列保持它原来的值。想存 SQL NULL，就写 `null`；见 [类型](types.zh-CN.md)。
- **写了列名、但表达式解析出空值，同样按 SQL NULL 写入。** `name: {_nick}` 在 `{_nick}` 未设置时，和"整行不写 `name`"不是一回事：键在那儿、只是没有值，于是语句写入 NULL（`not null` 列则直接失败）。只有把这一行整个省掉，才会保留原来的值。
- **列表变量带不动 SQL NULL。** Skript 会把设为 null 的键删掉，而查询结果里 NULL 列本来就没有键，所以"从查询结果里抄一行、再从变量写回去"时，这些列在变量里根本不存在：`insert` 会给它们数据库默认值（`not null` 又没有默认值的列会直接失败），`update` 则原样不动。想写 NULL，只能用带字面量 `null` 的 `values` 块。
- 列名不存在时，在发出任何语句之前就会失败。

## 插入一行

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "写入失败: %last database error%" to console
```

同一个 section 也可以从“形状像查询结果的变量”取这一行：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users"
```

这样的变量必须正好是一行；装着多行的变量在这里会被拒绝，它属于 `insert many`。

最后那一行下面没有内容可缩进，所以不写冒号；Skript 会把不带冒号的一行当作 effect 读。Skript 警告的是空 section，两种写法效果相同：有正文要缩进就带冒号，没有正文就不带。[读取行](reading.zh-CN.md) 与 [更新与删除](updating-and-deleting.zh-CN.md) 两页也是这个规则。

## 插入多行

`values:` 下的每个嵌套块是一行：

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

行也可以来自变量：

```sk
insert many {_rows::*} into table "archived_users" and wait
```

各行可以写不同的列，但有两条规则：

- **`values` 块**里每一行必须写同一组列。一条语句只能绑定一组列，所以某行少写了别的行有的列，运行时会被拒绝：`Batch row 2 does not contain the same columns as the first row.`
- 装着多行的**列表变量**则会补齐到共同的列集合，某行缺的列按 NULL 写入。查询结果因此可以直接插回去。MongoDB 两种写法都接受参差的行。

变量未设置或里面什么都没有时，语句会失败并报 `{_rows::*} is not set.`，而不是"写入 0 行"——而没匹配到任何行的 `select many` 恰恰会把结果变量留空，所以把它的结果喂给 `insert many` 之前要先检查一下。见 [读取行](reading.zh-CN.md)。

## 有则更新、无则插入

```sk
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

`upsert` 写入给定主键值的行：已经存在就更新它。主键写在 `by id` 里，**不要**写进 `values` 块：`values` 里出现主键会被拒绝，报 `The primary key must not be included in upsert values.`，因为正是主键决定这一行是插入还是更新。在 MySQL 上这是 `INSERT ... ON DUPLICATE KEY UPDATE`。

```sk
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
```

`if absent` 只在数据库认为该行不存在时插入，已经存在就按兵不动。在 MySQL 上这是 `INSERT IGNORE`，主键冲突会被静默跳过，不会变成更新，已有的行原封不动。但 `INSERT IGNORE` 不只是跳过重复键：MySQL 会把其它错误也降级成警告，所以超长的值会被截短存进去，而不是被拒绝。需要"放不下就报错"时，用 `insert one` 或 `upsert`。该用哪个，看你心里“已经存在”是什么意思：

| 想要 | 用 |
| --- | --- |
| 没有就建、有就拿这些值覆盖 | `upsert` |
| 只在缺失时建，已有行别动 | `insert entity if absent` |
| 想知道到底建没建 | `insert entity if absent ... and store affected rows in {_rows}`：`1` 表示确实写进去了，`0` 表示已有键占着它。读回来比对也行，但只有 `if absent` 会把旧行原样留着给你比 |

两者都取决于实现自己的冲突规则，它们的描述里也是这么写的。上文说的是 MySQL 的行为。

## 等待

写入会等自己的活儿干完：它之后的语句在改动被数据库接收后才执行，失败能在 `last database error` 里读到。等待停住的是这条 trigger，不是服务器主线程，所以语句在飞的时候别的玩家、别的脚本照常运行。

`and wait` 在这些语句上仍然照收，只是不起作用：现在每条语句都会等，写入也不例外；它曾经是写入用来要求这一点的写法，所以本页示例都还留着它。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 写入了多少行

这些语句都可以把影响的行数留在变量里：写上 `and store affected rows in {_rows}`：

```sk
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

对 `insert entity if absent` 来说，这个数就是"到底写没写进去"：`1` 写了，`0` 是已有键占着。脚本也靠它写出"只在读到的值仍然是当时那个值时才生效"的条件。至于别的语句，这个数字是各后端自己的说法——`upsert` 在 MySQL 上插入记 `1`、更新记 `2`，而 PostgreSQL 与 MongoDB 两种情况都记 `1`——所以拿它分支之前先看[影响行数](affected-rows.zh-CN.md)。
