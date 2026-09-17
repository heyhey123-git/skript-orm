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

根项目负责产出 shadow 插件 jar。`-PbundleModules=a,b` 选择打包哪些实现，默认只打包
`generic-jdbc-implementation`。

```bash
./gradlew build                                  # shadow jar 输出到 build/dist
./gradlew build -PbundleModules=generic-jdbc-implementation,postgresql-implementation
```

### wiki 镜像

`docs/` 是源，仓库 wiki 只承载其中的中文部分，作为阅读副本。改动页面的 push 会触发
`.github/workflows/wiki.yml`，它用 `scripts/publish-wiki.ps1` 渲染页面，再提交到 wiki 仓库。页面名以及页与页
之间的每条链接都来自那份脚本开头的映射表，所以新增一页就是在表里加一行、再到侧栏加一行。wiki 不手工编辑，
下一次同步会覆盖掉改动。

这个 workflow 需要一个已经存在的 wiki。在新的克隆上，先打开一次 wiki 并手工建一页，否则
`<repository>.wiki` 没有东西可以检出。

### 边界规则

`core` 不得依赖任何驱动、连接池或查询语言。它描述数据库**能做什么**；实现负责**怎么做**——自己的驱动、
客户端、连接生命周期和资源释放。

如果你发现需要在 `core` 里引用 JDBC 或 MongoDB 的类型，那说明抽象层次不对。应当扩宽抽象，
而不是把依赖泄露进去。

### 版本管理

所有版本号都集中在 `gradle/libs.versions.toml`。各模块不再重复写版本字面量，因此升级 Paper 或
Skript 只需改一个文件里的一行，测试所运行的产物也不会与插件编译所依赖的产物脱节。

有两项不能各自单独移动：

- `core/src/main/resources/plugin.yml` 的 `api-version` 必须与 `paper` 属于同一条发布线。Paper 会
  拒绝加载 `api-version` 高于服务端的插件，所以这里永远不能声明得比编译所用的 API 更新；但声明得
  *更低* 更糟——Paper 会在缺少所调用方法的服务端上照常加载插件，问题要到很久之后才以
  `NoSuchMethodError` 的形式暴露。两者必须在同一个提交里一起改。
- `mockbukkit` 的 artifact 名自带 Paper 发布线（`mockbukkit-v26.2`），因此它也要跟着 `paper` 走。

目录里只应放 Paper 的稳定版构建。更新的发布线如果仍以 ALPHA 发布，就不算升级。

Skript 的最低版本要求由 `XiaojieOrm.onEnable` 在运行时检查，而不是写在 `plugin.yml` 里：原因见
`MINIMUM_SKRIPT_VERSION` 上的 KDoc——带版本号的 `depend` 条目根本无法生效。

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

每个元素保留自己的 pattern，并对外提供一个 `register(addon)` 函数；插件启用时由
`skript/ElementRegistrations.kt` 统一调用。注册只发生在这份清单里，因为 Skript 是在加载脚本时读取语法
注册表的。`Skript.registerSection`、`registerEffect`、`registerExpression` 已弃用，取而代之的是把
`SyntaxInfo` 注册到 addon 自己的 `SyntaxRegistry` 上——`skript/utils/SkriptSyntax` 就是对它的薄封装。
新增元素时必须同时加进那份清单：服务端测试会逐个驱动它期望的元素，所以「忘记注册」会在那里失败，而不是
悄悄不存在。

section 只有在该行以冒号结尾时才会被识别，因此 body 可省略的元素要写成行尾带冒号、下面留空。Skript 会在
日志里记一条 `Empty configuration section!`，然后照常执行该 section。这些提示是预期行为，不是缺陷。

- `SecSelectBase` 与 `SecWriteBase` 承载共享的解析与派发逻辑。`SecCreateConnection` 与
  `SecRegisterTable` 独立实现，因为它们不符合这两种形态。
- 一条语句用哪条连接由 `ConnectionScope` 决定：最内层 `in connection` 作用域、本事件的
  `use connection`、最后是默认连接。作用域按事件保存（与 `last database error` 同一形状），所以函数
  调用会继承它，`wait` 之后的续接也仍然看得见它。
- `SecInConnection` 的主体是代码，因此用 `loadCode` 装载；装载同时把它登记进解析器的
  current sections，`exit`、`stop` 提前离开主体时才会通知到它。主体末尾还要接一个自己的
  `TriggerItem`，因为 Skript 没有“主体结束”回调，而这个节点的存在与否决定了作用域会不会泄漏。
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

