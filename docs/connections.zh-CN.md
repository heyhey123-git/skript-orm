# 连接

**简体中文** | [English](connections.md)

连接由脚本建立，属于整个服务端。同一时刻只有一个当前数据库，其余操作都用它。

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

## 同一时刻只有一个

再建一个连接，会先断开当前这个，然后才连新的，新者取而代之：

- 新连接建立期间，旧连接已经不在了。
- 新连接若失败，服务端会落到**没有**当前数据库的境地，而不是留住旧的。此后每个操作都报 `No database connected.`，直到某次连接成功为止。
- 已注册的表跟着连接走。新连接开始时一张表都没注册，所以重连过的脚本需要重新注册，见 [表](tables.zh-CN.md)。
- 服务端禁用插件时，插件会关掉当前连接。

## 断开连接

```sk
disconnect from the current database
```

它异步执行，而下一行会等它结束，所以脚本可以先断开，再做别的事。单独一行 `disconnect` 就是完整语法。

## 没有连接时的操作

每个 section 都会先看连接。没有当前数据库时，操作什么都不做，只报 `No database connected.`。带 `and wait` 时这句会落进 `last database error`，不带就只进日志。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 账号凭据

属性写在脚本里，意味着凡是能读到脚本文件、或能执行会打印语法的 `/sk` 命令的人，都能看到账号与密码。两个习惯可省去后患：

- 只给数据库账号脚本真正需要的权限。
- 把连接单独放在一个脚本里。这样设权限时，你只需盯住那一个文件。

## 并发操作

一个连接内部维持着一个小连接池，操作之间不必排队。也正因如此，一次写入要等它结束之后，才对读取可见，因为两个操作可能跑在池里不同的连接上。从脚本的角度看，决定先后的是 `and wait`。
