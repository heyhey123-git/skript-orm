# 读取行

**简体中文** | [English](reading.md)

读取行有四条语句：`select one`、`select many`、`select page`、`select entity ... by id`。它们都会等待查询完成，后续语句可直接读取结果。

## 查一行

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
send "name: %{_user::name}%, age: %{_user::age}%"
```

每一列对应变量中的一个键，键名就是列名。`where` 块可以省略，此时由数据库决定返回哪一行。要明确指定某一行，请按主键或唯一列查询。

## 查多行

```sk
select many entities from table "users" and store the results in {_users::*}:
    where all:
        active = true
send "第一个: %{_users::1::name}%"
```

结果以从 1 开始的行号和列名为键，例如 `{_users::1::name}`。即使只匹配到一行，也会保留行号，因此 `select many` 的结果结构始终一致。`rowIndex::column` 结构没有现成的行数表达式：`size of {_users::*}` 只统计第一层的值，而每行都是子列表。请根据行号键统计行数，或自行维护计数器。

`select many` 不带 `ORDER BY`，因此哪一行位于 `::1` 由数据库决定。需要固定顺序时，请在脚本中排序；本页只有 `select page` 保证返回顺序。

## 分页

```sk
select page 2 with size 20 from table "users" and store the results in {_page::*}:
    where all:
        active = true
```

- 页码与每页大小都必须至少为 1：`page 1` 是第一页，大小 20 表示每页二十行。
- 行号从每页重新开始，`{_page::1::name}` 是**当前页**的第一行，不是整张表的第一行。
- 结果按**主键升序**返回。因此，分页需要已注册的主键，以便各后端采用一致的排序方式。
- 页码超出末页时返回空结果，不会报错。
- 分页使用排序后的**偏移量，不是快照**。两次读取之间插入或删除行，可能使后续行的位置变化，造成重复读取或遗漏。遍历正在写入的表时，可考虑主键游标条件（`id > {_last}`），但必须同时保证按主键稳定排序；仅加这个条件并不足够，`select many` 本身不提供 `ORDER BY`。

## 按 id 查

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
```

这条语句直接按已注册的主键查找，不接受 `where` 块；没有匹配的行时，不存储任何结果。它没有正文，因此不加冒号，详见 [写入行](writing.zh-CN.md)。不带 `where` 块的 `select one`、`select many` 与 `select page` 也一样。

## where 块

`where` 块在 `where all:` 或 `where any:` 下每行写一个条件。前者要求全部成立，后者要求至少一条成立。两者都可以取反：`where not all:` 表示“至少一条不成立”，`where no any:`（或 `where not any:`）表示“全部不成立”。

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

值可以使用表达式，例如 `arg-1`、`{_cutoff}`、`now`，在块执行时求值。

## 空结果与 NULL 列

以下两种情况都会表现为某个键未设置：

- `select one` **没有匹配的行**，因此没有存储结果。
- 匹配到的行中，**该列为 NULL**。

失败也会影响结果变量：

- **语句执行失败**，例如数据库拒绝查询或无法读取结果：变量会被清空，原因记录在 `last database error` 中。
- **语句在发送前被拒绝**，例如没有连接、表不存在、`where` 中的值不适合列类型或页码为 0：变量**同样会被清空**。每次读取只保留本次结果或空变量，不会残留上次结果。

确认查询成功后，可以检查主键等不可能为 NULL 的列，区分“没有行”和“列为 NULL”：

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

请先用 `last database error` 判断查询是否成功。查询成功时，上面的两次检查才能区分“没有这一行”和“该列为 NULL”；空变量本身不能排除查询失败。

## 失败

读取总会等待完成，并报告失败原因。可在下一条语句中读取 `last database error`。读取也接受 `and wait`，但不会改变行为。见 [错误与等待](errors-and-waiting.zh-CN.md)。
