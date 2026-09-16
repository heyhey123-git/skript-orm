# 连接

**简体中文** | [English](connections.md)

连接由脚本建立，并且属于整个服务端。同一时刻只有一个“当前数据库”，其它所有操作都用它。

## 建立连接

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
```

- `"MySQL"` 是实现的名称；本 jar 只打包了这一个。见 [兼容性](compatibility.zh-CN.md)。
- `url` 必填；`username`、`password` 可以是空字符串。
- 块里其它字面量属性会原样交给实现，所以以后新增实现不必新增语法。
- 这个 section 一定会等：下一行执行时，连接要么已经可用、要么已经失败。这里既不需要、也不接受 `and wait`。

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

再建一个连接时，**会先断开当前连接**，然后才连新的：

- 新连接建立期间，旧连接已经不在了。
- 如果新连接失败，服务端会处于**没有**当前数据库的状态，而不是保留旧的。此后每个操作都会报
  `No database connected.`，直到某次连接成功。
- 已注册的表属于那一个连接。新连接开始时一张表都没注册，所以重连过的脚本需要重新注册表。见 [表](tables.zh-CN.md)。
- 服务端禁用插件时，插件会关闭当前连接。

## 断开连接

```sk
disconnect from the current database
```

它是异步执行的，并且下一行会等它结束，所以脚本可以先断开、再做别的事。单独一行 `disconnect` 就是完整语法。

## 没有连接时的操作

每个 section 都会先检查连接。没有当前数据库时，操作什么都不做并报 `No database connected.`；带 `and wait` 时
这会出现在 `last database error` 里，不带时只写日志。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 账号凭据

属性写在脚本里，意味着任何能读到脚本文件、或能执行会打印语法的 `/sk` 命令的人都能看到账号和密码。两个习惯：

- 只给数据库账号脚本真正需要的权限。
- 把连接单独放在一个脚本里，这样设权限时你只需要盯着那一个文件。

## 并发操作

一个连接内部维持着一个小连接池，所以操作之间不必排队。这也意味着：一次写入只有**结束之后**才对读取可见，
因为两个操作可能跑在池里不同的连接上。从脚本的角度看，决定先后顺序的是 `and wait`。
