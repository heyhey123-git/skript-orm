# 为 Xiaojie ORM 贡献代码

[English version](CONTRIBUTION.md)

Xiaojie ORM 是一个把数据库操作暴露为 Skript 元素的 Skript 扩展。项目处于开发初期：公开 API 和内部结构
都仍可能调整。单元测试与可选的 MySQL 集成测试覆盖了当前行为（见第 8 节）。

我们遵循三个原则：**适度抽象**、**可读性优于技巧**、**对外接口小而友好**。当某处改动与原则冲突时，
以原则为准。

---

## 1. 项目结构

| 模块                            | 职责                       |
|-------------------------------|--------------------------|
| `core`                        | 抽象、公开 API，以及全部 Skript 集成 |
| `generic-jdbc-implementation` | 通用 JDBC 行为，含 MySQL 方言    |
| `postgresql-implementation`   | PostgreSQL 专属 JDBC 行为    |
| `mongodb-implementation`      | MongoDB 行为               |
| `rocksdb-implementation`      | RocksDB 行为               |

根项目负责产出 shadow 插件 jar。`-PbundleModules=a,b` 选择打包哪些实现，默认只打包
`generic-jdbc-implementation`。

```bash
./gradlew build                                  # shadow jar 输出到 build/dist
./gradlew build -PbundleModules=generic-jdbc-implementation,postgresql-implementation
```

### 边界规则

`core` 不得依赖任何驱动、连接池或查询语言。它描述数据库**能做什么**；实现负责**怎么做**——自己的驱动、
客户端、连接生命周期和资源释放。

如果你发现需要在 `core` 里引用 JDBC、MongoDB 或 RocksDB 的类型，那说明抽象层次不对。应当扩宽抽象，
而不是把依赖泄露进去。

---

## 2. 域值与存储值

每个值都有两种形态：

- **域值**：Skript 作者直接使用的值，例如 `UUID`、`ItemStack`、Skript `Timespan`；
- **存储值**：后端实际保存的值，例如字节数组或 `Blob`。

`ValueConverter<D, S>` 负责两者之间的转换。**转换只在存储边界发生一次：**

- `toStorage()` 由实现在绑定参数时调用。在 JDBC 中即 `JdbcParameterBinder.bindValue()`，仅此一处。
- `fromStorage()` 由实现在读取列时调用。在 JDBC 中即 `JdbcDataCursor.get()`。

因此 `core` 与 `Queries` API 传递的始终是域值。不要在解析器、section 或查询对象里提前转换：重复转换
会静默损坏数据，因为下一层会把已经转换过的值再转换一次。

`DataType<D>` 把 `domainType` 与 `typeCode` 配对，并提供默认的恒等转换器。当后端无法直接存贮域值时，
实现覆盖 `converter`。

基础类型必须用装箱类注册（`Int::class.javaObjectType` 而非 `Int::class.java`），因为运行时的
`Class.isInstance()` 是针对装箱值判断的。

---

## 3. 异常处理

清理阶段的失败绝不能掩盖导致清理的那个失败。

当主操作失败、清理步骤也失败时，把清理异常作为 **suppressed** 附加上去，并重新抛出主异常：

```kotlin
try {
    statement.releaseBoundResources()
} catch (cleanupError: Throwable) {
    error.addSuppressed(cleanupError)
}
```

只有当不存在主失败时，清理异常才成为被抛出的异常。共享的
`PreparedStatement.withBoundResources { ... }` 已实现了这一约定，优先使用它而不是裸 `finally`。

推论：不要吞掉异常。只有资源本来就在销毁、且失败已无法被处理时，`catch (_: Throwable) { }` 才是可接受的。

---

## 4. 资源归属

每个实现端到端地拥有自己的资源，并按归属顺序释放。

JDBC 的链条是 `DataSource → Connection → PreparedStatement → ResultSet`，再加上绑定到 statement 的
已转换 `Blob` 值。由此有两条结论：

- **游标会让 statement 和 connection 保持存活。** 绑定资源在游标关闭时释放，而不是 `execute()`
  返回时。`JdbcDataCursor.close()` 是幂等的，并且按顺序释放。
- **批处理不管理事务。** `JdbcInsertMany` 不碰 `autoCommit`、`commit()` 或 `rollback()`。事务语义属于
  调用方或连接池。

`Database.tables` 只在注册成功后才发布；连接失败不得留下任何中间状态。

---

## 5. 写入语义

写入是**补丁**，不是整行替换。被省略的列不会出现在语句中，因此保留原值或采用数据库默认值。

这个区分很重要，因为 Skript 的列表变量无法表示「键存在但值为 null」——把键设为 null 时 Skript 会删除该键。
因此：

