# 兼容性

**简体中文** | [English](compatibility.md)

## 版本

| | |
| --- | --- |
| **Paper** | 26.2 或更高。插件声明 `api-version: '26.2'`，并针对该服务端版本系列编译。 |
| **Skript** | 2.16.2 或更高。版本过低时，插件会记录原因并自行禁用。 |
| **Java** | 使用 Paper 26.2 要求的版本，插件也以此版本为目标构建。 |
| **MySQL** | 通过 `"MySQL"` 支持，使用服务端已有的 MySQL 驱动。已在 CI 中通过 MySQL 8 测试。 |
| **MariaDB** | 通过 `"MariaDB"` 支持，其驱动 MariaDB Connector/J 在首次启动时下载。已在 CI 中通过 MariaDB 11 测试。 |
| **PostgreSQL** | 通过 `"PostgreSQL"` 支持。CI 测试实现本身，并在 PostgreSQL 服务端上运行插件测试。 |
| **MongoDB** | 通过 `"MongoDB"` 支持，并在 CI 中通过 MongoDB 8 测试。驱动与 PostgreSQL 一样，在首次启动时下载。 |
| **SkBee** | 可选，仅 `nbtcompound` 列需要。 |

## 脚本能写的类型名

`create a connection to database "..."` 接受以下五种实现名称，区分大小写：

