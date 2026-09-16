# Contributing to Xiaojie ORM

[中文版](CONTRIBUTION.zh-CN.md)

Xiaojie ORM is a Skript addon that exposes database operations as Skript elements. It is in early
development: both the public API and the internal structure may still change. Unit tests and an
opt-in MySQL integration suite cover the current behaviour (see §8).

The guiding principles are **moderate abstraction**, **readability over cleverness**, and **a small
surface that is pleasant to use**. When a change and a principle disagree, the principle wins.

---

## 1. Project layout

| Module                        | Responsibility                                           |
|-------------------------------|----------------------------------------------------------|
| `core`                        | Abstractions, the public API, and all Skript integration |
| `generic-jdbc-implementation` | Shared JDBC behaviour, including the MySQL dialect       |
| `postgresql-implementation`   | PostgreSQL-specific JDBC behaviour                       |
| `mongodb-implementation`      | MongoDB behaviour                                        |
| `rocksdb-implementation`      | RocksDB behaviour                                        |

The root project builds the shaded plugin jar. `-PbundleModules=a,b` selects which implementations
are bundled; it defaults to `generic-jdbc-implementation`.

```bash
./gradlew build                                  # shadow jar into build/dist
./gradlew build -PbundleModules=generic-jdbc-implementation,postgresql-implementation
```

### The boundary rule

`core` must not depend on any driver, connection pool, or query language. It describes *what* a
database can do. An implementation owns *how* it does it: its driver, its client, its connection
lifecycle, and its resource cleanup.

If you need JDBC, MongoDB, or RocksDB types in `core`, the abstraction is wrong. Widen the
abstraction instead of leaking the dependency.

---

## 2. Domain values and storage values

Every value has two forms:

- a **domain value** — what a Skript author works with, for example a `UUID`, an `ItemStack`, or a
  Skript `Timespan`;
- a **storage value** — what the backend actually stores, for example a byte array or a `Blob`.

`ValueConverter<D, S>` moves between them. **Conversion happens exactly once, at the storage
boundary:**

- `toStorage()` is called by the implementation when it binds a parameter. In JDBC that is
  `JdbcParameterBinder.bindValue()`, and nowhere else.
- `fromStorage()` is called by the implementation when it reads a column. In JDBC that is
  `JdbcDataCursor.get()`.

`core` and the `Queries` API therefore always carry domain values. Do not convert early in a parser,
a section, or a query object: a second conversion is silent data corruption, because the next layer
will convert the already-converted value again.

`DataType<D>` pairs a `domainType` with a `typeCode` and provides a default identity converter.
Implementations override `converter` when the backend cannot store the domain value directly.

Primitive Kotlin types must be registered with their boxed class
(`Int::class.javaObjectType`, not `Int::class.java`), because runtime `Class.isInstance()` checks
against boxed values.

---

## 3. Exception hygiene

A cleanup failure must never hide the failure that caused it.

When a primary operation fails and a cleanup step also fails, attach the cleanup failure as
**suppressed** and rethrow the primary:

```kotlin
try {
    statement.releaseBoundResources()
} catch (cleanupError: Throwable) {
    error.addSuppressed(cleanupError)
}
```

Only when there is no primary failure does the cleanup failure become the thrown exception. The
shared helper `PreparedStatement.withBoundResources { ... }` implements exactly this contract for
statement-scoped binding resources; prefer it over a bare `finally`.

Corollary: do not swallow exceptions. `catch (_: Throwable) { }` is acceptable only when the resource
is already being destroyed and nothing can act on the failure.

---

## 4. Resource ownership

Each implementation owns its resources end to end, and releases them in ownership order.

For JDBC the chain is `DataSource → Connection → PreparedStatement → ResultSet`, plus any converted
`Blob` values bound to a statement. Two consequences:

- **A cursor keeps its statement and connection alive.** Bound resources are released on cursor
  close, not when `execute()` returns. `JdbcDataCursor.close()` is idempotent and releases in
  order.
- **A batch does not manage transactions.** `JdbcInsertMany` does not touch `autoCommit`, `commit()`,
  or `rollback()`. Transaction semantics belong to the caller or to the connection pool.

