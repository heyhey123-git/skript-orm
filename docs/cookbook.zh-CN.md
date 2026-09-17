# 菜谱

**简体中文** | [English](cookbook.md)

整段可抄的写法，只使用文档其它页面里出现过的语法。

## 知道自己刚建的行的 id

插件不会把生成的 id 交回来，所以让脚本自己管这个值、并用 `upsert`：

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    register a database table "users":
        id: bigint, primary key, not null
        name: string(64), not null

command /adduser <text>:
    trigger:
        # 计数器把 id 留在脚本里。它不会在多台服务端之间共享，所以多服方案应该让数据库分配 id、
        # 之后再按别的列把行找回来。
        if {users::next-id} is not set:
            set {users::next-id} to 0
        add 1 to {users::next-id}
        upsert one entity in table "users" by id {users::next-id} and wait:
            values:
                name: arg-1
        if last database error is set:
            send "保存 %arg-1% 失败: %last database error%" to sender
            stop
        send "已把 %arg-1% 存为 id {users::next-id}。" to sender
```

## 每个玩家一行

把 `uuid` 列作为主键，玩家就成了身份，`upsert` 于是保证每人一行：

```sk
on join:
    upsert one entity in table "players" by id uuid of player and wait:
        values:
            name: name of player
            last_seen: now
    if last database error is set:
        send "保存你的数据失败: %last database error%" to console
```

## 读出来、改一改、存回去

```sk
command /addage <integer>:
    trigger:
        select one entity from table "players" and store the result in {_row::*}:
            where all:
                uuid = uuid of player
        if {_row::id} is not set:
            send "你还没有对应的行。" to sender
            stop
        set {_age} to {_row::age}
        if {_age} is not set:
            set {_age} to 0
        set {_new-age} to {_age} + arg-1
        update one entity in table "players" by id {_row::id} and wait:
            values:
                age: {_new-age}
        if last database error is set:
            send "更新失败: %last database error%" to sender
            stop
        send "你的年龄现在是 %{_new-age}%。" to sender
```

只有 `values` 块里出现的列会被写入，行里其余部分不受影响。

## 在两张表之间搬数据

```sk
select many entities from table "users" and store the results in {_rows::*}:
    where all:
        active = false

insert many {_rows::*} into table "archived_users" and wait
if last database error is set:
    send "归档失败: %last database error%" to console
    stop

delete entities from table "users" and wait:
    where all:
        active = false
```

`select many` 填出来的变量本来就是 `insert many` 要的形状，不需要重新整理。插入写了 `and wait`，删除就不会抢在
它前面执行。

## 在脚本里拼好多行再插入

```sk
set {_rows::1::name} to "Alice"
set {_rows::1::age} to 25
set {_rows::2::name} to "Bob"
set {_rows::2::age} to 30

insert many {_rows::*} into table "users" and wait
```

行号从 1 开始，和读取产生的键完全一致。

## 逐页遍历

没有计数查询，所以脚本一直翻页，直到某一页为空：

```sk
set {_page} to 1
while {_page} <= 100:
    select page {_page} with size 50 from table "users" and store the results in {_page-rows::*}:
        where all:
            active = true
    # 按行号自己往下走：每一行都是子列表，所以下一行的某一列没有值，就是行号到头了。
    # 走完之后 {_row} 还是 1，说明这一页本身就是空的。
    set {_row} to 1
    while {_page-rows::%{_row}%::name} is set:
        send "%{_page-rows::%{_row}%::name}%" to console
        add 1 to {_row}
    if {_row} is 1:
        exit loop
    add 1 to {_page}
```

`size of {_page-rows::*}` 数不出行数：它只数第一层的值，而每一行都是下一层的子列表，所以上面按行号自己往前走，走到下一行的列没有值为止。

循环上界是保险。没有计数查询时，正是它拦住了“条件一直匹配、脚本翻页不止”的可能。

## 存下物品的 NBT

需要 SkBee 和一个 `nbtcompound` 列：

```sk
register a database table "tools":
    id: bigint, primary key, auto increment, not null
    data: nbtcompound, nullable

command /savetool:
    trigger:
        insert one entity into table "tools" and wait:
            values:
                data: nbt of player's tool
        if last database error is set:
            send "保存工具失败: %last database error%" to sender
            stop
        send "已保存。" to sender
```

存下的是命令执行那一刻的 compound：之后改动物品不会改写这一行。读回来的是 SkBee 语法能直接使用的 compound，
想检查里面有什么，用它的 SNBT 文本最省事。见 [类型](types.zh-CN.md)。

## 分批删除旧行

```sk
command /prune:
    trigger:
        set {_cutoff} to now - 30 days
        loop 10 times:
            delete entities from table "logs" with limit 500 and wait:
                where all:
                    created < {_cutoff}
            if last database error is set:
                send "清理失败: %last database error%" to console
                exit loop
        send "最多清理了 5000 行旧数据。" to sender
```

有界批次能让一条语句不至于长时间占着表。删除不会报告删掉了几行，所以循环上界就是实际能用的终止条件：这段每次最多删 `10 × 500` 行，过一会儿再跑一次，接着往下删便是。

## 让第二个脚本共用这张表

连接属于服务端，所以应该只有一个脚本负责建立它并注册表：

```sk
# database.sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
    if last database error is set:
        send "数据库还没准备好: %last database error%" to console
        stop
    set {database::ready} to true
```

```sk
# users.sk
command /whois <text>:
    trigger:
        if {database::ready} is not true:
            send "数据库还没准备好。" to sender
            stop
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        # ...
```

第二个脚本也去连接并不算错误，但它会替换掉当前连接、并且一开始没有任何已注册的表，所以这件事还是交给那个负责的
脚本。见 [连接](connections.zh-CN.md)。