| 连接类型名 | 驱动与行为 |
| --- | --- |
| `"MySQL"` | 使用 MySQL SQL 语法和服务端已有的 MySQL 驱动。 |
| `"MariaDB"` | 使用 MySQL 系列的 SQL 语法及下载的 MariaDB 驱动。URL 须以 `jdbc:mariadb://` 开头。 |
| `"PostgreSQL"` | 使用 PostgreSQL 专用的 SQL 写法，驱动由插件下载。见 [jar 里有什么](#jar-里有什么)。 |
| `"MongoDB"` | 通过插件下载的阻塞式 MongoDB Java 驱动访问 MongoDB，不使用 SQL。事务和影响行数的行为与 SQL 后端不同，见 [MongoDB](#mongodb)。 |
| `"JDBC"` | 通过 `driver` 属性指定驱动，使用通用 SQL 语法；需要数据库专用 SQL 的操作会被拒绝。 |

其他名称会在执行时被拒绝，报 `Database '<名字>' is not supported.`。SQLite 可通过 `"JDBC"` 访问；`"SQLite"` 不是已注册的类型名。

## jar 里有什么

发布的 jar 包含插件本体、Kotlin 运行时、连接池，以及本仓库的**所有实现模块**：注册 MySQL、MariaDB 与通用类型的 JDBC 模块、PostgreSQL 和 MongoDB。它**不包含数据库驱动**，驱动按以下方式提供：

- MySQL 使用 Paper 自带的 MySQL Connector/J。对于 `"JDBC"`，脚本指定的驱动须已在服务端的 Java 类路径（classpath）中，即服务端启动时就能加载该驱动库。Paper 也自带 SQLite 驱动。
- MariaDB、PostgreSQL 与 MongoDB 的驱动声明在 `plugin.yml` 的 `libraries` 中，由 Paper 在首次启动时下载到服务端的 `libraries/` 目录，再供插件使用。清单根据 jar 包含的模块生成，默认构建列出 `org.mariadb.jdbc:mariadb-java-client:3.4.4`、`org.postgresql:postgresql:42.7.11` 和 `org.mongodb:mongodb-driver-sync:5.6.1`；若所选模块均无需下载驱动，则为 `libraries: []`。下载地址由**服务端**的 Maven Central 镜像设置决定：`PAPER_DEFAULT_CENTRAL_REPOSITORY` 或 `org.bukkit.plugin.java.LibraryLoader.centralURL` 系统属性。镜像是提供同一批依赖文件的下载站；Paper 默认使用 Google 的 Central 镜像。下载后会从本地 `libraries/` 复用，无需每次启动都下载。

Paper 无法下载 `plugin.yml` 中声明的驱动库时，会拒绝加载插件。如果服务端无法访问当前下载站，可设置 `PAPER_DEFAULT_CENTRAL_REPOSITORY` 或 `org.bukkit.plugin.java.LibraryLoader.centralURL`；所选下载站会影响该服务端上**所有插件**的库下载。也可以手动准备依赖，较可靠的做法是从已成功启动过的服务端复制整个 `libraries/` 目录。

驱动不打包进 jar，而由 Paper 加载发布版本，保留原有的服务声明文件并核对校验和。MongoDB 使用同步驱动 `mongodb-driver-sync`；插件会重定位自身使用的 kotlinx.coroutines，因此不使用依赖该运行时的 Kotlin 协程驱动。

开发构建可用 `-PbundleModules=` 选择打包的模块，生成的 `libraries` 清单只包含所选模块需要的驱动。自定义模块组合不属于正式支持的发布配置。

## NBT compound 与 SkBee

NBT compound 是一组有名称的 NBT 值。`nbtcompound` 是唯一需要其他插件的列类型，目前仅支持 SkBee，原因如下：

- SkBee 为脚本提供 compound 构造语法（`nbt compound from "{...}"`），这里使用的 compound 是 SkBee 对象。
- SkBee 将 NBT 库打包在自己的包名下，不提供独立 NBT API 的类，两者不能混用。

本插件不直接编译依赖任何 NBT 实现，也不将其作为必需的运行依赖。启用时会按名称查找并链接 SkBee 的类，因此：

- **独立的 NBT API 插件不能替代 SkBee**，但也不会与本插件冲突。
- **不限定 SkBee 版本。** 插件会在 `com.shanebeestudios.skbee.api.nbt` 下查找 `NBTCompound`、`NBTContainer(String)` 和 `NBTContainer(InputStream)` 构造方法，以及静态方法 `NBTReflectionUtil.writeApiNBT`。如果 SkBee 移动或更改这些 API，NBT 列将不可用，插件会报告原因。
- 未安装 SkBee 时，包含 `nbtcompound` 列的表无法注册，其他功能仍可使用。见[类型](types.zh-CN.md)。

插件描述中的 `softdepend: [SkBee]` 用于让 SkBee 优先加载。这是加载顺序提示，不是强制依赖：没有 SkBee，插件也能启用。

## 数据库产品

下表列出支持的数据库产品。建立连接时，请填写[脚本能写的类型名](#脚本能写的类型名)。

| | |
| --- | --- |
| MySQL | 支持。使用 `"MySQL"`，已在 CI 中通过 MySQL 8 测试。 |
| MariaDB | 支持。使用 `"MariaDB"`，驱动在首次启动时下载。插件生成 MariaDB 也支持的 MySQL 系列 SQL，例如 `ON DUPLICATE KEY UPDATE`，以及更新和删除中的 `LIMIT`。已在 CI 中通过 MariaDB 11 测试。 |
| PostgreSQL | 支持。使用 `"PostgreSQL"`，驱动在首次启动时下载。CI 使用 PostgreSQL 和 Paper 测试。 |
| MongoDB | 支持。使用 `"MongoDB"`，驱动在首次启动时下载。本实现尚不支持事务，`database transaction` 会报错。已在 CI 中通过 MongoDB 8 测试。 |
| SQLite 等 | SQLite 可通过 `"JDBC"` 和 Paper 自带驱动访问。插入、读取、分页、更新和删除均有测试覆盖。通用 SQL 生成器不支持 `auto increment`、`insert ... if absent`、`upsert` 和写操作的行数限制。访问其他产品需让对应驱动位于服务端的 Java 类路径中。 |

## MongoDB

`"MongoDB"` 与 `"MySQL"` 一样，既是产品名，也是连接类型名。使用 `"MongoDB"` 连接时，脚本仍可使用 `register a database table`、`insert`、`select`、`update` 和 `delete`。MongoDB 实现与 SQL 后端的主要区别如下：

- **支持所有声明的列类型。** UUID、物品、坐标、Bukkit 可序列化值和 NBT compound 存在 MongoDB 文档的二进制字段中；日期、时间和时长存为数字。
- **auto increment 使用计数器文档实现**，每个自增键对应一份，保存在 `skript_orm_sequences` 集合中。因此该名称为保留名称，不能用作表名。注册表时会创建计数器，并根据集合中已有的最大键值调整，避免再次分配注册前已存在的键。
- **显式指定键也会推进计数器。** 插入或 `upsert by id` 时提供键值，后续自动生成的键就会大于该值。脚本因此可以混用显式键与自增键。其他程序在注册后直接写入的文档仍可能引起键冲突；此时插入会报重复键错误，不会覆盖已有文档。
- **`_id` 是 MongoDB 保留的文档标识字段。** 表不能声明名为 `_id` 的列，否则注册时就会报错并说明原因。
- **`select page` 需要主键**，并按主键排序。MongoDB 文档的自然顺序不稳定，不适合直接用于分页。
- **`update`、`update by id` 与 `upsert by id` 返回过滤条件匹配的行数**，而非实际修改的行数。即使写入值与原值相同，该行仍计入结果。
- **`insert if absent` 不会覆盖已有的键。** MongoDB 在插入时检查唯一索引，因此两个并发请求不能创建键相同的两份文档。如果键已存在，插件会捕获重复键错误，保留原有文档。
- **`delete ... with limit n` 最多删除 n 行。** MongoDB 没有对应的删除上限参数，因此插件会先选择至多 n 个标识，再删除对应文档。
- **与 null 比较时遵循 MongoDB 规则。** `column = null` 匹配该列为 null **或不存在**的行；`column != null` 匹配该列存在且不为 null 的行。
- **可空性与长度仅作声明，不由服务端强制约束。** 注册会创建主键唯一索引和自增计数器。MongoDB 按需创建集合，创建索引也可能使集合在写入第一份文档之前就已存在。
- **尚未实现事务。** `database transaction` 会报 `This database implementation does not support transactions.`。MongoDB 本身支持副本集和分片集群上的多文档事务，但本插件尚未提供对应功能。见[连接](connections.zh-CN.md#mongodb-属性)。

## 依赖实现的行为

`upsert` 和 `insert ... if absent` 的冲突处理、写操作的 limit，以及不存在的行如何处理，都取决于连接类型。MySQL 与 MariaDB 共用 `ON DUPLICATE KEY UPDATE`、更新和删除的 `LIMIT` 等 SQL 语法，但驱动和 `insert many` 执行路径不同。MongoDB 的行为见 [MongoDB](#mongodb)。
