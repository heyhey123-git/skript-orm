# 快速上手

**简体中文** | [English](getting-started.md)

这一页的最后是一个能存下一行、再读回来的脚本。文档其余部分都是你现在还用不到的参考。

前提是插件已经装好：见 [环境要求](../README.zh-CN.md#环境要求) 和 [安装](../README.zh-CN.md#安装)。

## 1. 建立连接

连接由脚本建立，不通过配置文件：

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

- `"MySQL"` 是实现的名称，本 jar 只打包了这一个；见 [兼容性](compatibility.zh-CN.md)。
- `url` 必填；`username` 与 `password` 可以是空字符串，用于不需要账号的服务器。
- 这个 section 一定会等，所以同一个 trigger 里它之后的语句跑在已经连上的库上。更早执行的**另一个脚本**
  会看到 `No database connected.` —— 这也是通常把连接写在 `on load` 里的原因。
- 账号密码就写在脚本文件里，所以这个文件的可见范围要和数据库账号一样收紧。

## 2. 描述一张表

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

注册会在表不存在时建表，并等到它存在为止。它**不会**修改已经存在的表，所以这里新增的列对已有该表的数据库
没有任何作用；这个坑和完整的列语法都在 [表](tables.zh-CN.md)。

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

这里能读到失败原因，靠的是 `and wait`。不写它时 section 会把工作交给后台、下一行立刻执行，此时
`last database error` 还是空的；见 [错误与等待](errors-and-waiting.zh-CN.md)。

`values` 里没有 `id`，因为它由数据库分配。如果之后需要它，就自己写一个值并用 `upsert`；见
[菜谱](cookbook.zh-CN.md)。

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

查询一定会等，所以它之后的语句已经拿到行了。行里的每一列就是变量的一个键，键名就是列名。
`{_user::id} is not set` 既是“没有匹配的行”的样子，也是“该列是 NULL”的样子：NULL 列不会写下它的键。
两种情况都在 [读取行](reading.zh-CN.md)。

## 5. 修改与删除

```sk
update one entity in table "users" by id {_user::id} and wait:
    values:
        age: 26

delete one entity from table "users" by id {_user::id} and wait:
```

按 `where` 匹配批量修改/删除的写法在 [更新与删除](updating-and-deleting.zh-CN.md)。

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

另一个脚本可以直接用同一张表：连接属于服务端，而不是建立它的那个脚本。见
[连接](connections.zh-CN.md)。

## 接下来读什么

| 你想 | 读 |
| --- | --- |
| 知道列能怎么写，以及建表**不会**做什么 | [表](tables.zh-CN.md) |
| 一次存很多行，或者“有则更新、无则插入” | [写入行](writing.zh-CN.md) |
| 过滤、分页、按 id 查 | [读取行](reading.zh-CN.md) |
| 搞清 `last database error` 到底什么时候被设置 | [错误与等待](errors-and-waiting.zh-CN.md) |
| 存物品、坐标、日期、NBT compound | [类型](types.zh-CN.md) |
| 查“为什么它悄无声息地什么都没做” | [排雷](troubleshooting.zh-CN.md) |