- 内联 `column: value` 语法可以用字面量 `null` 表达 SQL NULL，并按 NULL 绑定。
- 列表变量做不到。缺失的键意味着**未提供**，而不是 NULL。
- `insert many` 是唯一例外：一条语句只能绑定一组列，因此读取器会把所有行补齐到公共列集合。

不要通过「把缺失的键读作 null」来"修复"问题。那会静默清空动态变量恰好遗漏的每一列。

### 影响行数的统计

`WriteResult(affectedCount, countExact)` 表示**已知**受影响的行数，以及该数字是否为精确总数。

对 JDBC 批处理而言，`SUCCESS_NO_INFO` 表示驱动无法报告该命令的影响行数。它**不是**一行：不要改动
`affectedCount`，只需把 `countExact` 置为 `false`。`EXECUTE_FAILED` 是错误。其他负数计数属于驱动的非法
响应，必须抛异常。

---

## 6. Skript 元素

元素类以 `Sec*`（section）、`Eff*`（effect）、`Expr*`（expression）命名，位于
`core/.../skript/elements/` 下。

- `SecSelectBase` 与 `SecWriteBase` 承载共享的解析与派发逻辑。`SecCreateConnection` 与
  `SecRegisterTable` 独立实现，因为它们不符合这两种形态。
- 写入是异步的。`and wait` 标签决定后续内容是否等待：等待式写入会保留事件 continuation，并通过
  `last database error` 暴露失败；而即发即弃式写入会立刻继续，后续失败只能写日志。
- 所有值都在主线程、派发之前解析完成，此时局部变量仍附着在事件上。
- 解析期问题用 `Skript.error(...)` 报告；运行期问题通过 `ErrorPrinter` 与 `SkriptDatabaseErrors` 报告。

---

## 7. 代码风格

风格只在根目录的 `.editorconfig` 中定义一次，IntelliJ IDEA 与 ktlint 都读取它。不要在构建脚本里重复
这些规则。

```bash
./gradlew ktlintCheck     # 报告问题；check 任务也会运行它
./gradlew ktlintFormat    # 自动修复所有可机械修复的问题
```

要点：

- 4 空格缩进、LF、UTF-8、行尾无空白、文件末尾保留一个换行。
- import 顺序为 `*`、`java.**`、`javax.**`、`kotlin.**`。禁止通配导入，但 `.editorconfig` 中允许的
  两条路径例外：`java.util.*` 与 `ch.njol.skript.doc.*`。
- 多行参数列表不写尾随逗号。
- 父类型列表与类型参数上界写作 `Foo : Bar`。
- **代码、KDoc 与注释使用英文。** 中文可以出现在文档和 `build.gradle.kts` 中。源码内保持单一语言，
  便于外部贡献者阅读。
- 较长的 `@Description` 字符串与 `executeWrite` 重写允许超过常规宽度，因此 `max_line_length`
  被有意关闭。`class-signature` 与 `function-signature` 两条规则同理关闭：签名写成一行还是多行，
  由作者决定。

公开 API，以及任何不够直觉的决定，都应当有 KDoc。如果某个设计选择看起来像错误——为什么缺失的键不等于
NULL、为什么批处理不提交、为什么清理失败要作为 suppressed——把原因写在它旁边。

### IntelliJ IDEA 中的格式化

`.editorconfig` 始终是唯一事实来源，IDEA 会原生读取它。`.idea/codeStyles/` 中的项目级代码风格是它在
IDE 格式化器中的镜像，`codeStyleConfig.xml` 则让 IDEA 从自身默认值切换到项目方案。这样 *Reformat
Code* 与 `./gradlew ktlintCheck` 的结果一致。

`.idea/` 下只跟踪每位贡献者都需要的配置：`codeStyles/`、`inspectionProfiles/`、`dictionaries/`、
`kotlinc.xml`、`ktlint-plugin.xml`、`vcs.xml` 以及 `.idea/.gitignore`。其余文件保持忽略，因为它们
保存的是个人状态（`workspace.xml`、`shelf/`）、本机 JDK 名称（`misc.xml`），或 IDEA 导入时会重新
生成的 Gradle 与编译器配置。

全新克隆后请执行一次 **File → Sync Project with Gradle Files**。如果 IDEA 的格式化结果仍不一致，检查
**Settings → Editor → Code Style → Kotlin**，确认顶部的方案选择器是 **Project**。

---

## 8. 测试

测试分为三层，由三个独立的 Gradle 任务运行。

**单元测试**（`core`、`generic-jdbc-implementation`）随每次构建运行，不需要网络和 Docker，覆盖的是契约
而不是行覆盖率：SQL 渲染、参数绑定、游标资源归属、快照语义、标识符校验，以及全局 `Database` 生命周期。
JDBC 接口用 MockK 模拟。