测试分为四层，由四个独立的 Gradle 任务运行。

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

`MinimumSkriptVersionTest` 刻意不覆盖我们自己的代码，而是固定 Skript 自身
`ch.njol.skript.util.Version` 的排序行为——`XiaojieOrm.onEnable` 里的版本下限依赖它。这个下限只是一次
比较，而它两种出错方向都无法从本仓库的代码里看出来。

**JDBC 测试**（`generic-jdbc-implementation`）让真实实现连接真实 MySQL，并让每个受支持类型走一遍自己的转换器
往返。转换器那一半不需要数据库，任何环境都能跑；数据库那一半属于可选任务，因为需要容器运行时且耗时更长：

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

集成测试的 classpath 刻意等同于“没有服务端的插件运行时”：包含 Paper 与 Skript，因为 JDBC 类型注册表在初始化
时会解析这些类；一旦这一点不再成立，`JdbcRuntimeClasspathIntegrationTest` 会立刻报错。NBT 正是这个运行时用来
证明的例外：本插件不编译依赖任何 NBT 实现，这里也没有 SkBee，所以注册表必须能在没有它的情况下初始化。因此
`NBT_COMPOUND` 在这里没有往返测试；覆盖 NBT 值的是安装了 SkBee 的 Skript 服务端测试。

NBT 是本插件唯一在运行期依赖其它插件的部分。能给脚本提供写法来构造 compound 的是 SkBee，而它把自己的 NBT 库
重定位到自己的包下，所以本插件既不编译依赖任何 NBT 实现、也不在代码里写死某一个实现：`NbtSupport` 按名字查找
SkBee 的类，用 `MethodHandle` 链接一次，并以 SNBT 作为进出的交换格式。这也是 `softdepend` 里列出 SkBee、缺少
SkBee 时注册 `nbtcompound` 列会被明确拒绝，以及服务端测试安装 SkBee 而不是独立 NBT API（本插件不支持后者）的
原因。

集成测试方法请写成 `= runBlocking<Unit> { ... }`。JUnit 只会发现返回 `void` 的 `@Test` 方法，而
`assertFailsWith`、`assertNotNull` 这类辅助函数会返回值；否则表达式体测试会被编译、却永远不执行、也不会
出现在任何报告里。

**Skript 服务端测试**（根项目）会启动一个真实的 Paper 服务端，加载 Skript 与本插件打出的 shaded jar，然后
检查插件在其中的行为。这是唯一能覆盖「元素注册」与「真实脚本的解析阶段」的层次，因为两者都需要一个正在运行
的 Skript：

```bash
./gradlew serverTest
```

与 MySQL 那一层一样，它是可选任务：会往 Gradle 用户目录下载 Paper jar（之后的运行会复用），并启动一个
Minecraft 服务端。它运行的版本取自版本目录里的 `paper`，因此是插件编译所针对的同一条 API 线与同一个构建，
而不是仅仅「兼容」的版本。

`server-test/skript/` 负责驱动这些元素：`elements/` 下每个元素一个文件，因此失败时从文件名就能看出是哪个
元素。这里刻意不连接任何数据库：每个元素都应当停在数据库查找这一步，并通过 `last database error` 报告
`No database connected.`，这样所有元素无需数据库服务端也能真实跑完。每个元素都会输出一行
`XIAOJIE_SELFTEST`，`serverTest` 任务拿这些行与 `build.gradle.kts` 里的清单核对。Skript 无法解析的语句会被
报错并跳过，所以「某个 pattern 不再注册」会表现为缺少一行，而不是悄悄通过。

`docs/examples/` 也会被复制进同一个服务端，因此文档页面上的 Skript 片段同样要过解析这一关：插件不认的语句
会让构建失败，而不会一路送到读者眼前。这些示例都写成命令而不是触发器，所以在测试服务端上不会真的执行。

Minecraft 服务端的两个特性决定了脚本的写法：

- 实际工作放在周期触发器里，因为服务端尚在启动时 Skript 的 `on script load` 不会触发
  （SkriptLang/Skript#5754），所以任何依赖脚本加载的写法都用不上。
- 每个元素跑完就把自己记进 `{xiaojie::selftest::done::*}`，全部到齐后 `99-finish.sk` 停服。它的第二个
  触发器负责在「始终没到齐」时停服，这正是把「卡住的运行」变成「失败的运行」的机制；它不使用本插件的语法，
  因此当出问题的正是插件本身时它照样能跑。也正因如此，`prepareServerTest` 会删掉 Skript 的数据目录：元素的
  运行守卫存在那里，留下一个会让下一次运行跳过某个元素。

