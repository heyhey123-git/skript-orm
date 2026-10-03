# skript-orm

<!-- 此页也用作 wiki 首页；图片使用绝对路径，避免指向 wiki 中不存在的文件。 -->
![skript-orm：适用于 Skript 的 ORM](https://raw.githubusercontent.com/heyhey123-git/skript-orm/master/docs/assets/banner.png)

适用于 Skript 的 ORM。定义好表结构，就能用 Skript 语法读写数据，无需手写 SQL。

**简体中文** | [English](README.md)

## 使用示例

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

插件会生成数据库语句并在服务端主线程之外执行。查询结果可直接存入 Skript 变量。

## 环境要求

| | |
| --- | --- |
| **Paper** | 26.2 或更高。本版本针对 Paper 26.2 系列编译。 |
| **Skript** | 2.16.2 或更高。版本过低时，插件会自行禁用，并在控制台说明原因。 |
| **MySQL** | 内置支持，已通过 MySQL 8 测试。Paper 自带所需驱动。 |
| **PostgreSQL** | 内置支持，已在 CI 中通过真实数据库测试。首次启动时会将驱动下载到服务端的 `libraries/`；见 [兼容性](docs/compatibility.zh-CN.md#jar-里有什么)。 |
| **MongoDB** | 内置支持，已在 CI 中通过 MongoDB 8 测试。驱动也会在首次启动时下载。 |
| **SkBee** | 可选，仅 `nbtcompound` 列需要。脚本中的 NBT compound 也由 SkBee 提供。 |

## 安装

1. 从 [Releases 页面](https://github.com/heyhey123-git/skript-orm/releases)下载 `skriptorm-<version>.jar`，或按[构建说明](CONTRIBUTION.zh-CN.md)自行构建。
2. 将 jar 与 Skript 一起放入 `plugins/`。
3. 启动一次服务端，再在脚本中建立连接、注册表，用 `/sk reload` 加载。连接和表都由脚本定义，无需修改配置文件。

## 主要功能

- **支持多条连接。** `create a connection` 建立默认连接，`named "logs"` 创建具名连接，`in connection "logs":` 或 `use connection "logs"` 指定语句使用的连接。
- **直接在 Skript 中读写数据。** 插入、查询、更新和删除都有对应语法。需要 `values` 或 `where` 块时，使用 Skript 段落。数据库操作会等待执行完成。
- **事务。** `database transaction:` 在段落正常结束时提交；其中的数据库语句失败时回滚。事务期间使用同一条连接。
- **写操作可以返回影响行数。** `and store affected rows in {_rows}` 保存行数，具体含义取决于操作和数据库；条件更新可借此检测并发修改。见 [影响行数](docs/affected-rows.zh-CN.md)。
- **错误可在脚本中读取。** 每条语句都会等待完成，所以下一行执行时，`last database error` 已记录本次操作的错误；操作成功时则未设置。

## 性能与边界

服务端每次更新称为一个 tick；通常每秒更新 20 次，每次的目标耗时不超过 50 毫秒。

在下表的测试中，读取不超过 5000 行时没有拉长 tick；大量写入则会造成短暂卡顿，应尽量避开服务端繁忙时段。超过上限的读取不会保存部分结果；大批量写入会拆成多条语句。数据只代表表中列出的测试环境，不保证其他服务器也有相同性能。同一 CI 标签下的任务也可能跑在不同型号的处理器上。

| 测的是什么 | 结果 | 机器 |
| --- | --- | --- |
| 写入 5000 行 | 首次运行时，最长的 tick 比 50 毫秒超出 70 至 90 毫秒；后续运行从未超时到超出约 30 毫秒不等 | 临时 Paper 26.2 服务端，AMD Ryzen 5 5600X，JDK 25 |
| 写入 100 行、10000 行 | 分别超出一次 tick 约 10 毫秒、100 至 110 毫秒 | Paper 26.2，Ryzen 5 5600X，JDK 25 |
| 读取 100 至 5000 行 | 没有一次拉长 tick，而且要的行全部存下 | Paper 26.2，Ryzen 5 5600X，JDK 25 |
| 读取 10000 行 | 被拒绝：结果变量被清空，`last database error` 说明上限 | Paper 26.2，Ryzen 5 5600X，JDK 25 |
| 走通用 JDBC 通道的 5000 行 `insert many` | 9.960 ± 0.384 毫秒；三台 CI 主机上为 6.1 至 10.0 毫秒 | Ryzen 5 5600X；AMD EPYC 9V74；Intel Xeon Platinum 8573C |
| 数据库收到的语句数（插入 5000 行） | MySQL 5038 条，其中 5000 条为插入语句；MariaDB 2 条，其中 1 条为多行插入；PostgreSQL 无法取得该指标 | 同一次 CI 运行：MySQL 与 MariaDB 在 Intel Xeon Platinum 8370C 上，PostgreSQL 在 AMD EPYC 7763 上 |

插件把批次交给驱动，驱动再决定向数据库发送多少条语句。这解释了上表中 MySQL 与 MariaDB 的差异。语句数可以稳定复现，共享 CI 主机上的耗时则会波动。[基准与压力测试](docs/benchmarking.zh-CN.md)说明了测量方法和哪些指标会导致构建失败；耗时只报告，不作为失败条件。

**数量限制。** 一次读取最多保存 5000 行。超过上限时不保存任何结果，`last database error` 会说明原因。一条写入语句最多绑定 30000 个值，例如六列的 5000 行；更大的批次会拆成多条语句。过大的请求仍可能因其他资源限制被拒绝。

### 这些数字为什么没有

- **没有 tick 百分位。** 测试脚本记录每次操作期间最长的 tick，时钟精度为 10 毫秒。MSPT 指每个 tick 的耗时，单位为毫秒；平均 MSPT 是每个 tick 耗时的平均值。p50 是中位数，p99 表示约 99% 的 tick 不超过的耗时。要计算这些数值，需直接采样服务端的 tick 循环。
- **没有 PostgreSQL 的语句数。** 当前使用的 PostgreSQL 统计信息能统计行数和事务数，但无法得出每次操作收到多少条语句。

**注意 MySQL 的批量插入。** 使用测试时的 Connector/J 默认设置，驱动会逐行发送插入语句。在一项重复执行 20 次、每次插入 5000 行的测试中，MySQL 用时 17.04 秒，PostgreSQL 和 MariaDB 各用时 1.99 秒。开启驱动的批次改写选项可以加快插入，但驱动随后无法提供可用的影响行数。详见[故障排查](docs/troubleshooting.zh-CN.md#把-insert-many-改快之后affected-rows-就不存了)。

## 文档

| 页面 | 内容 |
| --- | --- |
| [快速上手](docs/getting-started.zh-CN.md) | 从空脚本开始，保存第一行数据。 |
| [连接](docs/connections.zh-CN.md) | 连接属性、具名连接、切换与断开连接。 |
| [表](docs/tables.zh-CN.md) | 列语法、全部类型、主键与修饰符，以及注册表**不会**做的事。 |
| [原始语句](docs/raw-statements.zh-CN.md) | 手写 SQL 或 MongoDB 命令时需要了解的限制与风险。 |
| [写入行](docs/writing.zh-CN.md) | 插入一行或多行、从变量插入、upsert，以及 `values` 块的写法。 |
| [读取行](docs/reading.zh-CN.md) | 单行、多行、分页与按 id 查询，`where` 块及结果结构。 |
| [更新与删除](docs/updating-and-deleting.zh-CN.md) | 按条件或按 id 更新、删除，以及 limit。 |
| [影响行数](docs/affected-rows.zh-CN.md) | `store affected rows` 子句，以及不使用事务的条件写入。 |
| [错误与等待](docs/errors-and-waiting.zh-CN.md) | 等待机制、`last database error` 及失败后的行为。 |
| [事务](docs/transactions.zh-CN.md) | 一组语句如何共同提交或回滚，以及事务何时结束。 |
| [类型](docs/types.zh-CN.md) | 每种列类型接受的值与存储方式。 |
| [故障排查](docs/troubleshooting.zh-CN.md) | 改表未生效、NULL 不显示、缺少 SkBee 时的 NBT 等常见问题。 |
| [示例](docs/cookbook.zh-CN.md) | 常见需求的完整脚本。 |
| [兼容性](docs/compatibility.zh-CN.md) | 版本、可用类型名、jar 内容及不支持的功能。 |
| [更新日志](CHANGELOG.md) | 各版本的改动，与 release 页面的说明一致。 |

## 从源码构建

`./gradlew build` 会在 `build/dist/` 生成 shaded jar；`./gradlew serverTest` 会启动真实的 Paper 服务端，运行插件自测。详见 [CONTRIBUTION.zh-CN.md](CONTRIBUTION.zh-CN.md)。

## 许可证

MIT，见 [LICENSE](LICENSE)。
