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

- `"MySQL"` 是实现的名称。jar 里注册了四个：`"MySQL"`、`"PostgreSQL"`、`"MongoDB"` 与 `"JDBC"`，见下面的“实现名称”。
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

## 实现名称

引号里那个词是**类型名**，不是数据库产品的名字：它决定用哪套代码拼语句、读结果，而且是**大小写敏感的精确匹配**。jar 里注册了四个：

| 类型名 | 它带来什么 |
| --- | --- |
| `"MySQL"` | MySQL 方言——反引号标识符、`INSERT IGNORE`、`ON DUPLICATE KEY UPDATE`、更新与删除上的 `LIMIT`、`AUTO_INCREMENT`、`LIMIT` 分页——以及由插件自动找到的 MySQL 驱动（`com.mysql.cj.jdbc.Driver`，或更老的 `com.mysql.jdbc.Driver`）。脚本要用的几乎总是这一个。 |
| `"PostgreSQL"` | PostgreSQL 方言——`ON CONFLICT`、`EXCLUDED`、`GENERATED … AS IDENTITY`，以及因为 PostgreSQL 没有 `UPDATE ... LIMIT` 而改用 `ctid` 写出的行数限制——加上插件在首次启动时替它下载的驱动。 |
| `"MongoDB"` | MongoDB，通过插件为它下载的阻塞式 MongoDB Java 驱动访问。它底下没有 SQL，所以好几条语句的回答是刻意不同的，[兼容性](compatibility.zh-CN.md#mongodb) 一一列出；它自己的属性见下面一节。 |
| `"JDBC"` | 由**你自己**在 `driver` 属性里指定驱动，配一个写通用 SQL 的方言：`"双引号"` 标识符、取一行用 `LIMIT 1`、分页用 `LIMIT ? OFFSET ?`——这正是 MySQL、MariaDB、SQLite、PostgreSQL、H2 都接受的写法。凡是通用写法表达不了的——`insert ... if absent`、`upsert ... by id`、写操作加 limit、`auto increment`——它宁可拒绝也不猜。 |

驱动从哪来，区分的是这几种类型。`"PostgreSQL"` 与 `"MongoDB"` 指的是本插件会替你取好、备好的驱动；`"JDBC"`
指的是必须已经在服务端 classpath 上的类，这也正是它的用途。要连 SQLite 用的就是后者，因为 Paper 自带它的驱动：

```sk
create a connection to database "JDBC" with properties:
    driver: "org.sqlite.JDBC"
    url: "jdbc:sqlite:plugins/myplugin/data.db"
```

只有 `"JDBC"` 需要你给出类名，四种类型里没有任何一种的驱动会打进这个 jar。`"PostgreSQL"` 或 `"MongoDB"`
连接不需要类名，什么都不需要：两个驱动都会在首次启动时取来，[兼容性](compatibility.zh-CN.md#jar-里有什么)
讲了这件事，也讲了连不上它来源镜像的服务端该做些什么。

Paper 自己带两个驱动（MySQL Connector/J 与 SQLite 的）：

- **MySQL。** 这个方言写 `"双引号"` 标识符，而 MySQL 在没开 `ANSI_QUOTES` 时会把它当成字符串字面量，
  所以用 `"JDBC"` 连的 MySQL 先倒在标识符上，后面的问题都轮不到。连 MySQL 请写 `"MySQL"`。
- **SQLite。** 要连它靠的就是 `"JDBC"`，而且驱动已经在了：方言写出的东西 SQLite 全不反对，所以连一个文件
  就能用上这个类型支持的全部操作。方言拒绝的那些在这里同样被拒绝——没有 auto increment、没有
  `insert ... if absent`、没有 `upsert`、写操作不能加 limit——所以表的主键由脚本自己给。

所以 `"mysql"` 会被拒绝，报 `Database 'mysql' is not supported.`；`"MariaDB"`、`"SQLite"` 同样会被拒绝
——它们是产品，不是类型名。`"MongoDB"` 两样都是，正如 `"MySQL"`。连接**底下**连的是什么产品是一回事，
脚本**写**的是上面四个类型名之一，这是另一回事。[兼容性](compatibility.zh-CN.md) 里那份产品清单说的是前者。

## MongoDB 属性

MongoDB 连接用的还是同一个块，只是 `url` 的含义不同：

```sk
create a connection to database "MongoDB" with properties:
    url: "mongodb://localhost:27017"
    username: "admin"
    password: "p@ss:w/rd"
    database: "logs"
    auth database: "admin"
```

- `url` 可以只是 `host:port`，也可以是完整的连接串，以 `mongodb://` 或 `mongodb+srv://` 开头；完整的连接串会连
  选项一起原样交给驱动，例如 `mongodb://host:27017/logs?retryWrites=false`。
- `username` 与 `password` 和 SQL 实现一样是分开的两个属性，以字符串交给驱动，而不是拼进 url。所以含有 `@`、
  `:`、`/`、`%` 的密码就只是密码，不需要转义。
- `database` 指定要用的数据库，`mongodb://host:27017/mydb` 这样的 url 也指定了一个。写出来的 `database`
  优先于 url 里的那个；两边都没有时用 `skript-orm`。
- `auth database` 指定凭据所属的数据库，用于账号放在别处的服务端。默认就是正在使用的那个数据库。
- **MongoDB 的事务在本实现里还没有做。** MongoDB 本身是有多文档事务的——副本集从 4.0 起、分片集群从 4.2 起，
  单机服务端则直接拒绝——只是这条连接目前不会去开事务，所以 `database transaction` section 会失败，报
  `This database implementation does not support transactions.`；见 [兼容性](compatibility.zh-CN.md#mongodb)。

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

每个 section 都会先看连接。没有任何连接生效时，操作什么都不做，只报 `No database connected.`，这句会落进 `last database error`。当具名连接已经存在、却没有一条是默认时，报错会说清这一点并列出名字。见 [错误与等待](errors-and-waiting.zh-CN.md)。

## 账号凭据

属性写在脚本里，意味着凡是能读到脚本文件、或能执行会打印语法的 `/sk` 命令的人，都能看到账号与密码。两个习惯可省去后患：

- 只给数据库账号脚本真正需要的权限。
- 把连接单独放在一个脚本里。这样设权限时，你只需盯住那一个文件。

## 语句超时

每条语句默认有 **30 秒**。可以按连接改：

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
    statement timeout: 15
```

- `0` 表示不限，也就是驱动的默认行为：跑多久都等。
- 它存在的原因是：一条永远不结束的语句会一直占着池子里的一条连接，直到服务器重启，而没有别的东西会终结它。
- 它能保证的是**脚本不再等**，不是服务端停了：取消由驱动发起，MySQL 的做法是另开一条连接把那个查询杀掉。
- 它只管一条语句。从池里等一条空闲连接、以及 `commit` / `rollback` 等锁，都不在其中；那些由服务端自己的上限和 url 上的 `socketTimeout` 兜着。
- 在 MongoDB 上，同一个属性会变成驱动的 socket 读取超时，而这条连接的 `closeWaitTimeout` 是这个超时加五秒，和 SQL 那边完全一样。不同的是超时后两边各自怎么做：MySQL 从另一条连接把语句杀掉，MongoDB 的驱动则只是不再等响应——服务端会把已经收到的语句做完。无论哪边，这个属性限制的都是**脚本等多久**，而不是服务端做什么。
- MySQL 的 `innodb_lock_wait_timeout` 默认 50 秒，比这个长，所以等锁的语句通常先被这个超时取消，报出来的是超时而不是锁等待。想让数据库自己说话，就把这个值调到 50 以上。
- 这个值必须是整秒。别的写法会被拒绝，报 `Connection property 'statement timeout' must be a whole number of seconds, but was 'x'.`；负数则报 `Connection property 'statement timeout' must not be negative, but was -1.`。

事务内的语句拿到的是**事务剩余的时间**，而不是这整个超时。见 [事务](transactions.zh-CN.md)。

## 并发操作

一个连接内部维持着一个小连接池，操作之间不必排队。也正因如此，一次写入要等它结束之后，才对读取可见，因为两个操作可能跑在池里不同的连接上。从脚本的角度看，决定先后的是每条语句自己都会做的那次等待。

每条连接都有自己的一片连接池，所以脚本留着几条连接，服务端就运行着几片池子。