`prepareServerTest` 负责写入运行目录 `build/server-test`：`server.properties`、测试脚本，以及 `eula.txt`。
写入最后这个文件意味着为这个一次性测试服务端接受 Minecraft EULA——这也是由任务而非开发者去做的原因。

同一批元素脚本也可以对着数据库跑。传入 JDBC 测试使用的那组 MySQL 属性（`-Pxiaojie.test.mysql.url`、
`username`、`password`）后，会额外铺上 `server-test/database/`：一个由 `prepareServerTest` 写入凭据的 setup
脚本（连接并注册它自己的表），以及一个 roundtrip 脚本（写入一行、读回、比对）。此时元素脚本面对的是真实连接
——这也是唯一能覆盖 `values` / `where` 里「脚本值 → 列」转换的方式：转换发生在数据库查找之后，没有数据库的
运行永远走不到那里。

这带来两个写法上的后果。有连接之后，每个元素报告的内容取决于它走到了哪一步，所以该模式下它们的期望消息为空、
只检查「是否出现」，真正的断言由 roundtrip 承担。另外 setup 与 roundtrip 脚本同时保留「启动守卫」和「完成
标记」——两者不是一回事：它们的步骤会在数据库上等待，没有守卫的话，第二次触发会在第一次还没做完时开始，把同一
张表注册两次。

### 持续集成

`.github/workflows/ci.yml` 运行四个 job：

- `test`：跑单元测试、Skript 测试与 `ktlintCheck`，不需要任何外部依赖。
- `mysql-installed`：对着 runner 镜像**已经装好**的 MySQL 跑 JDBC 集成测试，用 `systemctl` 启动它。这样
  完全不产生镜像拉取，并且把镜像自带的版本（目前是 8.0）与另一个 job 固定的 8.4 并排放进矩阵，一次拉取覆盖
  两个服务端版本。连接信息通过 `-P` 属性而不是环境变量传入，因为 Gradle 属性每次调用都会重新传递，即使
  守护进程是更早启动的。
- `mysql-testcontainers`：不配置任何服务器，跑同一套测试。这正是开发者本地的路径，因此 Docker 探测与固定的
  `mysql:8.4` 镜像也会被一并验证。它只在默认分支和手动触发时运行，因为容器镜像是缓存唯一帮不上忙的东西。
- `skript-server`：启动上面描述的那个 Paper 服务端，检查插件在其中的表现。它既不需要 Docker 也不需要数据库，
  所以每次改动都会运行。它下载的服务端 jar 落在 Gradle 用户目录里，而 Gradle 状态缓存已经覆盖了那里，因此
  这个 job 不需要自己的缓存。

另外两个工作流负责打包，它们都不判断代码是否正确——那是 `ci.yml` 的事。

`build.yml` 在每次分支 push 时运行，把 shaded jar 作为该次运行的 artifact 上传；测试人员下载的就是它，用来
试当前代码的状态。artifact 属于那一次运行：随运行过期，也不构成任何承诺。工作流从 `gradle.properties` 读取
版本号，只用于给 artifact 命名，因此该名字与 jar 自身的名字不可能不一致。

`release.yml` 手动触发、没有输入，发布一个 GitHub Release。它读取 `gradle.properties` 里的版本号，并拒绝任何
不是纯 `x.y.z` 的值：release 记录的是已提交的内容，快照不能被当作正式版发出去。随后它会在即将打标签的那个提交
上跑与 `ci.yml` 相同的测试层（包括真实服务端测试），检查构建出的 jar 是否携带要发布的版本号，在它旁边写一份
`sha256`，最后在该提交上创建标签与 release。它被限制在默认分支；要在分支上发布，删掉那一个条件即可。

所以发一次版要先动仓库三处：`gradle.properties` 里的版本号、push、然后手动触发。这正是目的所在——仓库里的
`1.0-SNAPSHOT` 意味着「不是正式版」，工作流也是这么理解的。

#### CI 什么时候运行

`push` 与 `pull_request` 会忽略那些不可能影响构建的改动：Markdown、`.gitignore` 和 `.idea/`。其余任何改动
都会触发 CI，包括 `.editorconfig`——它看起来无关，但 ktlint 读的正是它。`workflow_dispatch` 不受过滤器
影响，所以随时可以手动强制跑一次。`build.yml` 用的是同样两个过滤器，因此不可能影响构建的改动既不会跑测试、
也不会打包。两者都忽略标签：`release.yml` 打的标签指向它已经构建并测试过的提交。

