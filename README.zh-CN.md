# skript-orm

写给 Skript 的 ORM。表描述一次，往后读写行都照 Skript 的写法来，SQL 由插件代劳。

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

两段里都没有一句 SQL。语句由插件拼好，在服务端线程之外执行，回到脚本里依然是普通的变量和值，伸手就能用。

## 环境要求

| | |
| --- | --- |
| **Paper** | 26.2 或更高，本插件正是针对这条版本线编译的。 |
| **Skript** | 2.16.2 或更高。版本不够时插件会自行禁用，并在控制台说明缘由。 |
| **MySQL** | 打包的数据库实现，对着 MySQL 8 测过。驱动 Paper 自带，不必另装。 |
| **SkBee** | 可选，只有 `nbtcompound` 列需要它。反过来说，没有 SkBee 时脚本本来也造不出 NBT 数据。 |

## 安装

1. 到 releases 页面取 `skriptorm-<version>.jar`，或者自己构建，见 [CONTRIBUTION.zh-CN.md](CONTRIBUTION.zh-CN.md)。
2. 把 jar 与 Skript 一并放进 `plugins/`。
3. 启动一次服务端，再把连接和建表写进脚本，`/sk reload` 即可。这两件事都由脚本完成，没有配置文件要改。

## 三条规矩

- **想要几条连接就有几条。** `create a connection` 让那个库成为默认连接，`named "logs"` 再留住一条，`in connection "logs":` 或 `use connection "logs"` 决定一条语句用哪一条。
- **碰数据的操作都是 section。** 写入、读取、更新、删除各有各的语法和主体，读取必定等结果。
- **一个事务就是一个 section。** `database transaction:` 在主体结束时提交，主体里有语句失败时回滚，运行期间独占一条连接。
- **失败是一个值。** 写了 `and wait` 的操作结束之后，`last database error` 里就是出错原因。一切顺利时它保持为空。

## 文档

| 页面 | 内容 |
| --- | --- |
| [快速上手](docs/getting-started.zh-CN.md) | 从空脚本到存下第一行，最短的一条路。 |
| [连接](docs/connections.zh-CN.md) | 连接属性、具名连接、切换、断开连接。 |
| [表](docs/tables.zh-CN.md) | 列语法、全部类型、主键与修饰符，以及建表**不会**做的事。 |
| [写入行](docs/writing.zh-CN.md) | 插入一行或多行、从变量插入、upsert，以及 `values` 块的写法。 |
| [读取行](docs/reading.zh-CN.md) | 查一行、多行、分页、按 id，`where` 块，以及结果的形状。 |
| [更新与删除](docs/updating-and-deleting.zh-CN.md) | 按条件或按 id 更新、删除，以及 limit。 |
| [错误与等待](docs/errors-and-waiting.zh-CN.md) | `and wait`、`last database error`，以及哪些部分在后台跑。 |
| [事务](docs/transactions.zh-CN.md) | 全做或全不做的一组语句，以及它怎样结束。 |
| [类型](docs/types.zh-CN.md) | 每种列类型接受什么、怎么存。 |
| [排雷](docs/troubleshooting.zh-CN.md) | 会踩的坑：静默的改表、看不见的 NULL、没有 SkBee 时的 NBT。 |
| [菜谱](docs/cookbook.zh-CN.md) | 脚本里最常用的几种写法，整段可抄。 |
| [兼容性](docs/compatibility.zh-CN.md) | 版本、jar 里打包了什么、不支持什么。 |

## 从源码构建

`./gradlew build` 会在 `build/dist/` 生成 shaded jar，`./gradlew serverTest` 会拉起一个真实的 Paper 服务端跑插件自测。两者都写在 [CONTRIBUTION.zh-CN.md](CONTRIBUTION.zh-CN.md) 里。
