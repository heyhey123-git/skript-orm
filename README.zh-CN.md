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

插件针对批量读写设计：数据库操作在后台执行，大列表的输入分散到多个 tick 读取，避免在主线程上一次扫描整个 Skript 变量。
这不代表完全没有 tick 延迟，保存查询结果、转换单个值等工作仍会用到主线程。

下面的测试每个操作**写入或读取 5000 行**。固定数据量，是为了展示各个受支持数据库在本次运行中完成相同任务的表现。
本次所有写入都在 750 毫秒内完成，读取都在 105 毫秒内完成。

| 数据库 | 写入总耗时 | 写入最大 tick 间隔 | 读取总耗时 | 读取最大 tick 间隔 |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 735.828866 ms | 65.767643 ms | 103.186998 ms | 103.066121 ms |
| MySQL | 462.517747 ms | 50.395059 ms | 78.302884 ms | 78.218777 ms |
| MariaDB | 685.122311 ms | 66.066614 ms | 95.789059 ms | 95.813175 ms |
| PostgreSQL | 585.103286 ms | 66.926809 ms | 94.172914 ms | 94.082624 ms |
| MongoDB | 717.433104 ms | 50.968606 ms | 78.761947 ms | 78.579400 ms |

**这张表怎么看：**

- **数据库**：这一行测试使用的数据库。
- **总耗时**：当前触发器等待整次操作完成的时间，包含变量处理、数据库执行和脚本恢复运行。不是主线程持续被阻塞的时间。
- **最大 tick 间隔**：tick 观察器相邻两次执行之间的最大间隔。观察从操作开始持续到返回后一个 tick；正常 20 TPS 下约为 50 毫秒。它能显示观察到的延迟，不等于插件本身占用 CPU 的时间。

例如，MySQL 写入总共用了 `462.517747 ms`，期间最大的 tick 间隔只有 `50.395059 ms`，接近正常的 50 毫秒。
读取数据也说明，总耗时和 tick 延迟需要分别看。

数据来自 2026-10-03 的 [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500)，被测提交为 `6476c9b`。
环境为 Paper 26.2 build 124、Skript 2.16.2、Java 25.0.4.1 和 4 个可见 CPU，输入行只包含一个 `id` 字段。
SQLite、MariaDB 和 PostgreSQL 使用 AMD EPYC 7763，MySQL 和 MongoDB 使用 Intel Xeon 6973P-C。
共享测试主机和不同 CPU 会影响耗时，不能按这一次结果给数据库做普遍排名。
这次测试早于 `21898af` 的 MySQL 数据包大小限制修复，并未测量该修复后的表现。

计时使用 `System.nanoTime()`，毫秒保留六位小数，完整保留记录的纳秒值。
100 到 10000 行用例、重复测试、原始 SQL 对照和测量限制，见[基准与压力测试](docs/benchmarking.zh-CN.md)。

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