这里刻意用「忽略清单」而不是「允许清单」。允许清单一旦漏掉某个新的构建输入，CI 就会从此对真实改动静默不跑，
而「没跑的检查」和「跑过的检查」在界面上长得一模一样。

有一个后果需要记住：当 PR 只改动了被忽略的文件时，整个工作流不会运行，也就不会上报任何状态。如果将来在分支
保护里把这些检查设为**必需**，这样的 PR 会一直等待一个永远不会到来的检查。届时的正确做法是让工作流无条件
运行，在内部用条件控制那些昂贵的 job，而不是在触发器上做过滤。

数据库测试只在有人记得起容器时才跑，是没人会信任的测试。任何只能对着真实服务端才能验证的类型、查询或转换器，
都应该放进有服务端的那个 job。

现在所有 MySQL 测试在服务器不可达时都只会以普通连接错误失败，因此可以在没有数据库的情况下检查这套测试是否
存在执行顺序问题：

```bash
./gradlew :generic-jdbc-implementation:integrationTest \
  -Pxiaojie.test.mysql.url="jdbc:mysql://127.0.0.1:1/xiaojie_orm_test"
```

任何不是连接错误的失败都是真实缺陷——生命周期测试里那个潜在的顺序依赖就是这样被找出来的。

失败的用例还会由 `.github/actions/annotate-test-failures` 写成 check annotations。通过 API 读取 job 日志
需要管理员权限，而 annotations 是公开的；HTML 报告则是需要认证才能下载的 artifact。所以 annotation 是让远程
失败可诊断的关键：每条都带有用例名和断言消息，足以定位到具体是哪一列、哪个值不一致。

#### CI 缓存了什么

`gradle/actions/setup-gradle` 会在每次运行之间恢复 Gradle User Home：Gradle wrapper 发行包、所有已解析的
依赖，以及 Gradle 首次使用时生成的 API jar。这正是避免每次重走安装流程的关键。不要给 `setup-java` 加
`cache: gradle`，也不要为 Gradle User Home 另加 `actions/cache` —— 该 action 的文档明确说明两者都会与它的
缓存机制冲突。

缓存提供方为 `enhanced`，也就是该 action 的默认值。它按 job 生成缓存键，所以三个 job 不会争抢同一条条目。
第一次 CI 用的是 `basic`（MIT 实现），而它恰好暴露了这一点：`basic` 只用构建文件计算键，于是三个 job 算出
同一个键，除第一个之外全部保存失败，报 "Unable to reserve cache ... another job may be creating this
cache"。最终存下来的那条只包含最先结束的那个 job 的依赖，其他 job 每次都要重新下载自己那部分。`enhanced`
是专有组件，对公开仓库免费、对私有仓库处于预览阶段；如果这个取舍以后变得不可接受，改回
`cache-provider: basic` 只需一行。

缓存只会由默认分支写入，其他运行从它恢复。每个 job 的摘要都会报告恢复了什么、保存了什么。

有两样东西是刻意不缓存的：

- **Gradle 构建缓存。** 开启 `org.gradle.caching` 也会让 `Test` 任务可被缓存，而“从缓存得到的测试结果”并不
  等于“测试真的跑过”。本项目的编译耗时相对于下载量而言微不足道。
- **MySQL 容器镜像。** GitHub 无法缓存 Docker 镜像，所以 `mysql-testcontainers` 每次参与运行都会拉取
  `mysql:8.4`。这正是另一个数据库 job 改用 runner 镜像自带 MySQL、而不是 service 容器的原因：那个 job 一次
  都不拉取；它还被限制为只在默认分支运行，好让 PR 只付一次拉取而不是两次。`mysql-testcontainers` 另外被标记
  为 `cache-read-only`：它与 `mysql-installed` 解析同一批依赖，让它写入只会多出一份大条目，并可能挤掉共享
  条目。

顺带说明：用 `actions/cache` 缓存 `docker save` 出来的 tar 并不是出路——tar 未压缩，比它替代的那次拉取更大，
还会和 Gradle 条目抢同一份缓存配额，并且每个 job 都得加 shell 胶水。

`ubuntu-latest` 已自带 JDK 25，所以 `actions/setup-java` 通常什么都不装。如果将来的镜像移除了它，action 会
下载 JDK——那是 Gradle 缓存唯一帮不上忙的一项下载。

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
