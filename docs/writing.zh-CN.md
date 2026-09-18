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

各行可以写不同的列。省略某列时，只要数据库允许，这条语句就不提它。而当一条语句必须为所有行绑定同一组列时，该列按 NULL 写入。

## 有则更新、无则插入

```sk
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

`upsert` 写入给定主键值的行：已经存在就更新它。在 MySQL 上这是 `INSERT ... ON DUPLICATE KEY UPDATE`。

```sk
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
```

`if absent` 只在数据库认为该行不存在时插入，已经存在就按兵不动。在 MySQL 上这是 `INSERT IGNORE`，主键冲突会被静默跳过，不会变成更新，已有的行原封不动。该用哪个，看你心里“已经存在”是什么意思：

| 想要 | 用 |
| --- | --- |
| 没有就建、有就拿这些值覆盖 | `upsert` |
| 只在缺失时建，已有行别动 | `insert entity if absent` |
| 想知道到底建没建 | 只有 `if absent` 能看出来：插入被跳过时旧行的值原封不动，所以读回来的值和你写下的不一样，就说明那一行本来就在；`upsert` 则是不管原来有没有，都写下你的值 |

两者都取决于实现自己的冲突规则，它们的描述里也是这么写的。上文说的是 MySQL 的行为。

## 等待

`insert`、`update`、`upsert`、`if absent` 都接受 `and wait`：

- **带上它**：trigger 的后续语句等这条写入结束之后才执行，失败能在 `last database error` 里读到。
- **不带它**：写入交给后台，下一行立刻执行。同步检查仍会通过 `last database error` 报告，比如没有连接、表不存在、值放不下；至于数据库本身拒绝的失败，只会写进日志。

读取必定等待，所以“先写后读”的脚本应该给写入加 `and wait`，否则读取可能看到写入之前的行。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 写入了多少行

这些语句都可以把影响的行数留在变量里：在 `and wait` 之前写上 `and store affected rows in {_rows}`：

```sk
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

脚本靠它分辨“本来就在”和“刚刚写入”，也靠它写出“只在读到的值仍然是当时那个值时才生效”的条件。见 [影响行数](affected-rows.zh-CN.md)。
