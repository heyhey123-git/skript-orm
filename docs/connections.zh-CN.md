# 连接

**简体中文** | [English](connections.md)

脚本建立的连接可供整个服务端使用。你可以同时保持多条连接，并为每条语句选择要使用的连接。

## 建立连接

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
```

- `"MySQL"` 是实现名称。jar 注册了五种实现：`"MySQL"`、`"MariaDB"`、`"PostgreSQL"`、`"MongoDB"` 与 `"JDBC"`，见下面的“实现名称”。
- `url` 必填，`username` 与 `password` 可以是空字符串。
- 未识别的属性会导致连接失败，不会被悄悄忽略。例如，MySQL 不接受 `database: "mydb"`，会报 `Connection property 'database' is not read by database 'MySQL'. It reads: password, statement timeout, url, username.`。`statment timeout` 之类的拼写错误也会被发现。所有类型都接受 `url`、`username`、`password` 和 `statement timeout`；`"JDBC"` 还接受 `driver`，MongoDB 还接受 `database` 和 `auth database`。
- 连接 `"MySQL"`、`"MariaDB"` 或 `"PostgreSQL"` 时，把库名写在 URL 路径中，例如 `jdbc:mysql://localhost:3306/mydb`。`database` 属性只供 MongoDB 使用。
- 连接成功或失败后才会执行下一行。此处不接受 `and wait`。
- 不能在 `database transaction` 中创建连接，否则会报 `A connection cannot be created inside a database transaction. Roll it back first.`。

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
if last database error is set:
    send "连接失败: %last database error%" to console
    stop
