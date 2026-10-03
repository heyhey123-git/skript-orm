# 错误与等待

**简体中文** | [English](errors-and-waiting.md)

在一次 Skript 命令执行或事件处理中，每项数据库操作完成后才会执行下一条语句。等待期间局部变量保持不变；操作失败时，可从 `last database error` 读取原因。

## 哪些会等

| 语句 | 是否等待 | 说明 |
| --- | --- | --- |
| `create a connection` | 总是 | 不接受也不需要 `and wait`。 |
| `register a database table` | 总是 | 不接受也不需要 `and wait`。 |
| `in connection` | 不等 | 块内语句各自等待；切换本身不执行数据库操作。 |
| `use connection` | 不等 | 只改变后续语句使用的连接。 |
| `make ... the default` | 总是 | 包括关闭原先承担该角色的连接。 |
| `select one`、`select many`、`select page`、`select ... by id` | 总是 | 查询完成后才继续。 |
| `insert`、`insert many`、`insert ... if absent`、`update`、`upsert`、`delete` | 总是 | 写入完成后才执行后续语句。 |
| `disconnect ...` | 是 | 断开连接后，脚本才会继续执行。 |

表中标为“等待”的操作会暂停当前脚本的这次执行，操作完成后再继续。等待 JDBC 返回时不会阻塞服务器主线程，也就是运行游戏 tick 的线程；从列表变量读取值仍需使用主线程。`insert many` 会把读取源变量的工作分到多个 tick，详见[写入行](writing.zh-CN.md#一次写入最多能发多少行)。

所有读取和写入仍接受 `and wait`，但加上该子句不会改变执行方式。`and wait` 原本用于要求写入等待；现在每条读写语句都会等待。本页示例保留 `and wait`，以兼容 1.1 的写法；旧脚本无需修改。

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

- 错误信息属于操作所在的**事件**。两个玩家执行同一条命令时，各自保存自己的错误信息；之后无关的事件无法通过 `last database error` 读取先前操作的错误。
- 每个操作**开始前都会清空错误信息**，避免残留先前的错误。**事务内则会保留首次错误**：失败后的数据库语句会被跳过，不再清空它，以便脚本读取回滚原因。见 [事务](transactions.zh-CN.md)。
- `store affected rows` 的目标变量不同：**每条**带此子句的语句都会先清空变量，事务内也一样，避免误用上一条语句的行数。见 [影响行数](affected-rows.zh-CN.md)。
- 没有错误时，显示值为 `<none>`。请用 `is set` 判断，不要比较显示文本。
- 失败会设置错误信息；事务外成功的操作会留下空值，可据此判断成功。事务内仍须留意之前保留的错误。
- **失败不会终止脚本执行。** 后续语句仍会执行，因此上面的插入示例会检查错误并调用 `stop`。后续操作依赖写入成功时，也应先检查 `last database error`。
- Skript 也会将失败作为运行时错误输出到控制台，并通知拥有 `skript.see_runtime_errors` 权限的玩家。重复报错可能受到 Skript 的控制台输出限制，但脚本仍能通过 `last database error` 读取每次失败的原因。

`insert many` 从列表变量读取时，校验错误可能在数个 tick 后才出现。所有行校验通过之前不会发送 SQL；失败时影响行数变量保持未设置。读取期间不要修改源变量。若写入位于事务内，这段读取时间也计入事务超时。

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

如果先读取再写入，或先删除再插入，前一项数据库操作同样会在后一项开始前完成，无需额外添加等待。

## 不是数据库的失败

有些错误发生在语句发送前，例如表尚未在当前连接注册、列不在表定义中、Skript 无法将值转换为列类型，或表名不符合命名规则。脚本下一行同样可以通过 `last database error` 读取这些校验错误。重复注册相同的表定义会成功；改动后的定义如果与数据库中的表不兼容，则可能注册失败。见[表](tables.zh-CN.md#注册)。

数据库返回的错误也会写入 `last database error`，例如向 `not null` 列写入 `null`，或写入超过列长度的值。语句会等待数据库返回结果，再继续执行后续语句。