`Database.tables` is published only after registration succeeds, and a failed connection attempt
must leave no partial state behind.

---

## 5. Write semantics

Writes are **patches**, not whole-row replacements. An omitted column is not part of the statement,
so the stored value or the database default is kept.

This distinction matters because Skript cannot represent "a key set to null" in a list variable —
Skript deletes the key. Therefore:

- Inline `column: value` syntax can express SQL NULL as a literal `null`, and it is bound as NULL.
- A list variable cannot. An absent key means **not supplied**, never NULL.
- `insert many` is the one exception: a single statement binds one column list, so the reader fills
  every row to a common column set.

Do not "fix" a missing key by reading it as null. That would silently clear every column a dynamic
variable happens to miss.

### Counting affected rows

`WriteResult(affectedCount, countExact)` reports how many rows are **known** to be affected, and
whether that number is the exact total.

For JDBC batches, `SUCCESS_NO_INFO` means the driver cannot report a count for that command. It is
**not** one row: leave `affectedCount` alone and set `countExact = false`. `EXECUTE_FAILED` is an
error. Other negative counts are an invalid driver response and must raise.

---

## 6. Skript elements

Element classes are named `Sec*` (sections), `Eff*` (effects), and `Expr*` (expressions), and live
under `core/.../skript/elements/`.

- `SecSelectBase` and `SecWriteBase` hold the shared parsing and dispatch logic.
  `SecCreateConnection` and `SecRegisterTable` are standalone because they do not fit either shape.
- Writes are asynchronous. The `and wait` tag decides whether the continuation waits: a waiting write
  preserves the event continuation and exposes failures through `last database error`, while a
  fire-and-forget write continues immediately and can only log a later failure.
- All values are resolved on the main thread, before dispatch, while local variables are still
  attached to the event.
- Report user-facing parse problems with `Skript.error(...)` during `init`, and runtime problems
  through `ErrorPrinter` plus `SkriptDatabaseErrors`.

---

## 7. Code style

Style is defined once in the root `.editorconfig`, which both IntelliJ IDEA and ktlint read. Do not
duplicate rules in the build script.

```bash
./gradlew ktlintCheck     # reports violations; also runs as part of check
./gradlew ktlintFormat    # fixes everything mechanically fixable
```

In short:

- 4 spaces, LF, UTF-8, no trailing whitespace, one final newline.
- Imports are ordered `*`, `java.**`, `javax.**`, `kotlin.**`. Wildcards are rejected, except for
  the single package the `.editorconfig` allows: `ch.njol.skript.doc.*`. Every other import is
  written out; `java.util` in particular, because ktlint cannot reliably parse more than the first
  entry of that list.
- No trailing commas in multi-line argument lists.
- Colons in supertype lists and type-parameter bounds are written `Foo : Bar`.
- **Code, KDoc, and comments are written in English.** Chinese is fine in documentation and in
  `build.gradle.kts`. Keeping one language in the source keeps it readable for outside contributors.
- Long `@Description` strings and `executeWrite` overrides may exceed the usual width;
  `max_line_length` is intentionally off for that reason. The `class-signature` and
  `function-signature` rules are disabled for the same reason: the author decides whether a signature
  is written on one line or several.

KDoc is expected on public API and on any non-obvious decision. When a design choice would otherwise
look like a mistake — why an absent key is not NULL, why a batch does not commit, why a cleanup
failure is suppressed — write the reason down next to it.

### Formatting in IntelliJ IDEA

`.editorconfig` stays the source of truth, and IDEA reads it natively. The shared project code style
in `.idea/codeStyles/` mirrors it for the IDE formatter, and `codeStyleConfig.xml` switches IDEA from
its own defaults to the project scheme, so *Reformat Code* and `./gradlew ktlintCheck` agree.

Only the settings every contributor needs are tracked in `.idea/`: `codeStyles/`,
`inspectionProfiles/`, `dictionaries/`, `kotlinc.xml`, `ktlint-plugin.xml`, `vcs.xml`, and
`.idea/.gitignore`. Everything else stays ignored, because it holds per-user state
(`workspace.xml`, `shelf/`) or the local JDK name (`misc.xml`) and the regenerated Gradle and
compiler configuration.

