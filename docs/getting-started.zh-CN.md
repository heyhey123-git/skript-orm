# 快速上手

**简体中文** | [English](getting-started.md)

从建立连接到写入、读取数据，本页会带你完成一份可运行的脚本。其余文档可在需要时查阅。

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

- `"MySQL"` 是数据库实现的类型名。jar 注册了四种类型：`"MySQL"`、`"PostgreSQL"`、`"MongoDB"` 与 `"JDBC"`，
  名称必须精确匹配，不能任意填写数据库**产品**名。见 [连接](connections.zh-CN.md) 中的“实现名称”一节。
- `url` 必填。不需要账号密码的数据库可将 `username` 与 `password` 设为空字符串。
- 这个 section 始终等待完成。连接成功后，同一 trigger 中的后续语句即可使用它。若另一个脚本在连接建立前执行数据库操作，会得到 `No database connected.`。通常将连接写在 `on load` 中，并注意脚本的加载顺序。
- 账号密码保存在脚本文件中，请限制该文件的读取权限。

## 2. 描述一张表

```sk
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
```

注册会在表不存在时创建它，并等待操作完成。它**不会**修改已有表，因此在脚本中新增列并不会改变数据库中已有的表结构。有关这一限制及完整的列语法，见 [表](tables.zh-CN.md)。

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

写入会等待完成，因此下一行执行时，`last database error` 反映的就是本次操作。数据库返回的错误也会记录在这里，而不只是输出到控制台。无论是否写 `and wait`，每条语句都会等待；见 [错误与等待](errors-and-waiting.zh-CN.md)。

`values` 中省略了 `id`，由数据库分配。如果后续操作需要这个 id，可以自行指定值并使用 `upsert`，见 [菜谱](cookbook.zh-CN.md)。

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

查询始终等待完成，后续语句可直接读取结果。每一列对应变量中的一个键，键名与列名相同。本例中，`{_user::id} is not set` 表示没有匹配的行。一般而言，值为 NULL 的列也不会设置对应的键，因此检查其他列时要区分这两种情况。详见 [读取行](reading.zh-CN.md)。

## 5. 修改与删除

```sk
update one entity in table "users" by id {_user::id} and wait:
    values:
        age: 26

delete one entity from table "users" by id {_user::id} and wait
```

这两种操作及使用 `where` 按条件修改、删除的写法，见 [更新与删除](updating-and-deleting.zh-CN.md)。示例中的删除没有主体，不写冒号；更新包含缩进的 `values` 块，需要冒号。见 [写入行](writing.zh-CN.md)。

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

其他脚本可以直接使用同一张表，因为连接属于整个服务端，而不只属于创建它的脚本。

**共享的默认连接应由一个脚本负责创建**：再次执行 `create a connection` 会替换默认连接，新连接中没有任何已注册的表，后续语句会报 `Table 'users' not found.`。这不会删除数据库中的表，只是新连接没有对应的注册信息。

其他脚本可以沿用已有连接，或用 `named` 创建自己的连接。见 [连接](connections.zh-CN.md)。

## 接下来读什么

| 你想 | 读 |
| --- | --- |
| 了解列的写法及注册表的限制 | [表](tables.zh-CN.md) |
| 批量写入，或“有则更新、无则插入” | [写入行](writing.zh-CN.md) |
| 过滤、分页、按 id 查询 | [读取行](reading.zh-CN.md) |
| 了解 `last database error` 何时被设置 | [错误与等待](errors-and-waiting.zh-CN.md) |
| 存储物品、位置、日期、NBT compound | [类型](types.zh-CN.md) |
| 排查没有报错却未生效的操作 | [排雷](troubleshooting.zh-CN.md) |
