# 读取行

**简体中文** | [English](reading.md)

读取行有四个 section：`select one`、`select many`、`select page`、`select entity ... by id`。它们都会等结果，
所以紧跟其后的语句已经拿到数据了。

## 查一行

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
send "name: %{_user::name}%, age: %{_user::age}%"
```

行里的每一列会成为变量的一个键，键名就是列名。`where` 块可以省略；省略时从表里取一行。取到哪一行由数据库决定，
所以需要可预期的读取就要指定主键或唯一列。

## 查多行

```sk
select many entities from table "users" and store the results in {_users::*}:
    where all:
        active = true
send "第一个: %{_users::1::name}%"
```

行以从 1 开始的行号加列名为键，例如 `{_users::1::name}`。哪怕只匹配到一行，行号也还在，所以 `select many` 的结果读法始终如一。要行数就用 `size of {_users::*}`。

## 分页

```sk
select page 2 with size 20 from table "users" and store the results in {_page::*}:
    where all:
        active = true
```

- 页码与每页大小都从 1 开始：`page 1` 是第一页，大小 20 表示二十行。
- 键是**页内**的，所以 `{_page::1::name}` 是**这一页**的第一行，不是整张表的第一行。
- 分页需要已注册的主键，因为必须排序才能分页。
- 超出末页的页是空的，不会报错。

## 按 id 查

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}:
```

它不接受 `where` 块：直接按已注册的主键查找。没有这个值的行时什么都不存。

## where 块

`where` 块里一行一个条件，放在 `where all:` 或 `where any:` 之下。前者要求条条成立，后者只要有一条成立。

```sk
select many entities from table "users" and store the results in {_users::*}:
    where any:
        name = "Alice"
        age > 30
        joined between {_from} and {_to}
```

| 条件 | 含义 |
| --- | --- |
| `column = value` | 相等。与 `null` 比较表示“是 NULL”。 |
| `column != value` | 不相等。 |
| `column > value`、`column >= value` | 大于、大于等于。 |
| `column < value`、`column <= value` | 小于、小于等于。 |
| `column between a and b` | 闭区间。 |

值那一边是表达式，所以 `arg-1`、`{_cutoff}`、`now` 都能用，并在块执行时求值。

## 空结果与 NULL 列

从脚本看这两者长得一样，都表现为某个键没有被设置：

- `select one` **没有匹配的行**，于是什么都没存。
- 匹配到的行里**该列是 NULL**。

要区分它们，就看一个不可能为 NULL 的列，比如主键：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
if {_user::id} is not set:
    send "没有这个用户。" to sender
    stop
if {_user::age} is not set:
    send "这个用户没有存年龄。" to sender
```

## 失败

读取一定会等，也一定会暴露失败，所以紧跟其后的 `last database error` 就是出错原因；给读取加 `and wait` 没有任何
区别。见 [错误与等待](errors-and-waiting.zh-CN.md)。
