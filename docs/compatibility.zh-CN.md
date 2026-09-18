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
| **MongoDB** | 通过 `"MongoDB"` 支持，CI 里对着 MongoDB 8 测过。它的驱动和 PostgreSQL 的一样，会在首次启动时下载。 |
| **SkBee** | 可选，只有 `nbtcompound` 列需要它。 |

## 脚本能写的类型名

`create a connection to database "..."` 里写的是实现的**类型名**，大小写敏感的精确匹配。发布 jar 里注册了四个：

| 类型名 | 它是什么 |
| --- | --- |
| `"MySQL"` | MySQL 方言，加上由插件自动找到的 MySQL 驱动。这里产品和类型名恰好是同一个词，这也是本页两份清单容易被混为一谈的唯一原因。 |
| `"PostgreSQL"` | PostgreSQL 方言，加上插件为它下载的驱动。见 [jar 里有什么](#jar-里有什么)。 |
| `"MongoDB"` | MongoDB，通过插件为它下载的阻塞式 MongoDB Java 驱动访问。它底下没有 SQL，所以有几条语句的行为是刻意不同的，见 [MongoDB](#mongodb)。 |
| `"JDBC"` | 由你在 `driver` 属性里指定驱动，配一个写通用 SQL 的方言（`"双引号"` 标识符、取一行用 `LIMIT 1`、分页用 `LIMIT ? OFFSET ?`），通用写法表达不了的一律拒绝。 |

其它任何名字都会在 section 运行时被拒绝，报 `Database '<名字>' is not supported.`。没有叫 `"MariaDB"` 或
`"SQLite"` 的类型；它们是产品，下面「数据库产品」那节讲的是它们。`"MongoDB"` 两样都是，正如 `"MySQL"`。

## jar 里有什么

发布出来的 jar 打包了插件本体、Kotlin 运行时、连接池，以及本仓库**每一个实现模块**——MySQL 方言与通用 JDBC
那一个、PostgreSQL 模块，还有 MongoDB 模块。它**一个数据库驱动都不带**，这是有意为之：

- MySQL 和通用类型用的是服务端本来就有的驱动。Paper 自带 MySQL Connector/J 并让插件可见，所以不需要为它装什么；
  `"JDBC"` 则是给服务端自带、而这个 jar 一无所知的驱动用的。
- PostgreSQL 与 MongoDB 的驱动写在 `plugin.yml` 的 `libraries` 条目里，由 Paper 在首次启动时下载一次，放进
  服务端的 `libraries/` 目录，再从那里加载到插件的 classpath 上。这份清单由 jar 打包的模块生成：默认构建里是
  `org.postgresql:postgresql:42.7.11` 与 `org.mongodb:mongodb-driver-sync:5.6.1`，而不需要任何驱动的模块组合会
  写出 `libraries: []`。下载源是**服务端**的 Maven Central 镜像：`PAPER_DEFAULT_CENTRAL_REPOSITORY`，或
  `org.bukkit.plugin.java.LibraryLoader.centralURL` 系统属性，默认则是 Google 的 Central 镜像。首次启动之后，
  驱动就在 `libraries/` 里，也从那里取用，所以每个服务端只下载一次。

这个条目是一份承诺：Paper 把解析不到的库当作致命错误，所以连不上镜像的服务端根本不会加载本插件。出路是上面那个
镜像设置——值得一提的是，它作用于该服务端上**每一个**插件的下载，而不只是本插件——或者手工把目录放好，其中可靠的
做法是从一个已经启动过一次的服务端上，把整个 `libraries/` 目录拷过来。

这也是这个 jar 不把任何驱动 shade 进去的原因。重写过的副本和下载来的那一份会变成两个驱动，而下载来的那一份才是
其作者发布的库：它保留自己的 service 文件，版本号只需在一处升级，校验和也由 Paper 核对。MongoDB 用的是阻塞式的
`mongodb-driver-sync`，而不是 Kotlin 协程那一个：这个 jar 会把自己的 kotlinx.coroutines shade 并重定位，而期待
未重定位版本的那种驱动会变成同一份运行时的第二个副本。

`-PbundleModules=` 可以按任意组合打包模块，包括还没发布的模块，那是开发者的事，而不是受支持的配置。`libraries`
清单跟着组合里打包的模块走，所以每个 jar 只索取自己用得到的驱动。

## NBT compound 与 SkBee

`nbtcompound` 是唯一一种实现位于别的插件里的列类型。只支持 SkBee，原因有两个：

- 能给脚本提供构造 compound 方式（`nbt compound from "{...}"`）的是 SkBee，所以脚本里的 compound 永远是
  SkBee 的对象。
- SkBee 把 NBT 库打包在自己的包下，并不提供独立 NBT API 的那些类，两者无法混用。

因此本插件既不编译依赖、也不运行期依赖任何 NBT 实现。它在插件启用时按名字查找一次 SkBee 的类并链接它们，于是：

- **不支持独立 NBT API 插件**；装它既没有帮助也不会冲突。
- **不锁定 SkBee 版本，但 NBT 按一组固定的类名与方法名链接。** 版本必须还在 `com.shanebeestudios.skbee.api.nbt` 下
  提供 `NBTCompound`、`NBTContainer(String)` 与 `NBTContainer(InputStream)` 两个构造方法，以及静态的
  `NBTReflectionUtil.writeApiNBT`；其中任何一处被挪走或改了形状，NBT 就变成不可用，而这会被报告出来，而不是让
  插件崩溃。
- **没有 SkBee 的服务器完全可用**，只有 NBT 列例外：注册声明了这种列的表会被拒绝，信息里点名 SkBee。见
  [类型](types.zh-CN.md)。

插件描述里的 `softdepend: [SkBee]` 让 SkBee 先加载。它是加载顺序提示，不是依赖：有没有它，插件都能启用。

## 数据库产品

这里没有一个名字是类型名：这一节说的是连接**能连到什么**，类型名只有「脚本能写的类型名」里那四个。

| | |
| --- | --- |
| MySQL | 支持。写 `"MySQL"`。CI 里对着 MySQL 8 测过。 |
| MariaDB | 未测试。写 `"MySQL"`——语句形状是 MySQL 的（`ON DUPLICATE KEY UPDATE`、更新与删除上的 `LIMIT`）——很可能可用，但没有任何检查。 |
| PostgreSQL | 支持。写 `"PostgreSQL"`；驱动会在首次启动时下载。CI 里对着真实服务端测过，插件也在真实 Paper 服务端上对着它完整跑过一遍。 |
| MongoDB | 支持。写 `"MongoDB"`；驱动会在首次启动时下载。它的事务在本实现里还没有做，`database transaction` section 会报那句拒绝。CI 里对着 MongoDB 8 测过。 |
| SQLite 等 | SQLite 能用，走的是 `"JDBC"` 和 Paper 已经带的那个驱动：方言写出的东西它全不反对，服务端测试在两种模式下都会对它跑一遍插入、读取、分页、更新和删除。那个方言拒绝的照旧拒绝——没有 auto increment、没有 `insert ... if absent`、没有 `upsert`、写操作不能加 limit——其它产品则需要有服务端没带的驱动。 |

## MongoDB

`"MongoDB"` 既是产品名也是类型名，就像 `"MySQL"`，其它页面上的语法在它这里同样成立。搬不过来的是 SQL 实现的那套
假设，所以这一种实现对每条语句的处理值得集中说一次。

- **声明过的列类型全都能用。** uuid 存成 BSON binary，date、time、timespan 存成数字，itemstack、location、
  bukkit-serializable、nbt 存成 BSON binary。没有哪种类型会因为没地方存而被丢掉。
- **auto increment 靠计数器文档实现**，每个自增键一份，放在名为 `skript_orm_sequences` 的集合里。这个名字因此
  被占用：表不能叫这个名字。计数器在注册表的时候建立，并抬到集合里已有的最高键——所以注册表之前就存着的键，不会
  再被发第二次。
- **语句自己带了键时，计数器会被抬到那个键之上。** 带显式键的插入和 `upsert by id` 都会把它们写下的键告诉计数器，
  这正是 MySQL 的 `AUTO_INCREMENT` 在显式插入时的行为，也是 PostgreSQL 的序列需要手工 `setval` 才能做到的事。
  因此无论脚本怎么混用显式键和自增键，同一个键都不会被发两次。唯一还可能撞键的情形，是注册表之后有别的程序直接往
  集合里写了文档——那时插入会以服务端的重复键错误失败，而不是覆盖掉它。
- **`_id` 是 MongoDB 自己的字段**，留给文档本身的身份，所以表不能声明名为 `_id` 的列。注册这样的表会被拒绝，并
  给出原因，而不是等到之后才出错。
- **`select page` 需要主键**，并且一律按主键排序，和 JDBC、PostgreSQL 两个实现一样：MongoDB 的自然顺序并不
  稳定，没有主键，「第 2 页」就没有意义。
- **`update`、`update by id` 与 `upsert by id` 报告的是过滤器匹配到的行数**，不是服务端改动的行数。把某列写成
  它已经是的值，这一行照样计数。
- **`insert if absent` 按 key 判断，而且是原子的。** 已经被唯一索引覆盖的写入会被忽略而不是报成错误；在 MongoDB 上这是一个被插件捕获的重复键错误，和 MySQL 方言捕获的是同一类。
- **`delete ... with limit n` 真的最多删 n 行。** MongoDB 没有删除上限，所以插件先选出这么多个标识，再删掉它们。
- **与 null 比较的过滤器遵循 MongoDB。** `column = null` 匹配该列是 null **或不存在**的行，`column != null`
  匹配该列存在且不是 null 的行。
- **除此之外，服务端什么都不强制。** 可空与长度是声明，不是约束。注册会建主键上的唯一索引和自增计数器，集合则由
  MongoDB 在写入第一份文档时自行创建。
- **事务还没有实现。** 这个实现的 `supportsTransactions` 保持为 false，所以 `database transaction` section 会
  失败，报 `This database implementation does not support transactions.`——这与任何没有事务的实现报的是同一句
  话。MongoDB 本身在副本集或分片集群上是有多文档事务的，单机服务端会拒绝；从这里开事务目前还没做。见
  [连接](connections.zh-CN.md#mongodb-属性)。

## 依赖实现的行为

语法在各处都一样，但有几件事由实现决定，文档在相关处会说明：`upsert` 与 `insert ... if absent` 的冲突处理、
更新与删除能不能加 limit、以及“行不存在”的含义。各页面写的是 MySQL 的行为，而不是假装它放之四海皆准；MongoDB
自己的答案在上面 [MongoDB](#mongodb) 一节。
