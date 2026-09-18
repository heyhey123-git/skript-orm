# 兼容性

**简体中文** | [English](compatibility.md)

## 版本

| | |
| --- | --- |
| **Paper** | 26.2 或更高。插件声明 `api-version: '26.2'`，并针对这条服务端版本线编译。 |
| **Skript** | 2.16.2 或更高。更低版本上插件会说明原因并禁用自己，所以旧 Skript 上的表现是“插件在、但没有启用”。 |
| **Java** | 用服务端自带的；插件针对 Paper 26.2 所用的 Java 版本构建。 |
| **MySQL** | 打包的实现针对 MySQL，CI 里对着 MySQL 8 测过。MariaDB 等 MySQL 兼容服务端没有测过。 |
| **PostgreSQL** | 通过 `"PostgreSQL"` 支持。CI 里对着 PostgreSQL 测过：既测了实现本身，也在真实服务端上完整跑过一遍插件。 |
| **SkBee** | 可选，只有 `nbtcompound` 列需要它。 |

## 脚本能写的类型名

`create a connection to database "..."` 里写的是实现的**类型名**，大小写敏感的精确匹配。发布 jar 里注册了三个：

| 类型名 | 它是什么 |
| --- | --- |
| `"MySQL"` | MySQL 方言，加上由插件自动找到的 MySQL 驱动。这里产品和类型名恰好是同一个词，这也是本页两份清单容易被混为一谈的唯一原因。 |
| `"PostgreSQL"` | PostgreSQL 方言，加上插件为它下载的驱动。见 [jar 里有什么](#jar-里有什么)。 |
| `"JDBC"` | 由你在 `driver` 属性里指定驱动，配一个写通用 SQL 的方言（`"双引号"` 标识符、取一行用 `LIMIT 1`、分页用 `LIMIT ? OFFSET ?`），通用写法表达不了的一律拒绝。 |

其它任何名字都会在 section 运行时被拒绝，报 `Database '<名字>' is not supported.`。没有叫 `"MariaDB"`、
`"MongoDB"` 或 `"SQLite"` 的类型；它们是产品，下面「数据库产品」那节讲的是它们。

## jar 里有什么

发布出来的 jar 打包了插件本体、Kotlin 运行时、连接池，以及本仓库**每一个实现模块**——MySQL 方言与通用 JDBC
那一个，还有 PostgreSQL 模块。它**一个 JDBC 驱动都不带**，这是有意为之：

- MySQL 和通用类型用的是服务端本来就有的驱动。Paper 自带 MySQL Connector/J 并让插件可见，所以不需要为它装什么；
  `"JDBC"` 则是给服务端自带、而这个 jar 一无所知的驱动用的。
- PostgreSQL 的驱动写在 `plugin.yml` 的 `libraries` 条目里，由 Paper 在首次启动时下载一次，放进服务端的
  `libraries/` 目录，再从那里加载到插件的 classpath 上。下载源是**服务端**的 Maven Central 镜像：
  `PAPER_DEFAULT_CENTRAL_REPOSITORY`，或 `org.bukkit.plugin.java.LibraryLoader.centralURL` 系统属性，默认则是
  Google 的 Central 镜像。首次启动之后，驱动就在 `libraries/` 里，也从那里取用，所以每个服务端只下载一次。

这个条目是一份承诺：Paper 把解析不到的库当作致命错误，所以连不上镜像的服务端根本不会加载本插件。出路是上面那个
镜像设置——值得一提的是，它作用于该服务端上**每一个**插件的下载，而不只是本插件——或者手工把目录放好，其中可靠的
做法是从一个已经启动过一次的服务端上，把整个 `libraries/` 目录拷过来。

这也是这个 jar 不把任何驱动 shade 进去的原因。重写过的副本和下载来的那一份会变成两个驱动，而下载来的那一份才是
其作者发布的库：它保留自己的 service 文件，版本号只需在一处升级，校验和也由 Paper 核对。

MongoDB 实现在仓库里，但不在 jar 里，不在 CI 覆盖范围内，本文档也不介绍。`-PbundleModules=` 可以按任意组合打包
模块，包括还没发布的模块，那是开发者的事，而不是受支持的配置。

## NBT compound 与 SkBee

`nbtcompound` 是唯一一种实现位于别的插件里的列类型。只支持 SkBee，原因有两个：

- 能给脚本提供构造 compound 方式（`nbt compound from "{...}"`）的是 SkBee，所以脚本里的 compound 永远是
  SkBee 的对象。
- SkBee 把 NBT 库打包在自己的包下，并不提供独立 NBT API 的那些类，两者无法混用。

因此本插件既不编译依赖、也不运行期依赖任何 NBT 实现。它会在处理第一个 NBT 值时按名字查找 SkBee 的类并链接它们，
于是：

- **不支持独立 NBT API 插件**；装它既没有帮助也不会冲突。
- **不锁定 SkBee 版本，但 NBT 按一组固定的类名与方法名链接。** 版本必须还在 `com.shanebeestudios.skbee.api.nbt` 下
  提供 `NBTCompound`、`NBTContainer(String)` 与 `NBTContainer(InputStream)` 两个构造方法，以及静态的
  `NBTReflectionUtil.writeApiNBT`；其中任何一处被挪走或改了形状，NBT 就变成不可用，而这会被报告出来，而不是让
  插件崩溃。
- **没有 SkBee 的服务器完全可用**，只有 NBT 列例外：注册声明了这种列的表会被拒绝，信息里点名 SkBee。见
  [类型](types.zh-CN.md)。

插件描述里的 `softdepend: [SkBee]` 让 SkBee 先加载。它是加载顺序提示，不是依赖：有没有它，插件都能启用。

## 数据库产品

这里没有一个名字是类型名：这一节说的是连接**能连到什么**，类型名只有「脚本能写的类型名」里那三个。

| | |
| --- | --- |
| MySQL | 支持。写 `"MySQL"`。CI 里对着 MySQL 8 测过。 |
| MariaDB | 未测试。写 `"MySQL"`——语句形状是 MySQL 的（`INSERT IGNORE`、`ON DUPLICATE KEY UPDATE`、更新与删除上的 `LIMIT`）——很可能可用，但没有任何检查。 |
| PostgreSQL | 支持。写 `"PostgreSQL"`；驱动会在首次启动时下载。CI 里对着真实服务端测过，插件也在真实 Paper 服务端上对着它完整跑过一遍。 |
| MongoDB | 本版本不支持。实现只在仓库里，不在 jar 里。 |
| SQLite 等 | SQLite 能用，走的是 `"JDBC"` 和 Paper 已经带的那个驱动：方言写出的东西它全不反对，服务端测试在两种模式下都会对它跑一遍插入、读取、分页、更新和删除。那个方言拒绝的照旧拒绝——没有 auto increment、没有 `insert ... if absent`、没有 `upsert`、写操作不能加 limit——其它产品则需要有服务端没带的驱动。 |

## 依赖实现的行为

语法在各处都一样，但有几件事由实现决定，文档在相关处会说明：`upsert` 与 `insert ... if absent` 的冲突处理、
更新与删除能不能加 limit、以及“行不存在”的含义。各页面写的是 MySQL 的行为，而不是假装它放之四海皆准。
