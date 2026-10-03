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
- 每页最多 **5000 行**，超过这个大小的分页会在查询发送前被拒绝。见[一次读取最多能存多少行](#一次读取最多能存多少行)。
- 每次翻页都会重新查询，按主键排序后跳过前面页数对应的行，并不会固定首次查询时的数据。两次读取之间插入或删除行，可能使后续行的位置变化，造成重复读取或遗漏。另一种翻页方法是记录上一页最后一个主键，再查询 `id > {_last}` 的行；但查询还必须按主键稳定排序。`select many` 本身不提供 `ORDER BY`，因此仅加这个条件不能可靠地翻页。

## 一次读取最多能存多少行

一次读取最多存储 **5000 行**。如果匹配结果超过上限，目标变量会被清空，`last database error` 会记录原因；不会返回不完整的结果。

限制主要是为了控制主线程开销。查询在后台执行，但 Skript 需要在主线程上存储结果。六列表的基准测试中，存储 5000 行约占用 36–47 毫秒主线程时间；读取 10000 行则会被拒绝。实际开销取决于结果包含多少值以及服务器使用的 Skript 版本。详见[存储结果的开销](#存储结果的开销)和[写入限制](writing.zh-CN.md#一次写入最多能发多少行)。

请把结果收窄，或者一页一页地遍历：

```sk
loop 100 times:
    select page loop-number with size 1000 from table "users" and store the results in {_rows::*}:
        where all:
            active = true
    if last database error is set:
        send "读取失败: %last database error%" to console
        stop loop
    if {_rows::1::id} is not set:
        stop loop
    # ... 使用这一页 ...
```

### 存储结果的开销

Skript 通常在主线程上逐个写入结果值。基准服务器上的耗时约为每值 1.0–1.4 微秒，具体取决于变量类型。六列的 5000 行包含 30000 个值，存储约需 47 毫秒；同样行数若只有两列，约需 15 毫秒。这些是主线程耗时，并不保证其他服务器上的 tick 时长相同。

结果超过 **10000 个值**时，插件可以先在后台按列表变量的层级整理结果，再由主线程将整理好的结果一次性设为全局列表变量。同一次测试中，30000 个值的主线程耗时从 47 毫秒降到 36 毫秒。这项优化有几个限制：

- 只适用于**全局列表变量**。结果存进局部或临时变量时，仍然逐值写入。
- 启动后**第一个**达到阈值的结果仍会逐值写入，插件会借此检查 Skript 的变量存储是否兼容。
- 如果不兼容，后续结果也会逐值写入，无需调整配置。

无论逐个写入值还是一次性设置整理好的结果，插件都会按顺序通知 Skript 保存各个值，因此这项优化不会跳过正常的变量保存流程。

## 按 id 查

```sk
select entity from table "users" by id {_id} and store the result in {_user::*}
```

`select entity ... by id` 直接按已注册的主键查找，不接受 `where` 块；没有匹配的行时，不存储任何结果。按主键查询没有缩进正文，因此不加冒号。不带 `where` 块的 `select one`、`select many` 与 `select page` 也不加冒号；见[写入行](writing.zh-CN.md)。

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
if last database error is set:
    send "查询失败: %last database error%" to sender
    stop
if {_user::id} is not set:
    send "没有这个用户。" to sender
    stop
if {_user::age} is not set:
    send "这个用户没有存年龄。" to sender
```

判断未设置的键表示“没有这一行”还是“该列为 NULL”之前，务必先检查 `last database error`。

## 失败

读取总会等待完成，并报告失败原因。可在下一条语句中读取 `last database error`。读取也接受 `and wait`，但不会改变行为。见 [错误与等待](errors-and-waiting.zh-CN.md)。
