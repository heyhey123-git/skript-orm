# 影响行数

**简体中文** | [English](affected-rows.md)

写入可以返回影响行数，供脚本判断操作结果。例如，不使用事务时，也可以据此检查条件更新是否生效：

```sk
update entities in table "accounts" with limit 1 and store affected rows in {_rows}:
    values:
        balance: {_balance} - {_amount}
    where all:
        id = {_from}
        balance = {_balance}

if {_rows} is 0:
    send "读余额的时候它已经被改掉了。" to console
```

## 这个子句

`store affected rows in {_rows}` 是可选子句，所有写入语句都支持：`insert one`、`insert many`、`insert ... if absent`、`update`、`upsert`、`delete`，无论是否带冒号。它必须用 `and` 连接，放在语句自身的参数之后，让 Skript 能确定前一个参数在哪里结束。后面仍可加 `and wait`，但不会改变等待行为。

```sk
delete entities from table "logs" with limit 500 and store affected rows in {_deleted}
insert one {_user::*} into table "archived_users" and store affected rows in {_rows}
upsert one entity in table "users" by id {_id} and store affected rows in {_rows}:
    values:
        name: "Alice"
```

目标必须是单个变量，例如 `{_rows}`，不能是 `{_rows::*}`。列表变量没有指定用于存储数字的键，其他表达式则无法赋值；这两种写法都会在解析脚本时被拒绝。

## 这个数字是什么

影响行数由后端返回：插入统计新增行，删除统计删除行，更新则按后端规则计数。匹配到行但没有改变值时，PostgreSQL 和 MongoDB 记一行，MySQL 记零行。因此，要用行数判断条件更新是否成功，应确保写入确实会改变值，如上例中金额不为零时。

| 语句 | MySQL | PostgreSQL | MongoDB |
| --- | --- | --- | --- |
| `insert one`、`insert many` | 写入的行数 | 写入的行数 | 写入的行数 |
| `update` | **改动**的行数 | 匹配的行数 | 匹配的行数 |
| `delete` | 删除的行数 | 删除的行数 | 删除的行数 |
| `upsert` | 插入 `1`、更新 `2`、值未改变时 `0` | 两种都是 `1` | 插入 `1`，更新时为匹配行数 |
| `insert ... if absent` | 插入 `1`，键已存在时 `0` | 同上 | 同上 |
| 驱动无法计数的批量操作 | 不返回数字 | 同上 | 不会发生 |

要在三种后端上一致地判断“是否插入了新行”，请用 `insert ... if absent`。`upsert` 不适合这一判断：MySQL 的 `2` 表示更新，另外两个后端则不区分插入和更新。

部分后端无法为批量操作提供逐行计数，此时**不返回数字**，而不是给出不准确的行数。

## 有数字、没数字、零

变量在**语句开始时就会被清空**，早于任何可能拒绝操作的检查；只有语句执行完成且能提供精确行数时，才会写入数字：

| 变量的内容 | 含义 |
| --- | --- |
| 一个数字 | 语句执行了，并按后端规则报告了影响行数 |
| 什么都没有 | 语句被拒绝、跳过或执行失败，或者后端无法计数 |

**零也是有效结果**，不等于缺失。它的含义取决于语句和后端，例如没有匹配行、键已存在，或 MySQL 更新时值未改变。用 `is set` 区分零与未返回行数：

```sk
if {_rows} is not set:
    send "没有任何语句报告写入了多少行。" to console
else if {_rows} is 0:
    send "没有匹配到行。" to console
```

上例中的零行提示适用于“零表示未匹配”的操作，不能直接套用到所有写入。

先清空变量，可以避免将上一次的数字误当成本次结果。写入会等待完成，因此下一条语句即可读取行数，无需额外等待。见 [错误与等待](errors-and-waiting.zh-CN.md)。

在[事务](transactions.zh-CN.md)中也一样：每条带此子句的语句都会先清空变量，被跳过的语句不会沿用上一条的数字。与之不同，`last database error` 会保留最初的错误，便于查询回滚原因。

事务结束不会清空这个变量，回滚也不会撤销变量赋值。因此，section 之后的 `{_rows}` 即使为 `1`，对应的数据库改动也可能已被回滚。使用事务中的行数前，请先检查 `last database error`。

## 一次安全的读改写

读取一个值，再据此写入新值，需要两条语句，期间可能发生其他写入。不使用事务时，可以让更新条件同时检查先前读到的值：

```sk
select one entity from table "accounts" and store the result in {_account::*}:
    where all:
        id = {_from}
set {_balance} to {_account::balance}

loop 3 times:
    update entities in table "accounts" with limit 1 and store affected rows in {_rows}:
        values:
            balance: {_balance} - {_amount}
        where all:
            id = {_from}
            balance = {_balance}

    if {_rows} is 1:
        stop
    # 未更新成功时，重新读取余额后重试；实际使用时还应检查数据库错误。
    select one entity from table "accounts" and store the result in {_account::*}:
        where all:
            id = {_from}
    set {_balance} to {_account::balance}
```

`where` 包含先前读到的余额，只有余额仍相同时更新才会匹配。在金额非零、语句成功的前提下，影响一行表示本次更新生效；零行表示条件未匹配，可能已有其他写入或该行已被删除。

有两点需要注意：

- MySQL 只统计实际改变值的行，因此金额为 `0` 时也可能返回零行。请在循环前拒绝零金额，而不是反复重试。
- 重试次数应有上限，否则面对持续更新的行，循环可能一直无法结束。需要多行一起生效时，应使用[事务](transactions.zh-CN.md)。