After a fresh clone, run **File → Sync Project with Gradle Files** once. If IDEA still formats
differently, check **Settings → Editor → Code Style → Kotlin** and confirm the scheme selector at
the top reads **Project**.

---

## 8. Testing

There are three layers, run by three separate Gradle tasks.

**Unit tests** (`core`, `generic-jdbc-implementation`) run on every build, need no network and no
Docker, and cover contracts rather than line coverage: SQL rendering, parameter binding, cursor
resource ownership, snapshot semantics, identifier validation, and the global `Database` lifecycle.
JDBC interfaces are mocked with MockK.

```bash
./gradlew test
```

**Skript tests** (`core`) exercise the parse phase of a section against Skript's real config parser,
which the unit tests deliberately do not have on their classpath:

```bash
./gradlew :core:integrationTest
```

They cover `values` and `where` blocks: header recognition, mode and negation, malformed entries,
mixing rows with single values, and the literal-`null` contract that keeps SQL NULL distinguishable
from an omitted column. The reachable surface stops at expression evaluation, because parsing a real
value expression needs Skript's syntax registry, which only exists inside a running Skript.

**JDBC tests** (`generic-jdbc-implementation`) run the real implementation against a real MySQL
server, and round-trip every supported type through its own converter. The converter half needs no
database and runs anywhere; the database half is opt-in, because it needs a container runtime and
takes longer:

```bash
./gradlew :generic-jdbc-implementation:integrationTest
```

One `mysql:8.4` container is started per test JVM. To reuse a server you already have, point the
tests at it instead. That database must be dedicated to testing, because the tests drop and recreate
the tables they use:

```bash
./gradlew :generic-jdbc-implementation:integrationTest \
  -Pxiaojie.test.mysql.url="jdbc:mysql://localhost:3306/xiaojie_orm_test"
```

The same settings are read from `XIAOJIE_TEST_MYSQL_URL`, `XIAOJIE_TEST_MYSQL_USERNAME`,
`XIAOJIE_TEST_MYSQL_PASSWORD`, `XIAOJIE_TEST_MYSQL_DRIVER`, and `XIAOJIE_TEST_MYSQL_IMAGE`. When
neither Docker nor an external server is available, the MySQL tests abort with a reason instead of
passing silently.

The MySQL test classpath is deliberately the plugin runtime without a server: it includes Paper,
Skript, and the NBT API, because `JdbcDataTypes` resolves those classes when it initializes.
`JdbcRuntimeClasspathIntegrationTest` fails loudly if that ever stops being true. MockBukkit is
started for the converter and MySQL tests, but only as a Bukkit server for the values that need one;
`NBT_COMPOUND` has no round-trip test, because NBT-API cannot build a compound without a real server.

Write integration test methods as `= runBlocking<Unit> { ... }`. JUnit only discovers `@Test` methods
that return `void`, and helpers such as `assertFailsWith` and `assertNotNull` return a value, so an
expression-bodied test would otherwise be compiled, never run, and never reported.

### Continuous integration

`.github/workflows/ci.yml` runs three jobs:

- `test` runs the unit and Skript tests plus `ktlintCheck`, and needs nothing else.
- `mysql-installed` runs the JDBC integration tests against the MySQL that the runner image already
  installs, which it starts with `systemctl`. That costs no image pull at all, and it puts the
  version the image ships (8.0 today) next to the 8.4 the other job pins, so the suite covers two
  servers for one pull. The connection details are passed as `-P` properties rather than environment
  variables, because a Gradle property is delivered with every invocation even when the daemon was
  started earlier.
- `mysql-testcontainers` runs the same suite with no server configured, which is the path a developer
  uses locally, so the Docker detection and the pinned `mysql:8.4` image get exercised as well. It
  runs only on the default branch and on demand, because a container image is the one thing the cache
  cannot help with.

A database-backed test that only runs when someone remembers to start a container is a test nobody
trusts. Any type, query, or converter that can only be checked against a server belongs in a job that
has one.

Every MySQL test fails with a plain connection error when the configured server is unreachable, which
is how the suite can be checked for execution-order problems without a database:

```bash
./gradlew :generic-jdbc-implementation:integrationTest \
  -Pxiaojie.test.mysql.url="jdbc:mysql://127.0.0.1:1/xiaojie_orm_test"
```

