# 读取行

**简体中文** | [English](reading.md)

读取行有四条语句：`select one`、`select many`、`select page`、`select entity ... by id`。它们都会等结果，
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

行以从 1 开始的行号加列名为键，例如 `{_users::1::name}`。哪怕只匹配到一行，行号也还在，所以 `select many` 的结果读法始终如一。`rowIndex::column` 形状的结果没有现成的计数表达式：`size of {_users::*}` 只数第一层的值，而每一行都是子列表。行数要从行号本身取，或者自己记一个计数器。

`select many` 没有 `ORDER BY`，所以哪一行成为 `::1` 是数据库说了算。顺序重要时请在脚本里自己排；本页只有 `select page` 自带顺序。

## 分页

```sk
select page 2 with size 20 from table "users" and store the results in {_page::*}:
    where all:
        active = true
```

- 页码与每页大小都从 1 开始：`page 1` 是第一页，大小 20 表示二十行。
- 键是**页内**的，所以 `{_page::1::name}` 是**这一页**的第一行，不是整张表的第一行。
- 行按**主键升序**返回，这也是分页必须有已注册主键的原因：这是各后端都能认同的顺序。
- 超出末页的页是空的，不会报错。
- 一页是那个顺序上的**偏移，不是快照**：两次读页之间插入或删除一行，它后面的所有行都会挪位，于是某一行可能被读到两次、或被跳过。表在被写入时，请改用主键游标（`id > {_last}`）遍历。

## 按 id 查

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
```

它不接受 `where` 块：直接按已注册的主键查找。没有这个值的行时什么都不存。它同样没有正文，所以也不写冒号，理由见 [写入行](writing.zh-CN.md)；不带 `where` 块的 `select one`、`select many` 与 `select page` 也一样。

## where 块

`where` 块里一行一个条件，放在 `where all:` 或 `where any:` 之下。前者要求条条成立，后者只要有一条成立。两种表头都可以取反：`where not all:` 要的是"至少有一条不成立"，`where no any:`（或 `where not any:`）要的是"没有一条成立"。

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

结果变量在其它情况下会怎样，也值得知道：

- **语句失败**（数据库拒绝了查询，或结果读不出来）：变量被清空，连它之前装的东西也一起没了，原因在 `last database error` 里。
- **语句在发出之前就被拒绝**（没有连接、表不存在、`where` 的值列放不下）：变量**原封不动**，还装着上一次读取的结果，`last database error` 是"这条语句根本没跑"的唯一迹象。

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

读变量之前先看 `last database error`，才能把"被拒绝"和"读到了"分开：被拒绝是唯一一种旧内容还在里面的情况。

## 失败

读取一定会等，也一定会暴露失败，所以紧跟其后的 `last database error` 就是出错原因；给读取加 `and wait` 照收，但没有
任何区别，因为它本来就会等。见 [错误与等待](errors-and-waiting.zh-CN.md)。
