# skript-orm

<!-- 此页也用作 wiki 首页；图片使用绝对路径，避免指向 wiki 中不存在的文件。 -->
![skript-orm：适用于 Skript 的 ORM](https://raw.githubusercontent.com/heyhey123-git/skript-orm/master/docs/assets/banner.png)

适用于 Skript 的 ORM。定义好表结构，就能用 Skript 语法读写数据，无需手写 SQL。

**简体中文** | [English](README.md)

## 先说效果

```sk
on load:
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/mydb"
        username: "root"
        password: "123456"

    register a database table "users":
        id: bigint, primary key, auto increment, not null
        name: string(64), not null
        age: int, nullable

command /whois <text>:
    trigger:
        select one entity from table "users" and store the result in {_user::*}:
            where all:
                name = arg-1
        if last database error is set:
            send "查询失败: %last database error%" to sender
            stop
        send "name: %{_user::name}%, age: %{_user::age}%" to sender
```

```sk
command /adduser <text> <integer>:
    trigger:
        insert one entity into table "users" and wait:
            values:
                name: arg-1
                age: arg-2
        if last database error is set:
            send "写入失败: %last database error%" to console
```

两段示例都不需要 SQL。插件负责生成语句，在服务端线程之外执行，再以普通 Skript 变量和值返回结果。

## 环境要求

| | |
| --- | --- |
| **Paper** | 26.2 或更高。本插件针对该版本系列编译。 |
| **Skript** | 2.16.2 或更高。版本过低时，插件会自行禁用，并在控制台说明原因。 |
| **MySQL** | 已包含对应实现，并通过 MySQL 8 测试。Paper 自带驱动，无需另行安装。 |
| **PostgreSQL** | 已包含对应实现，并在 CI 中通过真实数据库测试。驱动会在首次启动时下载到服务端的 `libraries/`；见 [兼容性](docs/compatibility.zh-CN.md#jar-里有什么)。 |
| **MongoDB** | 已包含对应实现，并在 CI 中通过 MongoDB 8 测试。驱动与 PostgreSQL 一样，会在首次启动时下载。 |
| **SkBee** | 可选，仅 `nbtcompound` 列需要。脚本中的 NBT compound 也由 SkBee 提供。 |

## 安装

1. 从 releases 页面下载 `skriptorm-<version>.jar`，或自行构建，见 [CONTRIBUTION.zh-CN.md](CONTRIBUTION.zh-CN.md)。
2. 将 jar 与 Skript 一起放入 `plugins/`。
3. 启动一次服务端，再在脚本中建立连接、注册表，用 `/sk reload` 加载。连接和表都由脚本定义，无需修改配置文件。

## 几条规矩

- **支持多条连接。** `create a connection` 建立默认连接，`named "logs"` 创建具名连接，`in connection "logs":` 或 `use connection "logs"` 指定语句使用的连接。
- **每种数据操作都有对应的 Skript 语法。** 写入、读取、更新、删除各有专用语法，包含 `values` 或 `where` 主体时使用 section。所有语句都会等待完成。
- **一个事务就是一个 section。** `database transaction:` 在主体结束时提交，其中的语句失败时回滚，运行期间独占一条连接。
- **写操作可以返回影响行数。** `and store affected rows in {_rows}` 保存行数，具体含义取决于操作和数据库；条件更新可借此检测并发修改。见 [影响行数](docs/affected-rows.zh-CN.md)。
- **错误可在脚本中读取。** 每条语句都会等待完成，所以下一行执行时，`last database error` 已记录本次操作的错误；操作成功时则未设置。

## 性能与边界

先说结论：日常读写不会拖慢服务端；大到存不下的请求会被拒绝而不是硬跑。拒绝是保护，不是插件的缺陷——玩家在意的是卡顿，不是一条报错。

下面每个数字都来自它旁边写着的那台机器，一次只跑一个服务端；写成区间的地方，就是当时真的有那么大的波动。不带机器的数字不算数字：同一个 CI 标签的两次运行可能落在不同型号的处理器上。

| 测的是什么 | 结果 | 机器 |
| --- | --- | --- |
| 写入 5000 行（两个上限都落在的尺寸） | 冷启动超出一次 tick 70 至 90 毫秒；热身后一次运行约 30 毫秒，另一次完全没有超出 | 临时 Paper 26.2 服务端，AMD Ryzen 5 5600X，JDK 25 |
| 写入 100 行、10000 行 | 分别超出一次 tick 约 10 毫秒、100 至 110 毫秒 | 同一台服务端 |
| 读取 100 至 5000 行 | 没有一次拉长 tick，而且要的行全部存下 | 同一台服务端 |
| 读取 10000 行 | 被拒绝：结果变量被清空，`last database error` 说明上限 | 同一台服务端 |
| 走通用 JDBC 通道的 5000 行 `insert many` | 9.960 ± 0.384 毫秒；三台 CI 主机上为 6.1 至 10.0 毫秒 | Ryzen 5 5600X；AMD EPYC 9V74；Intel Xeon Platinum 8573C |
| 真实服务器为这 5000 行记下多少条语句 | MySQL 5038 条（逐行一条）；MariaDB 2 条（一条多行插入）；PostgreSQL 数不出来 | 同一次 CI 运行：MySQL 与 MariaDB 在 Intel Xeon Platinum 8370C 上，PostgreSQL 在 AMD EPYC 7763 上 |

最后一行是驱动的行为，不是插件的：同一份 SQL、同一个服务器，插件交给驱动的是一个批次，服务器收到的是另一种形状。这也正是本项目两层都数、并且以条数作准的原因——同一台共享机器上两次完全相同的运行，墙钟能差一两成，条数却纹丝不动。[基准与压力测试](docs/benchmarking.zh-CN.md) 记录了每个数字是怎么量出来的，以及那条规矩：**条数和分配次数算结论，可以卡住一次合并；时间只做报告，永远不卡**。这些都不是对你那台机器的承诺。

**边界，同样用大白话说。** 一次读取最多存 5000 行，超过就什么都不存，`last database error` 会说清楚。一条写入最多绑定 30000 个值，也就是六列的 5000 行；超过预算的批次不会被拒绝，而是被拆成多条语句发出，每一行都写进去。大到离谱的请求会被拒绝而不是硬跑，这是刻意的——服务端一顿一顿，才是玩家注意到的事。

### 这些数字为什么没有

- **没有 tick 百分位。** 观察者在脚本这一侧：它能报出语句跑在哪个 tick 上、那个窗口里最长的一个 tick 有多长，但它的时钟只到 10 毫秒，还会被 trigger 恢复时所在的 tick 量化，下限就是一个 tick。要对 tick 时长算百分位，得往 tick 循环里放一个观察者，那是服务端里的插件，不是脚本。所以 MSPT 百分位是缺的，不是编出来的。
- **没有 PostgreSQL 的语句条数。** 数据库自己收到多少条，才是驱动之间差别所在。MySQL 系报得出来，就在上表里；PostgreSQL 自带的视图只数行数和事务数，没法把行归到语句上，所以那一半写“不可得”，而不是编一个。

**有一个坑，最好在这里看到，而不是在生产环境里遇到。** Connector/J 用默认设置时，批量插入是每行一条语句发出去的，所以同一段插入在 MySQL 上比在 PostgreSQL、MariaDB 上慢得多。`insert many` 的那个基准——两列 5000 行、重复二十遍，每个后端同一份脚本、同一张表定义——实测 MySQL 17.04 秒，PostgreSQL 与 MariaDB 各 1.99 秒；每批 5000 条语句是驱动发的，不是这个插件发的。让驱动改写批次的选项也不免费：插入变快了，影响行数却没了。细节见 [排雷](docs/troubleshooting.zh-CN.md) 里的“把 `insert many` 改快之后，`affected rows` 就不存了”一节。

## 文档

| 页面 | 内容 |
| --- | --- |
| [快速上手](docs/getting-started.zh-CN.md) | 从空脚本开始，保存第一行数据。 |
| [连接](docs/connections.zh-CN.md) | 连接属性、具名连接、切换与断开连接。 |
| [表](docs/tables.zh-CN.md) | 列语法、全部类型、主键与修饰符，以及注册表**不会**做的事。 |
| [原始语句](docs/raw-statements.zh-CN.md) | 自己写的语句：跳过了什么、仍然保证什么，以及为什么它是 unsafe 的。 |
| [写入行](docs/writing.zh-CN.md) | 插入一行或多行、从变量插入、upsert，以及 `values` 块的写法。 |
| [读取行](docs/reading.zh-CN.md) | 单行、多行、分页与按 id 查询，`where` 块及结果结构。 |
| [更新与删除](docs/updating-and-deleting.zh-CN.md) | 按条件或按 id 更新、删除，以及 limit。 |
| [影响行数](docs/affected-rows.zh-CN.md) | `store affected rows` 子句，以及不使用事务的条件写入。 |
| [错误与等待](docs/errors-and-waiting.zh-CN.md) | 等待机制、`last database error` 及失败后的行为。 |
| [事务](docs/transactions.zh-CN.md) | 一组语句如何共同提交或回滚，以及事务何时结束。 |
| [类型](docs/types.zh-CN.md) | 每种列类型接受的值与存储方式。 |
| [排雷](docs/troubleshooting.zh-CN.md) | 常见问题：改表未生效、NULL 不显示、缺少 SkBee 时的 NBT。 |
| [菜谱](docs/cookbook.zh-CN.md) | 常见需求的完整脚本示例。 |
| [兼容性](docs/compatibility.zh-CN.md) | 版本、可用类型名、jar 内容及不支持的功能。 |
| [更新日志](CHANGELOG.md) | 各版本的改动，与 release 页面的说明一致。 |

## 从源码构建

`./gradlew build` 会在 `build/dist/` 生成 shaded jar；`./gradlew serverTest` 会启动真实的 Paper 服务端，运行插件自测。详见 [CONTRIBUTION.zh-CN.md](CONTRIBUTION.zh-CN.md)。

## 许可证

MIT，见 [LICENSE](LICENSE)。
