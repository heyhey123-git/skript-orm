# skript-orm

<!-- 此页也用作 wiki 首页，图片使用绝对地址。 -->
![skript-orm：适用于 Skript 的 ORM](https://raw.githubusercontent.com/heyhey123-git/skript-orm/master/docs/assets/banner.png)

**用 Skript 保存你的 Minecraft 服务器数据。** 定义好表，就能用 Skript 语法保存玩家档案、奖励记录和日志，开始使用不需要先学 SQL。

**简体中文** | [English](README.md)

[下载](https://github.com/heyhey123-git/skript-orm/releases) · [快速上手](docs/getting-started.zh-CN.md) · [完整示例](docs/cookbook.zh-CN.md) · [性能测试](docs/benchmarking.zh-CN.md)

## 为什么使用 skript-orm？

- **不用拼 SQL 字符串。** 插入、查询、更新、删除都有对应的 Skript 语法，查询结果直接存进普通变量。
- **直接保存 Minecraft 数据。** UUID、物品、位置、日期和时间段都有对应的列类型。安装 SkBee 后还可以保存 NBT 复合标签。
- **玩家输入不会变成 SQL。** 结构化语句通过参数传递值，并检查字段名和数据类型。玩家输入的名字或消息只作为数据处理，不会被当作查询的一部分。自行执行原始语句时仍需注意输入安全。
- **适合批量保存。** `insert many` 跨 tick 分段读取列表变量，再把数据库工作交给后台线程。MySQL 使用带参数的多行插入语句。
- **保存成功再继续。** 当前触发器会等操作完成后继续执行，等待期间服务端仍能运行 tick。检查 `last database error`，就能在告知玩家“保存成功”之前确认结果。
- **按服务器需要选择数据库。** MySQL、MariaDB、PostgreSQL、SQLite 和 MongoDB 都有真实数据库及 Paper 测试。同一脚本也可以通过具名连接使用多个数据库。

## 环境要求

| 组件 | 要求 |
| --- | --- |
| Paper | 26.2 或更新版本，插件针对 26.2 系列编译。 |
| Skript | 2.16.2 或更新版本。 |
| Java | 25 或更新版本。 |
| SkBee | 可选，只有 `nbtcompound` 列需要。 |

### 选择数据库

下表列出已经测试的数据库，以及创建连接时应填写的类型名。SQLite 使用通用的 `"JDBC"` 连接类型。

| 数据库 | 连接类型 | 需要准备什么 |
| --- | --- | --- |
| MySQL | `"MySQL"` | MySQL 服务和账号，Paper 自带驱动。 |
| MariaDB | `"MariaDB"` | MariaDB 服务和账号。 |
| PostgreSQL | `"PostgreSQL"` | PostgreSQL 服务和账号。 |
| SQLite | `"JDBC"` | 服务端上的数据库文件，不需要单独运行数据库服务，Paper 自带驱动。 |
| MongoDB | `"MongoDB"` | MongoDB 服务和账号，同样可以使用插件的结构化读写语法。 |

MariaDB、PostgreSQL 和 MongoDB 的驱动由 Paper 在首次启动时下载。下载失败时，见[兼容性说明](docs/compatibility.zh-CN.md#jar-里有什么)。
本插件尚未开放 MongoDB 事务；SQLite 使用的通用 JDBC 路径也有[部分操作限制](docs/compatibility.zh-CN.md#数据库产品)。

## 安装

1. 从 [Releases](https://github.com/heyhey123-git/skript-orm/releases) 下载 `skriptorm-<version>.jar`。
2. 将 jar 和 Skript 一起放入 `plugins/`，重启服务端。
3. 在 `plugins/Skript/scripts/` 中添加脚本，填写数据库连接信息，再用 `/sk reload <脚本名>` 加载。

连接和表都在脚本里定义，不需要额外修改插件配置文件。完整步骤见[快速上手](docs/getting-started.zh-CN.md)。

## 示例：保存和查询玩家档案

下面用 MySQL 为每位玩家保存一条记录。玩家加入时更新名字和最近加入日期，输入 `/myrecord` 可以查询自己的记录。
加载脚本前，请先创建 `minecraft` 数据库和账号，并替换示例中的连接信息。

```sk
on load:
    set {playerdb::ready} to false
    create a connection to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/minecraft"
        username: "minecraft"
        password: "change-me"
    if last database error is set:
        send "数据库连接失败: %last database error%" to console
        stop

    register a database table "players":
        uuid: uuid, primary key, not null
        name: string(64), not null
        last_join: date, not null
    if last database error is set:
        send "玩家表注册失败: %last database error%" to console
        stop
    set {playerdb::ready} to true

on join:
    if {playerdb::ready} is not true:
        stop
    upsert one entity in table "players" by id uuid of player and wait:
        values:
            name: name of player
            last_join: now
    if last database error is set:
        send "玩家数据保存失败: %last database error%" to console

command /myrecord:
    executable by: players
    trigger:
        if {playerdb::ready} is not true:
            send "数据库尚未准备好。"
            stop
        select one entity from table "players" and store the result in {_row::*}:
            where all:
                uuid = uuid of player
        if last database error is set:
            send "查询失败，请稍后重试。"
            send "玩家查询失败: %last database error%" to console
            stop
        if {_row::uuid} is not set:
            send "还没有保存你的记录。"
            stop
        send "已保存的名字: %{_row::name}%"
        send "最近加入日期: %{_row::last_join}%"
```

`by id` 指的是表的主键，在这里就是 `uuid`。`upsert` 会创建缺失的记录，已有记录则更新。
SQL 的 `date` 列只保存年月日，不保存时分秒。

连接和表注册集中放在一个脚本里，其他脚本可以直接复用。注册表会创建缺失的表，但不会修改已有表的列。
需要调整已经使用的表结构时，请先阅读[表](docs/tables.zh-CN.md)。

## 真实服务端上的性能

数据库调用在后台执行。`{_rows::*}` 这样的局部列表，也会在当前脚本等待期间于后台处理；大批全局输入则分散到多个 tick 读取。物品、位置等需要服务器 API 的转换，共用每 tick 约 2 毫秒的目标预算。单个慢转换仍可能超过这个目标。

[Run #7](https://github.com/heyhey123-git/skript-orm/actions/runs/37205411783) 在五种数据库上读取 **5000 行数据，每行六个数值列都有值**。下表比较同一任务存进局部变量和全局变量的表现。每个数都是三次预热后十次测量的中位数。

- **数据库**：这一行使用的数据库。
- **局部／全局总耗时**：从读取语句开始到脚本恢复的时间，包含后台工作和等待。
- **局部／全局主线程**：已记录的主线程处理区间累计耗时。它不是整个服务端的 CPU 时间，也不是完整 tick 的耗时。

| 数据库 | 局部总耗时 | 全局总耗时 | 局部主线程 | 全局主线程 |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 49.925225 ms | 65.414039 ms | 0.018713 ms | 15.483043 ms |
| MySQL | 49.923762 ms | 86.510248 ms | 0.015720 ms | 36.381696 ms |
| MariaDB | 50.005949 ms | 66.299079 ms | 0.017637 ms | 15.888968 ms |
| PostgreSQL | 49.942947 ms | 69.211086 ms | 0.018167 ms | 19.242586 ms |
| MongoDB | 49.950872 ms | 71.939851 ms | 0.014090 ms | 21.958078 ms |

局部变量路径将普通结果的保存工作移出了主线程。完整读取仍需等待约一个 tick，因为 Skript 在主线程上恢复运行。这些数据比较的是当前实现中的两种变量作用域，不是新旧版本对照，也不是与其他插件的性能比赛。

本次测试于 2026-10-04 运行提交 `76dcc7e`，环境为 Paper 26.2 build 124、Skript 2.16.2 和 Java 25。不同数据库作业使用了不同 CPU，因此不能按这张表给数据库排名。大量游戏对象存进全局结果时，最后赋值仍可能拉长 tick；处理大量数据时，优先使用局部结果并缩小页大小。

计时使用 `System.nanoTime()`，毫秒保留六位小数，完整保留记录的纳秒值。CPU 型号、写入曲线、物品／位置测试、原始报告和测量限制，见[新版基准实测](docs/benchmarking.zh-CN.md#新版实测run-7)。


### 使用时要知道的限制

- 单次读取最多保存 **5000 行**，更多结果会被拒绝，不会只保存一部分。数据较多时使用[分页查询](docs/reading.zh-CN.md)。
- 大批量写入会拆成多条数据库语句，每条最多绑定 **30000 个值**。`insert many` 完成前，请保持源变量不变。
- SQL 事务可以让一组写入共同提交，失败时一起回滚。批量写入拆成多条语句，本身并不保证全部成功或全部撤销；需要这种保证时使用[事务](docs/transactions.zh-CN.md)。

## 与 skript-db 有什么区别？

`skript-db` 有多个同名项目。下表以 [btk5h 原版](https://github.com/btk5h/skript-db)和 [Limework 分支](https://github.com/Limework/skript-db)为 SQL 插件的例子。
不同分支的功能和兼容性并不相同，安装时应确认具体项目，不能仅凭名字就认定它已经停止维护。
[4w3 分支](https://hangar.papermc.io/4w3/skript-db)仍在维护，也提供参数绑定、批量操作和事务，这些并不是 skript-orm 独有的功能。

这里比较的是写脚本和维护脚本的方式。我们还没有与这些插件在同一服务端进行性能对照，前面的数据不能证明比它们快多少。

| 使用需求 | skript-orm | 以 SQL 为主的 skript-db 插件 |
| --- | --- | --- |
| 开始存数据 | 声明列，再使用 `insert`、`select`、`update`、`delete`。 | 编写 SQL 查询，用 SQL 管理表结构。 |
| 处理玩家输入 | 结构化语句通过参数传值，并检查声明的字段和数据类型。 | 原版及 Limework 项目也提供 SQL 注入防护；能否安全使用仍取决于查询接口和脚本传值方式。 |
| 保存 Minecraft 数据 | UUID、物品和位置都有内置类型，安装 SkBee 后可保存 NBT。 | 脚本需要自行安排这些值在数据库里的表示和转换。 |
| 选择数据库 | 为 MySQL、MariaDB、PostgreSQL、SQLite、MongoDB 提供结构化操作，并说明各后端的差异。 | 上述项目通过 JDBC 访问 SQL 数据库，该接口不包含 MongoDB。 |
| 批量写入 | 提供 `insert many`，跨 tick 读取输入，MySQL 使用带参数的多行插入。 | 批量执行方式取决于分支、查询写法和数据库驱动。 |
| 控制查询 | 常见任务有专用语法，也提供手写原始语句的入口。 | 直接使用 SQL，方便编写联表、聚合和数据库专属查询。 |

如果你希望沿用写 Skript 的方式存数据，尤其需要保存 Minecraft 对象或导入列表，skript-orm 能减少自己维护的查询拼接和类型转换代码。
已经依赖复杂 SQL 的脚本，也可以选择 SQL 插件。skript-orm 的优势是常见读写更容易写清楚，错误可在脚本中处理，数据库行为有测试和文档可查。

## 文档

| 你想做什么 | 阅读 |
| --- | --- |
| 保存第一条数据 | [快速上手](docs/getting-started.zh-CN.md) |
| 连接数据库、使用多条连接 | [连接](docs/connections.zh-CN.md) |
| 定义列和主键 | [表](docs/tables.zh-CN.md) |
| 保存一行、导入列表、存在时更新 | [写入行](docs/writing.zh-CN.md) |
| 按条件查询、分页读取 | [读取行](docs/reading.zh-CN.md) |
| 修改和删除数据 | [更新与删除](docs/updating-and-deleting.zh-CN.md) |
| 检查错误和影响行数 | [错误与等待](docs/errors-and-waiting.zh-CN.md)、[影响行数](docs/affected-rows.zh-CN.md) |
| 让多条写入一起成功或撤销 | [事务](docs/transactions.zh-CN.md) |
| 保存 Minecraft 数据类型 | [类型](docs/types.zh-CN.md) |
| 改写一个完整示例 | [示例](docs/cookbook.zh-CN.md) |
| 排查问题、确认兼容性 | [故障排查](docs/troubleshooting.zh-CN.md)、[兼容性](docs/compatibility.zh-CN.md) |
| 了解性能测试 | [基准与压力测试](docs/benchmarking.zh-CN.md) |
| 查看版本改动 | [更新日志](CHANGELOG.md) |

## 构建与许可

自行构建或运行测试，见 [CONTRIBUTION.zh-CN.md](CONTRIBUTION.zh-CN.md)。
构建产物位于 `build/dist/`。采用 MIT 许可，见 [LICENSE](LICENSE)。
