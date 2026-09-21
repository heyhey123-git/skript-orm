# 事务

**简体中文** | [English](transactions.md)

事务让一组数据库改动要么全部提交，要么全部回滚。事务内的语句使用同一条连接，可以读取彼此尚未提交的改动；其他脚本能看到哪些数据，则取决于后端及其隔离级别。

```sk
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}

    update one entity in table "accounts" by id {_to} and wait:
        values:
            balance: {_to::balance} + {_amount}

if last database error is set:
    send "转账已回滚: %last database error%"
```

这个 section 与其他语句一样，使用当前生效的连接。`database transaction on connection "logs":` 可指定具名连接，`with timeout 5 seconds` 可设置事务最长持续时间，见[超时](#超时)。

## 它怎样结束

| 发生什么 | 事务怎么做 |
| --- | --- |
| 主体正常执行到末尾 | 提交 |
| 主体中的语句失败 | 主体结束后回滚 |
| `exit`、`stop` 或 `return` 离开主体 | 回滚 |
| `rollback database transaction` | 回滚，并离开 section |
| 超过超时时间仍未结束 | 自动回滚 |
| 连接被关闭，或插件被禁用 | 自动回滚 |

不提供显式 `commit` 语句。事务只在主体正常结束时提交，避免中途提交后，剩余语句的事务归属不明确。

`rollback database transaction` 只能用于事务内部，用于提前回滚并退出：

```sk
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
    if {_from::balance} < {_amount}:
        rollback database transaction
```

## 有语句失败时

事务会保留首次失败的原因，并进入只能回滚的状态。后续数据库语句会被跳过，不再发送到数据库，但不会因此终止整个主体：发送消息、修改变量等普通 Skript 语句仍可能继续执行，这些操作也不会随数据库改动一起回滚。

```sk
database transaction:
    insert one entity into table "orders" and wait:
        values:
            item: "sword"
    insert one entity into table "orders" and wait:     # 失败：列不存在
        values:
            itemm: "sword"
    insert one entity into table "audit" and wait:      # 跳过，不执行写入
        values:
            what: "order stored"
```

即使回滚成功，section 之后仍可通过 `last database error` 读取最初的错误。事务内的语句不会清空这条错误信息。`store affected rows` 的目标变量则仍按语句清空，避免将上一条语句的行数误当成被跳过语句的结果。见 [影响行数](affected-rows.zh-CN.md)。

## 一条连接上一次一条

事务内的所有语句都使用事务占用的同一条连接，依次执行，不会并发或乱序。事务也会等所有语句结束后才提交。每条语句本来就会等待，因此无需额外设置；事务内仍接受 `and wait`，但它不改变行为。

## 事务里再开事务

嵌套的 `database transaction` 会**加入外层事务**。只有最外层 section 才会提交，内层不是保存点，也不能单独撤销。需要注意：

- 内层指定**另一条连接**会被拒绝，报 `A database transaction is already open on another connection.`。这会使外层事务进入只能回滚的状态，整个事务的改动都将撤销。
- 内层执行 `rollback database transaction` 会回滚**整笔事务**，而不只是内层主体。之后外层主体中的数据库语句会报告事务已不再运行。

事务内调用的函数也属于当前事务。函数中的语句使用事务连接，函数内部的 `database transaction` 也会加入调用方的事务，而不是另开一笔。

## 超时

事务在整个生命周期中占用连接池的一条连接，若迟迟不结束，连接就无法归还。默认超时为 30 秒，用来防止脚本中途停止后连接一直被占用：主体内的错误可能让 Skript 直接终止 trigger，却不通知 section；`wait` 也可能暂停很长时间。

可以在开启事务时指定其他时长：

```sk
database transaction with timeout 2 minutes:
    ...
```

- 超时使用 Skript 时间值，例如 `2 minutes`、`500 milliseconds`，且必须为正数。
- `with a timeout of 2 minutes` 是同一子句，`a` 和 `of` 都可省略。
- 超时子句必须写在 `on connection` 之后：`database transaction on connection "logs" with timeout 2 minutes:`。
- 从事务开启、占用连接时开始计时，不是从第一条语句开始。计时**不会暂停**：数据库语句、语句间的脚本处理和 `wait` 都计入总时长。即使每条语句都很快，较长的主体也可能超时回滚；限制的是连接与行锁的占用时间，而不是单条语句的执行速度。
- 非正数会报 `The transaction timeout has to be positive.`；表达式求值为空会报 `The transaction timeout is not set.`。这两种情况都不会开启事务。
- 超时按事务设置。连接可以设置[语句超时](connections.zh-CN.md)，但没有连接级的事务超时；需要更长时间时，请在开启该事务的位置指定。

超时后事务会回滚，事务内接下来的语句会报告原因。不要在事务内长时间 `wait`，因为等待期间连接和已取得的行锁仍被占用。

事务中的语句以**剩余事务时间**作为驱动执行时限，向上取整到秒，且至少为一秒，不会重新获得完整的超时时长。到达期限时，会尝试取消正在执行的语句，避免继续占用连接。连接自身的[语句超时](connections.zh-CN.md#语句超时)不适用于事务内部，这里使用的是事务剩余时间。

回滚需要使用同一条连接，因此不能绕过尚未结束的语句立即执行，必须先等待语句退出。如果连接完全无响应，实际回滚和连接释放可能晚于既定期限；截止时间本身不会延后。此时需要 URL 中的 `socketTimeout` 限制等待时间，见[语句超时](connections.zh-CN.md#语句超时)。

确实需要较长时间的事务，应明确设置超时。如果耗时只是因为主体包含过多工作，可以考虑拆分：将不必纳入事务的慢查询和长时间计算移到外面，只保留必须一起生效的语句。

## 它不做什么

- **它不是锁。** 事务保证一组改动一起提交或回滚，但两个脚本分别读取、修改、写回同一个值，仍可能丢失其中一次更新。各自能看到什么，由数据库隔离级别决定。
- **它不跨连接。** 事务开启期间，`use connection` 和 `in connection` 不允许切换到其他连接，`disconnect` 与 `make ... the default` 也受到相应限制。两条连接需要两笔事务，不能保证一起提交。
- **它不是万能的。** 事务内禁止 `register a database table`，因为 MySQL 等数据库的建表操作会提交事务。数据库以外的操作也不会回滚，例如已经发出的消息无法撤回。
