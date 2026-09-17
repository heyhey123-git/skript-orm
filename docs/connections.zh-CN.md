# 连接

**简体中文** | [English](connections.md)

连接由脚本建立，属于整个服务端。一个脚本可以同时留住好几条，每条语句自己决定用哪一条。

## 建立连接

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
```

- `"MySQL"` 是实现的名称，本 jar 只打包了这一个，见 [兼容性](compatibility.zh-CN.md)。
- `url` 必填，`username` 与 `password` 可以是空字符串。
- 块里其它字面量属性会原样交给实现。日后有实现需要更多参数，也不必新添语法。
- 这个 section 必定等待：下一行执行时，连接要么可用，要么已经失败。这里既不需要 `and wait`，也不接受它。

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
if last database error is set:
    send "连接失败: %last database error%" to console
    stop
```

这条连接没有名字。它会成为**默认连接**，也就是没有任何别的东西指定时语句所使用的那一条。所以只有一个库的脚本，完全不必关心下面这些。

## 给连接起名

加上 `named "..."`，连接就会记在这个名字下：

```sk
create a connection named "logs" to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/logs"
    username: "root"
    password: "123456"
```

- 具名连接登记在这个名字下，**不会碰任何别的连接**。
- 第一条连接成功的会成为默认连接，具名与否都一样。
- 用一个已经在用的名字建连接，替换的只是那一条：旧的断开，其它名字下的连接不受影响。把同一段 `on load` 再跑一遍，就是重连。
- 名字是脚本能够指向的东西。`main`、`logs`、`archive`，念得顺口就行。

不带名字的连接替换的是默认连接，这一点和以前一样。被替换的那条只有在**没有名字可以留住它**时才会断开，所以先建了 `"logs"`、再建一条无名连接的脚本，`"logs"` 仍然活着，只是不再是无限定语句的落点。

## 一条语句用哪条连接

语句会按顺序问三个问题，取第一个有答案的：

| 顺序 | 答案 | 写法 |
|---|---|---|
| 1 | 它所在的最内层作用域 | `in connection "logs":` |
| 2 | 本事件切换到的连接 | `use connection "logs"` |
| 3 | 默认连接 | 不带名字的 `create a connection` |

三个都没有答案时，语句什么都不做，只报 `No database connected.`。写错的名字、已经断开的连接，都不是可以退而求其次的情况：语句会在它被指向的那条连接上失败，而不会悄悄改用另一条。

## 临时切换

`in connection` 让一个块用指定的连接：

```sk
in connection "logs":
    insert one entity into table "entries" and wait:
        values:
            message: "写进日志库"
```

切换只在这个块内有效，正因如此，从一个库读、往另一个库写才写得清楚：

```sk
in connection "archive":
    select many entities from table "entries" and store the results in {_entries::*}

in connection "logs":
    insert many entities into table "entries" from {_entries::*} and wait
```

名字不存在时，块会被跳过并报错，所以打错字不会变成一次写向默认库的操作。

## 切换到这个事件结束

`use connection` 会立即切换，并影响当前事件余下的部分：

```sk
command /newlog <text>:
    trigger:
        use connection "logs"
        insert one entity into table "entries" and wait:
            values:
                message: arg-1
```

它不做任何数据库工作，所以和这里其它语句不同，它不等待：紧接着的下一行就已经在用它了。写在 `in connection` 块里时，块仍然优先，而块结束时，块内写的 `use connection` 会一并撤销。

同一个事件上的多个处理器共用这个切换，因为它按事件保存。介意的话，在每个处理器里各写一个 `in connection`。

## 选择默认连接

```sk
make connection "logs" the default
```

此后无限定语句都用 `"logs"`，直到有别的连接成为默认。脚本当前所在的连接不受影响：已经解析出连接的语句就继续用它。

## 断开连接

```sk
disconnect from the current database        # 当前生效的连接
disconnect from connection "logs"           # 某一条具名连接
disconnect from all connections             # 全部
```

第一种写法和其它语句一样走解析：写在 `in connection "logs":` 里，关掉的是 `"logs"`；写了
`use connection "logs"` 之后，关掉的也是 `"logs"`；两者都没有时，关掉的是默认连接。它异步执行，而下一行
会等它结束，所以脚本可以先断开，再做别的事。

断开一条仍被作用域指着的连接不算错误。只是那个作用域里的语句从此会失败，因为它们解析到的那条连接已经
关闭了。

- 已注册的表跟着连接走。另一条连接开始时一张表都没注册，所以同一个表名在两条连接上各注册一次并不冲突。见 [表](tables.zh-CN.md)。
- 服务端禁用插件时，插件会关掉每一条连接。

## 没有连接时的操作

每个 section 都会先看连接。没有任何连接生效时，操作什么都不做，只报 `No database connected.`。带 `and wait` 时这句会落进 `last database error`，不带就只进日志。当具名连接已经存在、却没有一条是默认时，报错会说清这一点并列出名字。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 账号凭据

属性写在脚本里，意味着凡是能读到脚本文件、或能执行会打印语法的 `/sk` 命令的人，都能看到账号与密码。两个习惯可省去后患：

- 只给数据库账号脚本真正需要的权限。
- 把连接单独放在一个脚本里。这样设权限时，你只需盯住那一个文件。

## 并发操作

一个连接内部维持着一个小连接池，操作之间不必排队。也正因如此，一次写入要等它结束之后，才对读取可见，因为两个操作可能跑在池里不同的连接上。从脚本的角度看，决定先后的是 `and wait`。

每条连接都有自己的一片连接池，所以脚本留着几条连接，服务端就运行着几片池子。
