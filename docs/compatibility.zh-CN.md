# 兼容性

**简体中文** | [English](compatibility.md)

## 版本

| | |
| --- | --- |
| **Paper** | 26.2 或更高。插件声明 `api-version: '26.2'`，并针对这条服务端版本线编译。 |
| **Skript** | 2.16.2 或更高。更低版本上插件会说明原因并禁用自己，所以旧 Skript 上的表现是“插件在、但没有启用”。 |
| **Java** | 用服务端自带的；插件针对 Paper 26.2 所用的 Java 版本构建。 |
| **MySQL** | 打包的实现针对 MySQL，CI 里对着 MySQL 8 测过。MariaDB 等 MySQL 兼容服务端没有测过。 |
| **SkBee** | 可选，只有 `nbtcompound` 列需要它。 |

## jar 里有什么

发布出来的 jar 打包了插件本体、Kotlin 运行时、连接池，以及**一个**数据库实现：配置为 MySQL 的 JDBC 实现。
它**不**打包 JDBC 驱动，因为服务端本来就有：Paper 自带 MySQL Connector/J 并让插件可见，所以不需要额外安装。

仓库里还有一个 PostgreSQL 实现和一个 MongoDB 实现。它们不在默认构建里、不在 CI 覆盖范围内、本文档也不介绍；
构建时可以用 `-PbundleModules=` 把它们打进去，那是开发者的事，不是受支持的配置。

## NBT compound 与 SkBee

`nbtcompound` 是唯一一种实现位于别的插件里的列类型。只支持 SkBee，原因有两个：

- 能给脚本提供构造 compound 方式（`nbt compound from "{...}"`）的是 SkBee，所以脚本里的 compound 永远是
  SkBee 的对象。
- SkBee 把 NBT 库打包在自己的包下，并不提供独立 NBT API 的那些类，两者无法混用。

因此本插件既不编译依赖、也不运行期依赖任何 NBT 实现。它会在处理第一个 NBT 值时按名字查找 SkBee 的类并链接它们，
于是：

- **不支持独立 NBT API 插件**；装它既没有帮助也不会冲突。
- **不锁定 SkBee 版本。** 只要还把 NBT 类放在 `com.shanebeestudios.skbee.api.nbt` 下就能用；哪天挪走了，NBT 会变成
  不可用，而这会被报告出来，而不是让插件崩溃。
- **没有 SkBee 的服务器完全可用**，只有 NBT 列例外：注册声明了这种列的表会被拒绝，信息里点名 SkBee。见
  [类型](types.zh-CN.md)。

插件描述里的 `softdepend: [SkBee]` 让 SkBee 先加载。它是加载顺序提示，不是依赖：有没有它，插件都能启用。

## 数据库

| | |
| --- | --- |
| MySQL | 支持，也是发布 jar 里唯一的实现。 |
| MariaDB | 未测试。语句形状是 MySQL 的（`INSERT IGNORE`、`ON DUPLICATE KEY UPDATE`、更新与删除上的 `LIMIT`），所以可能可用，但没有任何检查。 |
| PostgreSQL | 本版本不支持。 |
| MongoDB | 本版本不支持。 |
| SQLite 等 | 不支持。 |

## 依赖实现的行为

语法在各处都一样，但有几件事由实现决定，文档在相关处会说明：`upsert` 与 `insert ... if absent` 的冲突处理、
更新与删除能不能加 limit、以及“行不存在”的含义。各页面写的是 MySQL 的行为，而不是假装它放之四海皆准。
