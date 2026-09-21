# 兼容性

**简体中文** | [English](compatibility.md)

## 版本

| | |
| --- | --- |
| **Paper** | 26.2 或更高。插件声明 `api-version: '26.2'`，并针对该服务端版本系列编译。 |
| **Skript** | 2.16.2 或更高。版本过低时，插件会记录原因并自行禁用，因此可能显示为已安装但未启用。 |
| **Java** | 使用 Paper 26.2 所需的 Java 版本，插件也针对该版本构建。 |
| **MySQL** | 内置实现面向 MySQL，并在 CI 中通过 MySQL 8 测试。MariaDB 等 MySQL 兼容服务端尚未测试。 |
| **PostgreSQL** | 通过 `"PostgreSQL"` 支持。CI 既测试实现本身，也在真实服务端上运行完整插件测试。 |
| **MongoDB** | 通过 `"MongoDB"` 支持，并在 CI 中通过 MongoDB 8 测试。驱动与 PostgreSQL 一样，在首次启动时下载。 |
| **SkBee** | 可选，仅 `nbtcompound` 列需要。 |

## 脚本能写的类型名

`create a connection to database "..."` 接受实现的**类型名**，必须精确匹配，且区分大小写。发布的 jar 注册了四种类型：

| 类型名 | 它是什么 |
| --- | --- |
| `"MySQL"` | MySQL 方言，使用服务端已有的 MySQL 驱动。这里的产品名与类型名相同，但两者含义不同。 |
| `"PostgreSQL"` | PostgreSQL 方言，驱动由插件下载。见 [jar 里有什么](#jar-里有什么)。 |
| `"MongoDB"` | 通过插件下载的阻塞式 MongoDB Java 驱动访问 MongoDB，不使用 SQL。部分语句的行为有所不同，见 [MongoDB](#mongodb)。 |
| `"JDBC"` | 通过 `driver` 属性指定驱动，使用通用 SQL 方言：`"双引号"` 标识符、单行查询的 `LIMIT 1`、分页的 `LIMIT ? OFFSET ?`。不具备通用写法的操作不受支持。 |

其他名称会在 section 执行时被拒绝，报 `Database '<名字>' is not supported.`。`"MariaDB"` 和 `"SQLite"` 是产品名，不是已注册的类型名，详见下方“数据库产品”。`"MongoDB"` 与 `"MySQL"` 则既是产品名，也是类型名。

## jar 里有什么

发布的 jar 包含插件本体、Kotlin 运行时、连接池，以及本仓库的**所有实现模块**：MySQL 方言、通用 JDBC、PostgreSQL 和 MongoDB。它**不包含数据库驱动**，驱动按以下方式提供：

- MySQL 和通用 JDBC 使用服务端已有的驱动。Paper 自带 MySQL Connector/J，插件可直接使用，无需另行安装。`"JDBC"` 则用于访问服务端 classpath 上、未由本插件专门提供的驱动。
- PostgreSQL 与 MongoDB 的驱动声明在 `plugin.yml` 的 `libraries` 中，由 Paper 在首次启动时下载到服务端的 `libraries/` 目录，再加载到插件 classpath。清单根据 jar 包含的模块生成，默认构建列出 `org.postgresql:postgresql:42.7.11` 和 `org.mongodb:mongodb-driver-sync:5.6.1`；若所选模块均无需下载驱动，则为 `libraries: []`。下载源由**服务端**的 Maven Central 镜像设置决定：`PAPER_DEFAULT_CENTRAL_REPOSITORY` 或 `org.bukkit.plugin.java.LibraryLoader.centralURL` 系统属性，默认使用 Google 的 Central 镜像。下载后会从本地 `libraries/` 复用，无需每次启动都下载。

Paper 无法解析这些库时，会将其视为致命错误并拒绝加载插件。如果服务端无法访问镜像，可调整上述镜像设置；注意，这会影响该服务端上**所有插件**的库下载。也可以手动准备依赖，较可靠的做法是从已成功启动过的服务端复制整个 `libraries/` 目录。

不将驱动 shade 到 jar 中，可以避免重定位后的副本与下载版本并存。下载的库保留作者发布的 service 文件，版本只需在一处更新，校验和由 Paper 核对。MongoDB 使用阻塞式 `mongodb-driver-sync`，而非 Kotlin 协程驱动，因为本插件会打包并重定位 kotlinx.coroutines；依赖未重定位协程库的驱动会引入另一份运行时。

`-PbundleModules=` 可按任意组合打包模块，包括尚未发布的模块。这是开发用途，不属于受支持的部署配置。`libraries` 清单随所选模块生成，每个 jar 只请求自身需要的驱动。

## NBT compound 与 SkBee

`nbtcompound` 是唯一借助其他插件实现的列类型，目前仅支持 SkBee，原因如下：

- SkBee 为脚本提供 compound 构造语法（`nbt compound from "{...}"`），这里使用的 compound 是 SkBee 对象。
- SkBee 将 NBT 库打包在自己的包名下，不提供独立 NBT API 的类，两者不能混用。

本插件不直接编译依赖任何 NBT 实现，也不将其作为必需的运行依赖。启用时会按名称查找并链接 SkBee 的类，因此：

- **不支持独立 NBT API 插件**；安装它不能替代 SkBee，也不会与本插件冲突。
- **不限定 SkBee 版本，但依赖固定的类名和方法名。** 所用版本必须在 `com.shanebeestudios.skbee.api.nbt` 下提供 `NBTCompound`、`NBTContainer(String)` 与 `NBTContainer(InputStream)` 两个构造方法，以及静态方法 `NBTReflectionUtil.writeApiNBT`。若这些类或方法被移动或更改签名，NBT 功能会不可用；插件会报告原因，不会因此崩溃。
- **未安装 SkBee 时，其他功能仍可正常使用。** 只有声明了 NBT 列的表会在注册时被拒绝，错误信息会提示需要 SkBee。见 [类型](types.zh-CN.md)。

插件描述中的 `softdepend: [SkBee]` 用于让 SkBee 优先加载。这是加载顺序提示，不是强制依赖：没有 SkBee，插件也能启用。

## 数据库产品

本节列出连接可以访问的数据库产品，而非可填写的类型名。类型名以“脚本能写的类型名”中的四种为准，即使两者名称相同，也应区分用途。

| | |
| --- | --- |
| MySQL | 支持。使用 `"MySQL"`，已在 CI 中通过 MySQL 8 测试。 |
| MariaDB | 未测试。可尝试 `"MySQL"`，其语句使用 MySQL 写法，包括 `ON DUPLICATE KEY UPDATE` 及更新、删除中的 `LIMIT`。可能可用，但尚无测试验证。 |
| PostgreSQL | 支持。使用 `"PostgreSQL"`，驱动在首次启动时下载。CI 使用真实数据库测试，也在真实 Paper 服务端上运行完整插件测试。 |
| MongoDB | 支持。使用 `"MongoDB"`，驱动在首次启动时下载。本实现尚不支持事务，`database transaction` section 会报错。已在 CI 中通过 MongoDB 8 测试。 |
| SQLite 等 | SQLite 可通过 `"JDBC"` 和 Paper 自带驱动访问，支持该方言生成的 SQL。服务端测试在两种模式下验证插入、读取、分页、更新和删除。不支持的操作仍包括 auto increment、`insert ... if absent`、`upsert` 及写操作的 limit。其他产品需要另行提供服务端未自带的驱动。 |

## MongoDB

`"MongoDB"` 与 `"MySQL"` 一样，既是产品名，也是类型名。其他页面的语法同样适用，但 SQL 实现中的部分行为不能直接套用。主要区别如下：

- **支持所有声明的列类型。** uuid 存为 BSON binary，date、time、timespan 存为数字，itemstack、location、bukkit-serializable、nbt 存为 BSON binary，不会因存储形式不同而缺少类型支持。
- **auto increment 使用计数器文档实现**，每个自增键对应一份，保存在 `skript_orm_sequences` 集合中。因此该名称为保留名称，不能用作表名。注册表时会创建计数器，并根据集合中已有的最大键值调整，避免再次分配注册前已存在的键。
- **显式指定键时也会更新计数器。** 带键的插入和 `upsert by id` 会按写入的键推进计数器，确保后续生成的键大于它。这与 MySQL 的 `AUTO_INCREMENT` 处理显式插入的方式一致；PostgreSQL 序列则需要手动调用 `setval` 才有同样效果。因此，脚本混用显式键与自增键不会导致重复分配。注册后由其他程序直接写入集合的文档仍可能引起键冲突，此时插入会返回数据库的重复键错误，不会覆盖已有文档。
- **`_id` 是 MongoDB 保留的文档标识字段。** 表不能声明名为 `_id` 的列，否则注册时就会报错并说明原因。
- **`select page` 需要主键**，且始终按主键排序，与 JDBC、PostgreSQL 实现一致。MongoDB 的自然顺序不稳定，无法保证分页结果的顺序。
- **`update`、`update by id` 与 `upsert by id` 返回过滤条件匹配的行数**，而非实际修改的行数。即使写入值与原值相同，该行仍计入结果。
- **`insert if absent` 按键判断，且具有原子性。** 违反唯一索引的写入会被忽略，不会作为错误返回。在 MongoDB 中，这是通过捕获重复键错误实现的，与 MySQL 方言的处理方式相同。
- **`delete ... with limit n` 最多删除 n 行。** MongoDB 没有对应的删除上限参数，因此插件会先选择至多 n 个标识，再删除对应文档。
- **与 null 比较时遵循 MongoDB 规则。** `column = null` 匹配该列为 null **或不存在**的行；`column != null` 匹配该列存在且不为 null 的行。
- **可空性与长度仅作声明，不由服务端强制约束。** 注册会创建主键唯一索引和自增计数器。MongoDB 按需创建集合，创建索引也可能使集合在写入第一份文档之前就已存在。
- **尚未实现事务。** 该实现的 `supportsTransactions` 为 false，`database transaction` section 会报 `This database implementation does not support transactions.`，与其他不支持事务的实现一致。MongoDB 本身支持副本集和分片集群上的多文档事务，不支持独立部署服务端上的事务；本插件尚未提供入口。见 [连接](connections.zh-CN.md#mongodb-属性)。

## 依赖实现的行为

语法相同，不代表所有实现的行为都相同。`upsert` 与 `insert ... if absent` 的冲突处理、更新与删除是否支持 limit，以及“行不存在”的含义，都由具体实现决定。文档会在相关位置注明 MySQL 的行为，避免将其当作通用规则；MongoDB 的区别见上方 [MongoDB](#mongodb)。
