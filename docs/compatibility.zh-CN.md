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

## 脚本能写的类型名

`create a connection to database "..."` 里写的是实现的**类型名**，大小写敏感的精确匹配。发布 jar 里注册了两个：

| 类型名 | 它是什么 |
| --- | --- |
| `"MySQL"` | MySQL 方言，加上由插件自动找到的 MySQL 驱动。这里产品和类型名恰好是同一个词，这也是本页两份清单容易被混为一谈的唯一原因。 |
| `"JDBC"` | 由你在 `driver` 属性里指定驱动，配一个写通用 SQL 的方言（`"双引号"` 标识符、取一行用 `LIMIT 1`、分页用 `LIMIT ? OFFSET ?`），通用写法表达不了的一律拒绝。 |

其它任何名字都会在 section 运行时被拒绝，报 `Database '<名字>' is not supported.`。没有叫 `"MariaDB"`、
`"PostgreSQL"`、`"MongoDB"` 或 `"SQLite"` 的类型；它们是产品，下面「数据库产品」那节讲的是它们。

## jar 里有什么

发布出来的 jar 打包了插件本体、Kotlin 运行时、连接池，以及上面那两个类型名——两者其实是同一个 JDBC 实现，
一个加上 MySQL 方言，一个从脚本取驱动。它**不**打包 MySQL 的 JDBC 驱动，因为服务端本来就有：Paper 自带
MySQL Connector/J 并让插件可见，所以不需要额外安装；`"JDBC"` 用的是服务端为它指向的那个库所带的驱动，
这也是它唯一的用武之地。

仓库里还有一个 PostgreSQL 实现和一个 MongoDB 实现。它们不在默认构建里、不在 CI 覆盖范围内、本文档也不介绍；
构建时可以用 `-PbundleModules=` 把它们打进去，那是开发者的事，不是受支持的配置——它们也是本仓库提到的数据库
类型比发布 jar 注册的更多的原因。

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

这里没有一个名字是类型名：这一节说的是连接**能连到什么**，类型名只有「脚本能写的类型名」里那两个。

| | |
| --- | --- |
| MySQL | 支持。写 `"MySQL"`。CI 里对着 MySQL 8 测过。 |
| MariaDB | 未测试。写 `"MySQL"`——语句形状是 MySQL 的（`INSERT IGNORE`、`ON DUPLICATE KEY UPDATE`、更新与删除上的 `LIMIT`）——很可能可用，但没有任何检查。 |
| PostgreSQL | 本版本不支持。仓库里有实现，发布 jar 里没有。 |
| MongoDB | 本版本不支持。实现只在仓库里，不在 jar 里。 |
| SQLite 等 | 不支持。Paper 确实带 SQLite 的驱动，`"JDBC"` 方言写出的 SQL 本身 SQLite 也不反对，但那个驱动没有实现本实现绑定值用的那个 `setObject` 重载，所以凡是带值的语句都会以 `setObject not implemented` 失败。其它产品需要有服务端没带的驱动。 |

## 依赖实现的行为

语法在各处都一样，但有几件事由实现决定，文档在相关处会说明：`upsert` 与 `insert ... if absent` 的冲突处理、
更新与删除能不能加 limit、以及“行不存在”的含义。各页面写的是 MySQL 的行为，而不是假装它放之四海皆准。
