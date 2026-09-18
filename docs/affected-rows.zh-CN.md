# 影响行数

**简体中文** | [English](affected-rows.md)

写入可以报出它动了多少行，脚本可以拿这个数字做判断。没有事务也能写条件更新，靠的就是它：

```sk
update entities in table "accounts" with limit 1 and store affected rows in {_rows} and wait:
    values:
        balance: {_balance} - {_amount}
    where all:
        id = {_from}
        balance = {_balance}

if {_rows} is 0:
    send "读余额的时候它已经被改掉了。" to console
```

## 这个子句

`store affected rows in {_rows}` 是可选的，每条写入语句都能带：`insert one`、`insert many`、`insert ... if absent`、`update`、`upsert`、`delete`，section 形式和不带冒号的形式都一样。它和 `and wait` 一样带一个 `and`，写在语句自己的参数之后、`and wait` 之前——那个 `and` 不是装饰，它让 Skript 知道前面那个参数到哪里为止。

```sk
delete entities from table "logs" with limit 500 and store affected rows in {_deleted} and wait
insert one {_user::*} into table "archived_users" and store affected rows in {_rows} and wait
upsert one entity in table "users" by id {_id} and store affected rows in {_rows} and wait:
    values:
        name: "Alice"
```

目标必须是单个变量，写 `{_rows}` 而不是 `{_rows::*}`：列表变量会把数字塞进一个没人指定的键里，而表达式根本没法被写入。这两种情况都会在解析脚本时就报错。

## 这个数字是什么

它是数据库自己报出的“写入了多少行”：插入加了多少行、删除去掉多少行、更新写了多少行。至于哪些更新算数，是数据库的说法，不是这个插件的规定——更新匹配到一行但值没变，PostgreSQL 和 MongoDB 算一行，MySQL 算零行。所以只在语句确实改动了值时拿这个数字做比较，上面的例子就是这么写的。

有些后端在批量操作里无法逐行报数。这种情况报的是**没有数字**，而不是一个错的数字。

## 有数字、没数字、零

变量在**语句开始时就被清空**，早于任何可能拒绝它的检查；只有在语句跑完、并且能给出精确行数之后才会被写入：

| 变量的内容 | 含义 |
| --- | --- |
| 一个数字 | 语句执行了，影响了这么多行 |
| 什么都没有 | 没有任何语句给出答复：语句被拒绝或被跳过、失败了、后端数不出来，或者写的时候没带 `and wait` |

**零是一个正经答案**，不是缺失：语句执行了，只是没匹配到任何行。这正是它有用的地方，而区分这两种情况靠 `is set`：

```sk
if {_rows} is not set:
    send "没有任何语句报告写入了多少行。" to console
else if {_rows} is 0:
    send "没有匹配到行。" to console
```

先清空变量，是为了不让上一个语句留下的数字被当成这一条语句的答案。而不带 `and wait` 时，语句会把它清空、然后永远不再写它；所以哪怕活儿已经干完，这个变量读起来仍然是“没有答复”——等到有数字可给的时候，脚本早就往下走了。见 [错误与等待](errors-and-waiting.zh-CN.md)。

在[事务](transactions.zh-CN.md)里每条语句都会等待，所以这个子句在事务里一样可用。变量在事务里仍然是逐条语句清空的：被事务跳过的语句没有给出答复，留着上一条语句的数字就变成了假话。事务里特意保留下来的是 `last database error`，为的是回滚的原因还能被读到。

## 一次安全的读改写

先读一个值、再按它写入，这是两条语句，其间可能有别的写入插进来。没有事务时，办法是让写入本身去核对它所依据的那个值：

```sk
select one entity from table "accounts" and store the result in {_account::*}:
    where all:
        id = {_from}
set {_balance} to {_account::balance}

loop 3 times:
    update entities in table "accounts" with limit 1 and store affected rows in {_rows} and wait:
        values:
            balance: {_balance} - {_amount}
        where all:
            id = {_from}
            balance = {_balance}

    if {_rows} is 1:
        stop
    # 没人匹配刚才读到的余额，说明这一行动了：重新读一次，再试。
    select one entity from table "accounts" and store the result in {_account::*}:
        where all:
            id = {_from}
    set {_balance} to {_account::balance}
```

`where` 里重复了脚本读到的那个值，所以只有当这一行仍然是那个值时，更新才可能匹配。影响一行，说明正是这条语句改动了它；零行，说明别人先动手了。

有两点值得记住：

- 要让 MySQL 把它算进去，写入必须真的改动这一行，所以转账金额为 `0` 看起来就像抢输了。与其重试，不如在循环之前就拒绝零金额。
- 重试的圈数要有上限。面对一个不断被写入的行，不肯放弃的循环可以一直转下去；而当需要一起变动的不是一行，而是好几行时，答案是一笔[事务](transactions.zh-CN.md)。
