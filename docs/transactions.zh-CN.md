# 事务

**简体中文** | [English](transactions.md)

事务让一组数据库改动一起提交，或一起回滚。事务内的语句共用一条连接，可以读取彼此尚未提交的改动。其他脚本能看到什么，取决于数据库的隔离级别。

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

事务默认使用当前连接。要指定具名连接，写 `database transaction on connection "logs":`；要限制执行时间，可加上 `with timeout 5 seconds`。详见[超时](#超时)。

## 它怎样结束

| 发生什么 | 事务怎么做 |
| --- | --- |
| 主体正常执行到末尾 | 提交 |
| 主体中的语句失败 | 主体结束后回滚 |
| `exit`、`stop` 或 `return` 离开主体 | 回滚 |
| `rollback database transaction` | 回滚，并退出事务块 |
| 超过超时时间仍未结束 | 自动回滚 |
| 连接被关闭，或插件被禁用 | 自动回滚 |

没有显式的 `commit` 语句。事务块正常结束时会自动提交。

`rollback database transaction` 只能用于事务内部，用于提前回滚并退出：

```sk
database transaction:
    if {_from::balance} < {_amount}:
        rollback database transaction
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
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

即使回滚成功，事务块结束后仍可通过 `last database error` 读取最初的错误。事务内的语句不会清空这条错误信息。`store affected rows` 的目标变量则会清空，以免将上一条语句的行数误当成被跳过语句的结果。见[影响行数](affected-rows.zh-CN.md)。

## 执行顺序

事务内的语句依次使用同一条专用连接，全部完成后才提交。这里可以写 `and wait`，但不会改变执行方式。

## 嵌套事务

嵌套的 `database transaction` 会**加入外层事务**。只有最外层事务块会提交；内层不是保存点，无法单独回滚。需要注意：

- 内层指定**另一条连接**会被拒绝，报 `A database transaction is already open on another connection.`。这会使外层事务进入只能回滚的状态，整个事务的改动都将撤销。
- 内层执行 `rollback database transaction` 会回滚**整笔事务**，而不只是内层主体。之后外层主体中的数据库语句会报告事务已不再运行。

事务内调用的函数也属于当前事务。函数中的语句使用事务连接，函数内部的 `database transaction` 也会加入调用方的事务，而不是另开一笔。

## 超时

事务从开始到结束都占用连接池中的一条连接，默认超时为 30 秒。

可以在开启事务时指定其他时长：

```sk
database transaction with timeout 2 minutes:
    ...
```

- 超时使用 Skript 时间值，例如 `2 minutes`、`500 milliseconds`，且必须为正数。
- `with a timeout of 2 minutes` 是同一子句，`a` 和 `of` 都可省略。
- 超时子句必须写在 `on connection` 之后：`database transaction on connection "logs" with timeout 2 minutes:`。
- 从事务占用连接时开始计时。数据库语句、脚本处理和 `wait` 都计入总时长，即使每条数据库语句都很快，事务仍可能超时。这个时限也限制了连接和已取得的行锁被占用的时间。
- 非正数会报 `The transaction timeout has to be positive.`；表达式求值为空会报 `The transaction timeout is not set.`。这两种情况都不会开启事务。
- 超时按事务设置。连接可以设置[语句超时](connections.zh-CN.md)，但没有连接级的事务超时；需要更长时间时，请在开启该事务的位置指定。

超时后事务会回滚，事务内接下来的语句会报告原因。不要在事务内长时间 `wait`，因为等待期间连接和已取得的行锁仍被占用。

事务中的语句以**剩余事务时间**作为驱动执行时限，向上取整到秒，且至少为一秒，不会重新获得完整的超时时长。到达期限时，会尝试取消正在执行的语句，避免继续占用连接。连接自身的[语句超时](connections.zh-CN.md#语句超时)不适用于事务内部，这里使用的是事务剩余时间。

回滚需要使用同一条连接，因此不能绕过尚未结束的语句立即执行，必须先等待语句退出。如果连接完全无响应，实际回滚和连接释放可能晚于既定期限；截止时间本身不会延后。此时需要 URL 中的 `socketTimeout` 限制等待时间，见[语句超时](connections.zh-CN.md#语句超时)。

如果回滚后仍可能有语句在执行，或回滚本身失败，插件会关闭连接，不把它归还连接池。这能避免恢复自动提交时意外提交尚未完成的操作。回滚失败会附在报告的错误中；若事务超时，`last database error` 仍会显示超时原因。

事务确实需要更多时间时，可延长超时。不需要纳入事务的慢查询和耗时计算，尽量移到事务外。

## 它不做什么

- **它不是锁。** 事务保证一组改动一起提交或回滚，但两个脚本分别读取、修改、写回同一个值，仍可能丢失其中一次更新。各自能看到什么，由数据库隔离级别决定。
- **它不跨连接。** 事务开启期间，`use connection` 和 `in connection` 不允许切换到其他连接，`disconnect` 与 `make ... the default` 也受到相应限制。两条连接需要两笔事务，不能保证一起提交。
- **它不是万能的。** 事务内禁止 `register a database table`，因为 MySQL 等数据库的建表操作会提交事务。数据库以外的操作也不会回滚，例如已经发出的消息无法撤回。