```bash
./gradlew test
```

**Skript 测试**（`core`）用 Skript 真实的配置解析器覆盖一个 section 的解析阶段——这正是单元测试刻意不放到
classpath 上的部分：

```bash
./gradlew :core:integrationTest
```

覆盖 `values` 与 `where` 块：头部识别、模式与取反、格式错误的值、行与单值混用，以及“字面量 `null` 与省略列
必须可区分”这一契约。可达范围止于表达式求值，因为解析真实值表达式需要 Skript 的语法注册表，而它只存在于
运行中的 Skript 里。

**MySQL 测试**（`generic-jdbc-implementation`）让真实实现连接真实 MySQL。它属于可选任务，因为需要容器
运行时且耗时更长：

```bash
./gradlew :generic-jdbc-implementation:integrationTest
```

每个测试 JVM 会启动一个 `mysql:8.4` 容器。若想复用已有的 MySQL，可以把测试指向它；该数据库必须专用于
测试，因为测试会删除并重建自己使用的表：

```bash
./gradlew :generic-jdbc-implementation:integrationTest \
  -Pxiaojie.test.mysql.url="jdbc:mysql://localhost:3306/xiaojie_orm_test"
```

同样的设置也可以从环境变量读取：`XIAOJIE_TEST_MYSQL_URL`、`XIAOJIE_TEST_MYSQL_USERNAME`、
`XIAOJIE_TEST_MYSQL_PASSWORD`、`XIAOJIE_TEST_MYSQL_DRIVER`、`XIAOJIE_TEST_MYSQL_IMAGE`。当既没有
Docker 也没有外部服务器时，MySQL 测试会带着原因中止，而不是静默通过。

集成测试的 classpath 刻意等同于“没有服务端的插件运行时”：包含 Paper、Skript 和 NBT API，因为
`JdbcDataTypes` 在初始化时会解析这些类。一旦这一点不再成立，`JdbcRuntimeClasspathIntegrationTest`
会立刻报错。

集成测试方法请写成 `= runBlocking<Unit> { ... }`。JUnit 只会发现返回 `void` 的 `@Test` 方法，而
`assertFailsWith`、`assertNotNull` 这类辅助函数会返回值；否则表达式体测试会被编译、却永远不执行、也不会
出现在任何报告里。

### 为什么没有“模拟服务端”方案

MockBukkit 装不下 Skript。它加载插件的方式是生成主类的子类，因此 `final` 的主类根本无法加载，而 Skript 的
主类正是 final。本插件也无法单独加载：`plugin.yml` 声明了 `depend: [Skript]`，且 `onEnable` 会调用
`Skript.registerAddon`。因此这里完全没有“模拟服务端”的测试：完整启动插件（元素注册、脚本执行）仍然需要真实
服务端，那是 `run-paper` 的职责，目前尚未接入。

Skript 测试里仍然用到 MockBukkit，但只把它当作一个 Bukkit 服务端：Skript 通过
`Bukkit.getConsoleSender()` 记录日志，配置解析器也通过它上报问题，没有服务端时解析器会 NPE 而不是返回节点。

---

## 9. 新增一个数据库实现

1. 新建模块并加入 `settings.gradle.kts`。
2. 实现 `Database`：`doConnect`、`doDisconnect`、`doRegisterTable` 与 `dataTypes`。
3. 实现 `Queries`，为每种操作构造对应的查询对象。
4. 为每个受支持的逻辑类型实现 `DataType`，在后端需要不同表示时覆盖 `converter`。
5. 实现 `DatabaseFactory`，并在 `init` 块中注册到 `DatabaseRegistry`。
6. 在 `XiaojieOrm.onEnable()` 的候选列表中加上工厂类名，让插件加载它。

实现可以把某个操作标记为不支持，但不得把它静默降级成另一个操作。

---

## 10. 我们有意避免的做法

- **投机式抽象。** 不要为一个实现引入接口，也不要为一个配置项引入配置对象。两行相似代码好过一个过早的框架。
- **按操作重复配置。** 横切关注点应当只有一处。例如查询超时，不该由每个查询各自配置。
- **以"以后可能用到"为由保留无用代码。** 直接删除，Git 记得住。
- **让 linter 充当设计权威。** 格式交给自动化，设计由人评审。工具报出问题应当促使你思考，而不是命令你重构。
- **改变从未声明过的行为。** 如果需要新的保证——批处理的原子性、查询的顺序性——先在 API 中写明，再实现。

---

## 11. 提交信息

标题使用祈使句。当改动原因不够直观时，在正文里说明**为什么**。不相关的改动请拆成不同提交。
