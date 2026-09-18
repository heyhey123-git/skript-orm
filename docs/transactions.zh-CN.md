# 事务

**简体中文** | [English](transactions.md)

事务让一组语句要么全部生效，要么全部不生效。它跑在同一条连接上，所以里面的语句能看见彼此尚未提交的改动，而在提交之前，别的脚本读不到其中任何一条。

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

这个 section 用的是当前生效的连接，和别的语句一样。`database transaction on connection "logs":` 改成指定某条具名连接，`with timeout 5 seconds` 则改变它能开多久——见[超时](#超时)。

## 它怎样结束

| 发生什么 | 事务怎么做 |
| --- | --- |
| 主体跑到末尾 | 提交 |
| 主体里有语句失败 | 主体结束后回滚 |
| `exit`、`stop` 或 `return` 离开主体 | 回滚 |
| `rollback database transaction` | 回滚，并离开 section |
| 超过超时仍然开着 | 自己回滚 |
| 连接被关闭，或插件被禁用 | 自己回滚 |

没有 `commit` 可以写。一个"提交之后继续跑主体"的语义，会让后面的语句落在一个已经不存在的事务里，而"它们现在算哪个事务"的每一种答案都是意外。

`rollback database transaction` 只能写在事务里面，它是从事务中途脱身的办法：

```sk
database transaction:
    update one entity in table "accounts" by id {_from} and wait:
        values:
            balance: {_from::balance} - {_amount}
    if {_from::balance} < {_amount}:
        rollback database transaction
```

## 有语句失败时

第一个失败会被保留，事务变成只能回滚的状态。它之后的语句什么都不做：把它们发给数据库，等于写入一份马上要丢掉的工作。

```sk
database transaction:
    insert one entity into table "orders" and wait:
        values:
            item: "sword"
    insert one entity into table "orders" and wait:     # 失败：没有这个列
        values:
            itemm: "sword"
    insert one entity into table "audit" and wait:      # 什么都不做
        values:
            what: "order stored"
```

`last database error` 里是最初那个失败，所以 section 之后的那行仍然说得出出了什么事，即使回滚本身是成功的。事务唯一会跨语句保留的就是这个槽位：事务里的语句不会清掉它，因为回滚的原因比那些被跳过的语句的沉默值钱。`store affected rows` 的变量不按这个规矩来——它逐条语句清空，事务里也一样，免得留下一条从未给出答复的语句的数字。见 [影响行数](affected-rows.zh-CN.md)。

## 一条连接上一次一条

事务里的每条语句都跑在事务占住的那条连接上，所以两条语句不可能同时在飞，也不可能互相超车：第二条会在一个还没搭好的事务里写入，而事务也不可能在构成它的语句跑完之前提交。这一点不需要额外写什么，因为每条语句本来就会等；事务里的 `and wait` 照收，不起作用。

## 超时

一个事务从头到尾占着连接池里的一条连接，所以永远不结束的事务就是池子永远拿不回来的那条。默认超时是 30 秒，它是为"脚本中途停了"准备的：主体里出错时，Skript 会直接结束整条 trigger 而不通知 section；而一个 `wait` 想停多久就停多久。

想要别的时长，就写在开事务的那条语句上：

```sk
database transaction with timeout 2 minutes:
    ...
```

- 它是一段 Skript 时间（`2 minutes`、`500 milliseconds`），而且必须是正数。
- `with a timeout of 2 minutes` 是同一个子句：`a` 和 `of` 都可以省。
- 它写在 `on connection` 之后，顺序固定：`database transaction on connection "logs" with timeout 2 minutes:`。
- 计时从事务开启时开始，因为占住连接的是"开启"这个动作，而不是主体里的第一条语句。
- 值不是正数、或者表达式算出来是空的，会分别报 `The transaction timeout has to be positive.` 与 `The transaction timeout is not set.`，并且不会开启事务。
- 它是逐事务写的。连接能给自己的语句设超时（见[语句超时](connections.zh-CN.md)），但没有"连接级的事务超时"：想要更长超时的脚本，就在开事务的地方自己写。

超时之后事务被回滚，事务内接下来的语句会报告原因。不要在事务里写很长的 `wait`：等待期间连接被占着，主体已经拿到的行锁也一直握着。

事务内的语句拿到的不是整个超时，而是**剩余的那部分**，所以最后一条语句不会比事务再多跑一整个超时。事务里只有这一个限制：连接自己的[语句超时](connections.zh-CN.md)管的是事务之外的语句。

## 它不做什么

- **它不是锁。** 事务决定一组语句到底做不做；两个脚本各自"读出来、改一改、写回去"仍然可能丢掉其中一次改动。各自看到什么，由数据库的隔离级别决定。
- **它不跨连接。** 事务开着时，`use connection` 和 `in connection` 拒绝切到别的连接，`disconnect` 也一样。两条连接就是两个事务，没有"一起提交"的承诺。
- **它不是万能的。** 事务内禁止 `register a database table`，因为在 MySQL 这类数据库上建表会提交事务。非数据库的语句也不会被撤销：已经发出的消息就是发出去了。
