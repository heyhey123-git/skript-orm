# 快速上手

**简体中文** | [English](getting-started.md)

这一页的终点，是一段能存下一行、再读回来的脚本。文档其余部分都是参考，留着你真用到时再翻。

前提是插件已经装好，见 [环境要求](../README.zh-CN.md#环境要求) 与 [安装](../README.zh-CN.md#安装)。

## 1. 建立连接

连接由脚本建立，配置文件里没有这一项：

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    if last database error is set:
        send "数据库连接失败: %last database error%" to console
        stop
```

- `"MySQL"` 是某个实现的类型名。jar 里注册了四个：`"MySQL"`、`"PostgreSQL"`、`"MongoDB"` 与 `"JDBC"`，
  而且是精确匹配，所以单是数据库**产品**的名字不在其中；见 [连接](connections.zh-CN.md) 里的“实现名称”一节。
- `url` 必填。`username` 与 `password` 可以是空字符串，供不需要账号的服务器使用。
- 这个 section 必定等待，所以同一 trigger 里它之后的语句，跑在已经连上的库上。若另一个脚本先跑，它会看到 `No database connected.`。连接写在 `on load` 里，正是为了免去这层先后之忧。
- 账号密码就写在脚本文件里，这个文件的可见范围，得跟数据库账号一样收着。

## 2. 描述一张表

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

注册会在表不存在时建表，并等它存在。它**不会**动已经存在的表，所以这里新增的列，对已有该表的数据库毫无作用。这个坑连同完整的列语法，都在 [表](tables.zh-CN.md)。

## 3. 写入一行

```sk
command /adduser <text> <integer>:
    trigger:
        insert one entity into table "users" and wait:
            values:
                name: arg-1
                age: arg-2
        if last database error is set:
            send "保存 %arg-1% 失败: %last database error%" to sender
            stop
        send "已保存 %arg-1%。" to sender
```

写入会等，所以下一行执行时 `last database error` 说的就是它：数据库自己拒绝的失败也在里面，而不只是打在控制台。每条语句都会等，写不写 `and wait` 都一样；见 [错误与等待](errors-and-waiting.zh-CN.md)。

`values` 里没有 `id`，它由数据库分配。日后需要它，就自己写一个值并改用 `upsert`，见 [菜谱](cookbook.zh-CN.md)。

## 4. 读回来

```sk
command /whois <text>:
    trigger:
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        if last database error is set:
            send "查询失败: %last database error%" to sender
            stop
        if {_user::id} is not set:
            send "没有叫 %arg-1% 的用户。" to sender
            stop
        send "name: %{_user::name}%, age: %{_user::age}%" to sender
```

查询必定等待，所以它之后的语句已经拿到行了。行中每一列就是变量的一个键，键名即列名。`{_user::id} is not set` 既是“没有匹配的行”的模样，也是“该列是 NULL”的模样，因为 NULL 列不会写下自己的键。两种情形都在 [读取行](reading.zh-CN.md)。

## 5. 修改与删除

```sk
update one entity in table "users" by id {_user::id} and wait:
    values:
        age: 26

delete one entity from table "users" by id {_user::id} and wait
```

按 `where` 批量修改或删除的写法，在 [更新与删除](updating-and-deleting.zh-CN.md)。删除没有正文，所以不写冒号；上面的更新有 `values` 要缩进，仍带冒号。见 [写入行](writing.zh-CN.md)。

## 完整脚本

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"
    if last database error is set:
        send "数据库连接失败: %last database error%" to console
        stop

    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
        age: int, nullable
    if last database error is set:
        send "建表失败: %last database error%" to console

command /adduser <text> <integer>:
    trigger:
        insert one entity into table "users" and wait:
            values:
                name: arg-1
                age: arg-2
        if last database error is set:
            send "保存 %arg-1% 失败: %last database error%" to sender
            stop
        send "已保存 %arg-1%。" to sender

command /whois <text>:
    trigger:
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        if last database error is set:
            send "查询失败: %last database error%" to sender
            stop
        if {_user::id} is not set:
            send "没有叫 %arg-1% 的用户。" to sender
            stop
        send "name: %{_user::name}%, age: %{_user::age}%" to sender
```

另一份脚本可以直接用同一张表，因为连接属于服务端，不属于建立它的那个脚本，见 [连接](connections.zh-CN.md)。

## 接下来读什么

| 你想 | 读 |
| --- | --- |
| 知道列能怎么写，以及建表**不会**做什么 | [表](tables.zh-CN.md) |
| 一次存很多行，或者“有则更新、无则插入” | [写入行](writing.zh-CN.md) |
| 过滤、分页、按 id 查 | [读取行](reading.zh-CN.md) |
| 弄清 `last database error` 究竟何时被设置 | [错误与等待](errors-and-waiting.zh-CN.md) |
| 存物品、坐标、日期、NBT compound | [类型](types.zh-CN.md) |
| 查“为什么它悄无声息，什么都没做” | [排雷](troubleshooting.zh-CN.md) |