```

这条连接没有名称，会成为**默认连接**。未另行指定连接的语句都会使用它；只用一个数据库的脚本通常无需切换连接。

## 实现名称

引号中的名称用于选择数据库实现，必须与以下五种名称之一完全一致，并区分大小写：

| 类型名 | 实现方式 |
| --- | --- |
| `"MySQL"` | MySQL 方言：反引号标识符、用于 `upsert` 的 `ON DUPLICATE KEY UPDATE`、更新与删除中的 `LIMIT`、`AUTO_INCREMENT` 及 `LIMIT` 分页。插件会自动查找服务端的 MySQL 驱动（`com.mysql.cj.jdbc.Driver` 或旧版 `com.mysql.jdbc.Driver`）。连接 MySQL 时应选用此类型。 |
| `"MariaDB"` | 与 `"MySQL"` 相同的方言（MariaDB 同样接受这些写法），驱动 MariaDB Connector/J 在首次启动时下载。url 必须写成 `jdbc:mariadb://`：该驱动会拒绝 `jdbc:mysql://`，MySQL 驱动也会拒绝 `jdbc:mariadb://`。连接 MariaDB 时应选用此类型。 |
| `"PostgreSQL"` | PostgreSQL 方言：`ON CONFLICT`、`EXCLUDED`、`GENERATED … AS IDENTITY`。由于 PostgreSQL 没有 `UPDATE ... LIMIT`，行数限制通过 `ctid` 实现。驱动在插件首次启动时下载。 |
| `"MongoDB"` | 通过插件下载的阻塞式 MongoDB Java 驱动访问 MongoDB，不使用 SQL。部分语句的行为因此不同，详见 [兼容性](compatibility.zh-CN.md#mongodb)；连接属性见下一节。 |
| `"JDBC"` | 由你在 `driver` 属性中指定驱动，使用通用 SQL 方言：`"双引号"` 标识符、单行查询的 `LIMIT 1`、分页的 `LIMIT ? OFFSET ?`。这种行数限制写法可用于 MySQL、MariaDB、SQLite、PostgreSQL 和 H2。不具备通用写法的操作不受支持，包括 `insert ... if absent`、`upsert ... by id`、写操作的 limit 及 `auto increment`。 |

连接 SQLite 时使用 `"JDBC"`。Paper 已提供 SQLite 驱动：

```sk
create a connection to database "JDBC" with properties:
    driver: "org.sqlite.JDBC"
    url: "jdbc:sqlite:plugins/myplugin/data.db"
```

只有 `"JDBC"` 需要填写 `driver`。MySQL 使用服务端已有的驱动；Paper 会在首次启动时下载 MariaDB、PostgreSQL 和 MongoDB 驱动。服务端无法访问下载镜像时，见[兼容性](compatibility.zh-CN.md#jar-里有什么)。

Paper 自带 MySQL Connector/J 和 SQLite 驱动：

- **MySQL：** `"JDBC"` 使用双引号包围标识符；MySQL 未启用 `ANSI_QUOTES` 时会将双引号内容视为字符串，导致语句失败。请使用 `"MySQL"`。
- **SQLite：** 使用 `"JDBC"`，无需另装驱动。此类型支持的读写操作均可用于 SQLite，但不支持 `auto increment`、`insert ... if absent`、`upsert` 或写操作的行数限制。主键值需要由脚本提供。

名称区分大小写：`"mysql"` 会报 `Database 'mysql' is not supported.`。`"SQLite"` 也不是已注册的名称；请使用 `"JDBC"`。支持的数据库产品见[兼容性](compatibility.zh-CN.md)。

## MongoDB 属性

MongoDB 使用相同的属性块，但 `url` 的含义不同：

```sk
create a connection to database "MongoDB" with properties:
    url: "mongodb://localhost:27017"
    username: "admin"
    password: "p@ss:w/rd"
    database: "logs"
    auth database: "admin"
```

- `url` 可以是 `host:port`，也可以是以 `mongodb://` 或 `mongodb+srv://` 开头的完整连接串。完整连接串会连同选项原样传给驱动，例如 `mongodb://host:27017/logs?retryWrites=false`。
- `username` 与 `password` 和 SQL 实现一样，分别以字符串传给驱动，不会拼入 URL。因此，密码中的 `@`、`:`、`/`、`%` 无需转义。
- `database` 指定要使用的数据库，优先于 `mongodb://host:27017/mydb` 这类 URL 中的数据库名。两处都未指定时使用 `skript-orm`。
- `auth database` 指定凭据所属的数据库，用于账号不在目标数据库中的情况。默认值为当前使用的数据库。
- **本插件尚不支持 MongoDB 事务。** 执行 `database transaction` 会报 `This database implementation does not support transactions.`。见[兼容性](compatibility.zh-CN.md#mongodb)。

## 给连接起名

加上 `named "..."`，即可按名称注册连接：

```sk
create a connection named "logs" to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/logs"
    username: "root"
    password: "123456"
```

- 具名连接按名称注册，**不影响其他连接**。
- 第一条成功建立的连接会成为默认连接，无论是否具名。
- 名称已存在时，新连接会替换同名连接并断开旧连接，其他名称的连接不受影响。重新运行同一段 `on load` 即可重连。
- 名称用于在脚本中引用连接，建议选择便于识别的名字，例如 `main`、`logs`、`archive`。

无名连接会替换默认连接。被替换的连接只有在**没有注册名称**时才会断开。例如，先创建 `"logs"`，再创建无名连接，`"logs"` 仍保持打开，只是不再用于未指定连接的语句。

## 一条语句用哪条连接

语句按以下优先级选择连接：

| 优先级 | 使用的连接 | 写法 |
|---|---|---|
| 1 | 所在的最内层连接作用域 | `in connection "logs":` |
| 2 | 当前事件切换到的连接 | `use connection "logs"` |
| 3 | 默认连接 | 不带名字的 `create a connection` |

三者都未指定连接时，语句不执行操作，并报 `No database connected.`。如果指定的名称不存在或连接已断开，语句会失败，**不会自动改用其他连接**。

## 临时切换

`in connection` 让一个块使用指定连接：

```sk
in connection "logs":
    insert one entity into table "entries" and wait:
        values:
            message: "写进日志库"
```

切换只在块内有效，适合从一个数据库读取、向另一个数据库写入：

```sk
in connection "archive":
    select many entities from table "entries" and store the results in {_entries::*}

in connection "logs":
    insert many entities into table "entries" from {_entries::*} and wait
```

名称不存在时，插件会跳过该块并报错，不会因拼写错误而误写默认数据库。

## 切换到这个事件结束

`use connection` 立即切换连接，并影响当前事件的后续操作：

```sk
command /newlog <text>:
    trigger:
        use connection "logs"
        insert one entity into table "entries" and wait:
            values:
                message: arg-1
```

它不执行数据库操作，因此无需等待，下一行即可使用新连接。在 `in connection` 块内，块指定的连接仍然优先；块内的 `use connection` 切换也会在块结束时撤销。

切换状态按事件保存，同一事件的多个处理器会共享它。如果需要彼此独立，请在各处理器中使用 `in connection`。

## 选择默认连接

```sk
make connection "logs" the default
```

此后，未指定连接的语句会使用 `"logs"`，直到默认连接再次改变。当前作用域中的连接不受影响，已经确定连接的语句也会继续使用原连接。

如果原默认连接没有注册名称，插件会将其关闭，因为脚本已无法再选用它。具名连接仍保持打开。语句会等待关闭完成，再执行下一行；事务运行期间不能更换默认连接。

## 断开连接

```sk
disconnect from the current database        # 当前生效的连接
disconnect from connection "logs"           # 指定的具名连接
disconnect from all connections             # 全部连接
```

第一种写法按前述优先级选择连接：先看 `in connection`，再看 `use connection`，否则断开默认连接。关闭完成后才会执行下一行。

断开仍被作用域引用的连接本身不会报错，但该作用域中的后续语句会失败，因为它们使用的连接已经关闭。

- 表的注册信息属于连接。新连接最初没有已注册的表，因此同一个表名可以分别在两条连接上注册。见 [表](tables.zh-CN.md)。
- 连接也接受[原始语句](raw-statements.zh-CN.md)——按原文发出的 SQL，不经过上述任何检查。它以该连接的账号身份、用该账号的权限运行。
- 插件被禁用时会关闭所有剩余连接；`disconnect from all connections` 会关闭所有已注册连接。

## 没有连接时的操作

数据库操作会先检查连接。没有可用连接时，操作不会执行，`last database error` 会记录 `No database connected.`。如果已有具名连接但没有默认连接，错误信息会列出可用名称。见[错误与等待](errors-and-waiting.zh-CN.md)。

## 账号凭据

连接属性写在脚本中，因此能读取脚本文件，或执行会输出语法内容的 `/sk` 命令的人，都能看到账号密码。建议：

- 只授予数据库账号脚本所需的权限。
- 将连接定义放在独立脚本中，便于统一管理凭据文件的访问权限。

## 语句超时

每条语句默认超时为 **30 秒**，可以按连接设置：

```sk
create a connection to database "MySQL" with properties:
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
    statement timeout: 15
```

- `0` 表示不限制，与驱动默认的无限等待行为一致。
- 超时用于避免未结束的语句持续占用池中的连接，否则这种占用可能一直持续到服务端重启。
- 它限制的是**脚本的等待时间**，并不保证数据库服务端已经停止执行。驱动负责取消语句，MySQL 会通过另一条连接终止查询；MariaDB 不需要第二条连接，其驱动会让服务端限制该语句，由服务端自行中止。
- 在 SQL 后端，超时只适用于单条语句，且从**取得连接后**开始计算。等待池中空闲连接由连接池另行限制，最多 **30 秒**，超过后报 `HikariPool-1 - Connection is not available, request timed out after 30000ms`。`statement timeout: 0` 不会取消这项限制。`commit` / `rollback` 的锁等待受数据库服务端自身限制及 URL 中的 `socketTimeout` 控制。
- 对 MongoDB，这个属性设置驱动的 socket 读取超时；连接的 `closeWaitTimeout` 与 SQL 实现一样，为该超时加五秒。区别在于超时后的处理：SQL 驱动会取消语句，而 MongoDB 驱动只停止等待响应，服务端仍会完成已收到的操作。因此，这个属性同样只限制**脚本等待多久**。它优先于 URL 设置：连接串中的 `socketTimeoutMS` 会被该属性或其 30 秒默认值覆盖，请在这里设置；设为 `statement timeout: 0` 则保留 URL 中的值。
- MySQL 的 `innodb_lock_wait_timeout` 默认为 50 秒，因此等待锁的语句通常会先触发本插件的语句超时，返回超时错误而非锁等待错误。若希望收到 MySQL 自身的锁等待错误，可将本属性设为大于 50 秒。MariaDB 同样由服务端中止语句，其 `innodb_lock_wait_timeout` 默认值也是 50 秒。
- 属性值必须是整数秒，否则会报 `Connection property 'statement timeout' must be a whole number of seconds, but was 'x'.`；负数会报 `Connection property 'statement timeout' must not be negative, but was -1.`。

事务中的语句使用**事务剩余的超时时间**，而不是这里设置的完整时长。见 [事务](transactions.zh-CN.md)。

## 并发操作

每条 SQL 连接内部维护一个最多包含**十条**数据库连接的 Hikari 连接池，允许多个操作并发执行。第十一个并发操作需要等待，最多等待上述 30 秒。连接池大小不能通过脚本属性调整。

不同操作可能使用连接池中的不同连接。同一个触发器中，前一条写入完成后才会执行后续读取；并发操作之间的数据可见性取决于事务隔离级别。

每条 SQL 连接都有独立的连接池，三条连接最多可占用三十条数据库连接。配置时请一并考虑数据库的 `max_connections` 限制。
