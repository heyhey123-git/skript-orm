# 写入行

**简体中文** | [English](writing.md)

写入行的 section 有五个：`insert one`、`insert many`、`insert entity if absent`、`upsert one entity`，以及用于
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

- 右边可以是任何 Skript 表达式，变量、参数、函数都可以。
- 每行必须在同一行里写成 `column: expression`；只有需要多行的操作（见 `insert many`）才允许嵌套块。
- **没写的列不会出现在语句里**，于是数据库默认值生效 —— 自增主键就是这样保持自动的。写 `null` 才是存 SQL NULL；
  见 [类型](types.zh-CN.md)。
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
insert one {_user::*} into table "archived_users":
```

这样的变量必须正好是一行；装着多行的变量在这里会被拒绝，它属于 `insert many`。

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
insert many {_rows::*} into table "archived_users" and wait:
```

各行可以写不同的列。省略某列时，数据库允许的情况下这条语句就不提它；而当一条语句必须为所有行绑定同一组列时，
该列会按 NULL 写入。

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

`if absent` 只在数据库认为该行不存在时插入，存在时什么都不做。在 MySQL 上这是 `INSERT IGNORE`，所以主键冲突会被
静默跳过、而不是变成更新：已存在的行保留原值。选哪个取决于“已经存在”该是什么意思：

| 想要 | 用 |
| --- | --- |
| 没有就建、有就拿这些值覆盖 | `upsert` |
| 只在缺失时建，已有行别动 | `insert entity if absent` |
| 想知道到底建没建 | 先用 `upsert` 或 `if absent`，再把行读回来比较 |

两者都取决于实现的冲突规则（它们的描述里就是这么写的）；上面说的是 MySQL 的行为。

## 等待

`insert`、`update`、`upsert`、`if absent` 都接受 `and wait`：

- **带上它**：trigger 的后续语句在这条写入结束之后才执行，失败可以在 `last database error` 里读到。
- **不带它**：写入交给后台，下一行立刻执行。同步检查仍然会通过 `last database error` 报告（没有连接、表不存在、
  值放不下），但来自数据库本身的失败只会写日志。

读取一定会等，所以“先写后读”的脚本应该给写入加 `and wait`；否则读取可能看到写入之前的行。见
[错误与等待](errors-and-waiting.zh-CN.md)。
