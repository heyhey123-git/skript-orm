# 更新与删除

**简体中文** | [English](updating-and-deleting.md)

更新和删除各有两种形式：按 `where` 条件操作，可匹配多行；按主键操作，最多匹配一行。

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

主体包含 `values` 块和可选的 `where` 块，两者顺序不限。值的写法与插入相同：省略的列保留原值，字面量 `null` 用于写入 SQL NULL。见 [写入行](writing.zh-CN.md)。

## 按 id 更新

```sk
update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
```

这条语句更新已注册主键等于给定值的行，不接受 `where` 块。

**主键不存在不是错误。** 此时不会改动任何行，`last database error` 为空，`store affected rows` 返回 `0`。`delete one entity ... by id` 也一样。注意，MySQL 更新已有行但值未改变时也可能返回 `0`，不能仅凭更新行数为零断定行不存在。

## 按条件删除

```sk
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
```

删除不接受 `values` 块，只接受可选的 `where` 块和 limit。

## 按 id 删除

```sk
delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "删除失败: %last database error%" to console
```

这个检查判断的是语句是否成功，而不是行是否存在。主键没有匹配的行时，删除不会产生改动，也不会报错。要确认是否删掉了行，请用 `and store affected rows in {_rows}` 读取行数。

冒号用于引出语句正文。这条语句没有正文，因此不加冒号；从变量取值的 `update`、`upsert` 与 `insert` 也是如此，见 [写入行](writing.zh-CN.md)。

## 不写 where 会怎样

**不带 `where` 块的 `update entities` 和 `delete entities` 会作用于实现允许范围内的所有行。** 两个 section 都接受这种写法，因此漏写 `where` 不会报错：

```sk
# 删除表中的所有行。
delete entities from table "users" and wait
```

空的 `where` 块则会在解析脚本时被拒绝，`delete` 和 `update` 都一样。确实要操作所有行时，请省略整个 `where` 块，例如 `delete entities from table "users"`；需要限制操作范围时，可加上 `with limit`。

## limit

`with limit N` 表示最多操作 N 行。MySQL 使用 `... LIMIT N`；PostgreSQL 先通过 `ctid` 选出限定数量的行；MongoDB 则先选择 id。通用 `"JDBC"` 连接不支持这种限制，会明确报错，不会忽略 limit。

limit 必须求值为**单个正数**。零或负数会在运行时被拒绝，报 `Update limit must be positive.` 或 `Delete limit must be positive.`。如果表达式没有得到单个值，例如变量未设置或包含多个值，语句将**不限制行数**。若用 limit 防止误操作，请先在脚本中检查它的值。

limit 只限制数量，不用于分页；具体选中哪些行由数据库决定。要明确更新或删除的范围，仍应使用 `where` 条件。

## 等待

两个 section 都会等待操作完成，再执行后续语句。失败原因可通过 `last database error` 读取。等待只暂停当前 trigger，不阻塞服务器主线程。`and wait` 仍可使用，但不再改变行为。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 改动了多少行

两个 section 都支持 `and store affected rows in {_rows}`，将影响行数存入变量：

```sk
delete entities from table "sessions" and store affected rows in {_deleted} and wait:
    where all:
        last_seen < {_cutoff}
```

删除返回 `0` 表示没有删除任何行。更新返回 `0` 可能表示没有匹配的行，也可能是 MySQL 上匹配行的值未改变。用先前读取的值作为更新条件时，只有确保写入会改变值，才能用零行判断更新未生效。见 [影响行数](affected-rows.zh-CN.md)。

## 从变量更新

`update` 与 `upsert` 都接受符合查询结果结构的变量作为新值，适合读取、修改后再写回：

```sk
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = arg-1
set {_user::age} to {_user::age} + 1
set {_id} to {_user::id}
delete {_user::id}
update one entity {_user::*} in table "users" by id {_id} and wait
```

`update` 只修改变量中包含的列；`upsert` 还会在主键不存在时创建新行。查询结果不能直接传给 `update by id` 或 `upsert by id`，因为结果中包含主键，而这两种写法的 values 都不允许包含主键。请像示例一样，先保存主键值，再从变量中删除该键，最后将值单独传给 `by id`。见 [菜谱](cookbook.zh-CN.md)。
