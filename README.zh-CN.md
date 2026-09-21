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

## 文档

| 页面 | 内容 |
| --- | --- |
| [快速上手](docs/getting-started.zh-CN.md) | 从空脚本开始，保存第一行数据。 |
| [连接](docs/connections.zh-CN.md) | 连接属性、具名连接、切换与断开连接。 |
| [表](docs/tables.zh-CN.md) | 列语法、全部类型、主键与修饰符，以及注册表**不会**做的事。 |
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