Any failure that is not a connection error is a real defect. This is how a latent order dependency in
the lifecycle tests was found.

#### What CI caches

`gradle/actions/setup-gradle` restores the Gradle User Home between runs: the Gradle wrapper
distribution, every resolved dependency, and the API jars Gradle generates on first use. That is what
stops a run from repeating the installation. Do not add `cache: gradle` to `setup-java` or an
`actions/cache` entry for the Gradle User Home — the action's documentation warns that both interfere
with it.

The cache provider is `basic`, the MIT implementation on top of `actions/cache`. The default
`enhanced` provider is a proprietary component, free for public repositories and in preview for
private ones; switching is one line if smaller, deduplicated entries are worth it. Caches are written
only from the default branch, and every run restores from it. Each job's summary reports what was
restored and saved.

Two things are deliberately not cached:

- **The Gradle build cache.** Enabling `org.gradle.caching` would also make `Test` tasks cacheable,
  and a test answered from a cache is not a test that ran. Compiling this project is cheap next to
  the downloads.
- **The MySQL container image.** GitHub cannot cache Docker images, so `mysql-testcontainers` pulls
  `mysql:8.4` on every run it takes part in. This is why the other database job starts the MySQL the
  runner image already has instead of using a service container: that job pulls nothing, and it is
  also restricted to the default branch so that pull requests pay one pull rather than two.
  `mysql-testcontainers` is additionally marked `cache-read-only`, because it resolves the same
  dependencies as `mysql-installed`, so letting it write would only duplicate a large entry and risk
  evicting the shared ones.

Note that a `docker save` tarball cached with `actions/cache` is not a way out: the tarball is
uncompressed and therefore larger than the pull it replaces, it competes for the same cache budget as
the Gradle entries, and it needs shell glue in every job.

`ubuntu-latest` already carries JDK 25, so `actions/setup-java` normally installs nothing. If a future
image drops it, the action downloads the JDK, which is the one download the Gradle cache cannot help
with.

### Why there is no mocked server

MockBukkit cannot host Skript. It loads a plugin by generating a subclass of the main class, so a
`final` main class cannot be loaded at all, and Skript's main class is final. This addon cannot be
loaded alone either: `plugin.yml` declares `depend: [Skript]` and `onEnable` calls
`Skript.registerAddon`. There is therefore no mocked-server test at all: a full boot, including
element registration and script execution, needs a real server, which is what `run-paper` is for and
what is still missing.

MockBukkit is still used by the Skript tests, but only as a Bukkit server: Skript logs through
`Bukkit.getConsoleSender()`, and the config parser reports through it, so without a server the parser
NPEs instead of returning nodes.

---

## 9. Adding a database implementation

1. Create a module and add it to `settings.gradle.kts`.
2. Implement `Database`: `doConnect`, `doDisconnect`, `doRegisterTable`, and `dataTypes`.
3. Implement `Queries` to construct one query object per operation.
4. Implement `DataType` for each supported logical type, overriding `converter` where the backend
   needs a different representation.
5. Implement `DatabaseFactory` and register it in `DatabaseRegistry` from an `init` block.
6. Make the plugin load the factory by adding its class name to the candidate list in
   `XiaojieOrm.onEnable()`.

An implementation may report an operation as unsupported, but it must not silently degrade it into a
different operation.

---

## 10. What we deliberately avoid

- **Speculative abstraction.** Do not add an interface for one implementation, or a configuration
  object for one setting. Two similar lines are better than a premature framework.
- **Per-operation configuration duplication.** Cross-cutting concerns belong in one place. Query
  timeout, for example, is not something every query should configure for itself.
- **Unused code kept "for later".** Delete it. Git remembers.
- **Linters as a design authority.** Formatting is automated; design is reviewed by people. A finding
  is a prompt to think, not an instruction to restructure.
- **Changing behaviour that was never declared.** If a new guarantee is needed — atomicity for a
  batch, ordering for a query — state it in the API before implementing it.

---

## 11. Commit messages

Write the subject in the imperative mood and explain the **why** in the body when the change is not
self-evident. Keep unrelated changes in separate commits.
