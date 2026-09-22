# 错误与等待

**简体中文** | [English](errors-and-waiting.md)

每条数据库语句都会等待操作完成，再执行后续语句。等待期间局部变量保持不变，失败原因记录在 `last database error` 中。因此，同一次 trigger 执行中的数据库操作按书写顺序执行，不会在后续语句已经运行后才完成写入。

## 哪些会等

| Section | 是否等待 | 说明 |
| --- | --- | --- |
| `create a connection` | 总是 | 不接受也不需要 `and wait`。 |
| `register a database table` | 总是 | 同上。 |
| `in connection` | 不等 | 块内语句各自等待；切换本身不执行数据库操作。 |
| `use connection` | 不等 | 只改变后续语句使用的连接。 |
| `make ... the default` | 总是 | 包括关闭原先承担该角色的连接。 |
| `select one`、`select many`、`select page`、`select ... by id` | 总是 | 查询完成后才继续。 |
| `insert`、`insert many`、`insert ... if absent`、`update`、`upsert`、`delete` | 总是 | 写入完成后才执行后续语句。 |
| `disconnect ...` | 下一行会等 | 异步执行，完成后 trigger 才继续；所有形式都不报告成功。 |

这些语句与 Skript 中其他延迟操作一样，会暂停当前 trigger，稍后再执行后续语句。**等待不会阻塞服务器主线程**，其他玩家和脚本不受影响。

所有读取和写入仍接受 `and wait`，但它不再改变行为。这个子句原本用于要求写入等待，现在所有语句都会等待。本页及相关页面的示例保留它，以兼容 1.1 的写法；旧脚本无需修改。

## last database error

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"
if last database error is set:
    send "写入失败: %last database error%" to console
    stop
send "已保存。" to console
```

- 错误信息属于操作所在的**事件**。两个玩家执行同一条命令时，各自保存自己的错误信息；不能在之后无关的事件中读取这次操作的结果。
- 每个操作**开始前都会清空错误信息**，避免残留先前的错误。**事务内则会保留首次错误**：失败后的数据库语句会被跳过，不再清空它，以便脚本读取回滚原因。见 [事务](transactions.zh-CN.md)。
- `store affected rows` 的目标变量不同：**每条**带此子句的语句都会先清空变量，事务内也一样，避免误用上一条语句的行数。见 [影响行数](affected-rows.zh-CN.md)。
- 没有错误时，显示值为 `<none>`。请用 `is set` 判断，不要比较显示文本。
- 失败会设置错误信息；事务外成功的操作会留下空值，可据此判断成功。事务内仍须留意之前保留的错误。
- **失败不会终止 trigger。** 后续语句仍会执行，因此示例会主动检查错误并 `stop`。如果后续操作依赖本次写入成功，也应先做检查。
- 失败也会作为 Skript 的运行时错误报出来，走的是 Skript 自己的那条通道：控制台会写明脚本、语法、行号以及那一行的内容，持有 `skript.see_runtime_errors` 权限的玩家会收到提示。同一行反复失败时，Skript 会按自己的帧限制合并这些输出（见其配置的 `runtime errors.*`）；脚本要读的那份始终在 `last database error` 里，不受影响。

## 先写后读

写入在下一条语句执行前已经完成，因此紧接着的查询可以读到成功写入的数据：

```sk
insert one entity into table "users" and wait:
    values:
        name: "Alice"

select many entities from table "users" and store the results in {_users::*}:
    where all:
        name = "Alice"
```

先读后写、先删后插也遵循这个顺序，无需额外添加等待。

## 不是数据库的失败

有些错误发生在语句发送前，例如表尚未在当前连接注册、列不在表定义中、Skript 无法将值转换为列类型，或重复注册同一张表。这些错误同样可以在下一条语句中通过 `last database error` 读取。

数据库返回的错误也通过同一方式报告，例如向 `not null` 列写入 `null`，或写入超过列长度的值。语句会等待数据库返回结果，再继续执行后续语句。
