# 菜谱

**简体中文** | [English](cookbook.md)

这里收集了几种常用写法，可以复制后按需调整。所用语法都在文档的其他页面中介绍过。

[`docs/examples/cookbook.sk`](examples/cookbook.sk) 提供了配套示例，由人工维护，并非从本页自动提取，也不保证逐字同步。

CI 会在真实服务端上检查示例能否解析，但不执行命令，因此不验证运行结果。示例放在 `command /example-…` 中，使用英文消息；所有示例文件会加载到同一台测试服，所以命令名必须唯一。

## 知道自己刚建的行的 id

插件不返回自动生成的 id，可以由脚本分配这个值，再用 `upsert` 保存：

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
        # 由脚本计数器分配 id。计数器不在多台服务器间共享；多服环境应由数据库分配 id，
        # 再按其他列查回该行。
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

将 `uuid` 列设为主键，用玩家 UUID 标识行，再通过 `upsert` 为每位玩家维护一行数据：

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
        if {_row::uuid} is not set:
            send "你还没有对应的行。" to sender
            stop
        set {_age} to {_row::age}
        if {_age} is not set:
            set {_age} to 0
        set {_new-age} to {_age} + arg-1
        update one entity in table "players" by id uuid of player and wait:
            values:
                age: {_new-age}
        if last database error is set:
            send "更新失败: %last database error%" to sender
            stop
        send "你的年龄现在是 %{_new-age}%。" to sender
```

`by id` 使用注册表时声明的主键，这里是上例中的 `uuid`。即使表中同时有 `id` 和 `uuid` 两列，也以主键为准。

只有 `values` 块中列出的列会被写入，其余列保持不变。

读取和更新是两次独立操作。若其他脚本在两者之间修改年龄，后一次写入可能覆盖那次修改，造成更新丢失。需要处理并发时，可增加版本列，改用同时匹配主键和旧版本的条件更新，并在同一次更新中写入新版本；通过 `store affected rows` 检查是否更新了一行。若为 0，先重新读取，再决定重试或提示冲突。

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

`select many` 的结果结构可以直接交给 `insert many`，无需重新整理。插入完成后，脚本才会继续执行删除；1.2 中所有数据库语句都会等待完成，示例保留的 `and wait` 不改变行为。

这三步不是一个事务，失败时不会整体回滚。执行期间应避免并发修改相关数据：删除会重新匹配 `active = false`，可能删掉读取后才符合条件、尚未归档的行；读取后发生的修改也不会自动进入归档。请在暂停相关写入的维护时段使用这段示例。

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

目前没有计数查询，可以逐页读取，遇到空页就停止；本例还设置了 100 页的上限：

```sk
set {_page} to 1
while {_page} <= 100:
    select page {_page} with size 50 from table "users" and store the results in {_page-rows::*}:
        where all:
            active = true
    # 按行号遍历，以主键是否存在判断还有没有下一行。每一行都是一个子列表。
    # 不用其他列判断，因为 NULL 列没有对应的键，会让遍历提前结束。
    # 遍历后 {_row} 仍为 1，说明当前页为空。
    set {_row} to 1
    while {_page-rows::%{_row}%::id} is set:
        send "%{_page-rows::%{_row}%::name}%" to console
        add 1 to {_row}
    if {_row} is 1:
        exit loop
    add 1 to {_page}
```

`size of {_page-rows::*}` 只统计第一层的值，每行却是下一层的子列表，所以不能用它统计行数。上例按行号遍历，直到下一行的主键未设置。

循环上限用于防止脚本无限翻页。如果到第 100 页仍有数据，本次遍历会在这里停止，不会继续读取剩余行。

分页取的是主键顺序上的偏移，不是快照。两次读取之间插入或删除行，可能使后续行的位置变化，造成重复或遗漏，因此这段写法适合遍历期间没有写入的表。主键游标（`where all: id > {_last}`）需要配合稳定的主键排序；`select many` 不提供 `ORDER BY`，仅加游标条件并不能完整替代分页。见 [读取行](reading.zh-CN.md)。

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

保存的是命令执行时的 compound 快照，之后修改物品不会影响这一行。读回的 compound 可直接用于 SkBee 语法，也可以转为 SNBT 文本检查内容。见 [类型](types.zh-CN.md)。

## 分批删除旧行

```sk
command /prune:
    trigger:
        set {_cutoff} to now - 30 days
        set {_pruned} to 0
        loop 10 times:
            delete entities from table "logs" with limit 500 and store affected rows in {_deleted} and wait:
                where all:
                    created < {_cutoff}
            if last database error is set:
                send "清理失败: %last database error%" to console
                exit loop
            add {_deleted} to {_pruned}
            if {_deleted} is less than 500:
                exit loop
        send "清理了 %{_pruned}% 行旧数据。" to sender
```

分批处理有助于缩短单条语句占用表的时间。删除操作会返回影响行数；某批不足 500 行时，脚本就停止，不必跑满十次，并能报告累计删除的行数。若十批都删满，本次最多处理 5,000 行，剩余数据留待下次清理。

## 让第二个脚本共用这张表

连接由服务端上的脚本共享，建议指定一个脚本负责建立连接和注册表：

```sk
# database.sk
on load:
    set {database::ready} to false    # 先清除上次运行留下的就绪状态
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

第二个脚本也可以建立连接，但这样会替换当前连接，而新连接中还没有注册任何表。让同一个脚本统一负责，可以避免意外替换。见 [连接](connections.zh-CN.md)。

`{database::ready}` 是全局变量，重启后仍会保留。加载时先将它设为 false，才能避免连接失败后沿用上次的 `true`。这样，第二个脚本会提示数据库尚未准备好，而不是继续执行并报出 `No database connected.`。
