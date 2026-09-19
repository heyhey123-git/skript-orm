# Contributing to Skript ORM

[中文版](CONTRIBUTION.zh-CN.md)

Skript ORM is a Skript addon that exposes database operations as Skript elements. It is in early
development: both the public API and the internal structure may still change. Unit tests and opt-in
integration suites for MySQL, PostgreSQL and MongoDB cover the current behaviour (see §8).

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

The root project builds the shaded plugin jar. It bundles every implementation module — the generic JDBC
one, the PostgreSQL one, and the MongoDB one — and **no database driver**: the `libraries` entry in
`plugin.yml` is generated from the modules the jar bundles, so a module that needs a driver names it
there, and Paper downloads that driver on the first start into the server's `libraries/` directory. In
the default build the list is `org.postgresql:postgresql:42.7.11` and
`org.mongodb:mongodb-driver-sync:5.6.1`, while a combination of modules that need no driver writes
`libraries: []`. That keeps the jar the size of the plugin and the driver the library its authors
published, at the cost of a server that must be able to reach the server's mirror of Maven Central once.
`-PbundleModules=a,b` builds a jar for another combination, which is how a module that is not released
yet is tried:

```bash
./gradlew build                                  # shadow jar into build/dist
./gradlew build -PbundleModules=generic-jdbc-implementation,mongodb-implementation
```

### The wiki mirror

`docs/` is the source, and the repository wiki carries the Chinese half of it as a reading copy. A push
that changes a page runs `.github/workflows/wiki.yml`, which renders the pages with
`scripts/publish-wiki.ps1` and commits them to the wiki repository. Page names, and every link between
pages, come from the table at the top of that script, so a new page is one entry there and one line in
the sidebar. The wiki is not edited by hand: the next run replaces whatever is there.

That workflow needs a wiki that already exists. On a fresh clone, open the wiki once and create a page,
or a checkout of `<repository>.wiki` has nothing to check out.

### The boundary rule

`core` must not depend on any driver, connection pool, or query language. It describes *what* a
database can do. An implementation owns *how* it does it: its driver, its client, its connection
lifecycle, and its resource cleanup.

If you need JDBC or MongoDB types in `core`, the abstraction is wrong. Widen the
abstraction instead of leaking the dependency.

### Versions

Every version lives in `gradle/libs.versions.toml`. No module repeats a version literal, so upgrading
Paper or Skript is a one-line change in one file, and the artifact the tests run against cannot drift
from the one the plugin compiles against.

Two entries are not free to move on their own:

- `api-version` in `core/src/main/resources/plugin.yml` must name the same release line as `paper`.
  Paper refuses to load a plugin whose `api-version` is newer than the server, so the file must never
  claim more than the compiled API; claiming *less* is worse, because Paper then loads the plugin on
  a server that lacks the methods it calls and the failure surfaces much later as a
  `NoSuchMethodError`. Bump both together.
- The `mockbukkit` artifact name carries the Paper line (`mockbukkit-v26.2`), so it moves with `paper`
  too.

Only stable Paper builds belong in the catalog. A newer line that is still published as ALPHA is not
an upgrade.

Skript's own floor is checked at runtime in `SkriptOrm.onEnable`, not declared in `plugin.yml`: see
the KDoc on `MINIMUM_SKRIPT_VERSION` for why a version-qualified `depend` entry cannot work.

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

A statement can hand that number to a script with the optional `store affected rows` clause, which
`skript/utils/AffectedRows` owns: the pattern fragment, the variable it may name, and the rule that says
when the variable is written. The clause is cleared when the statement starts and written only for an
exact count, so a zero stays distinguishable from "no answer"; `WriteResult.countExact` is what a new
implementation has to get right for that to hold.

For JDBC batches, `SUCCESS_NO_INFO` means the driver cannot report a count for that command. It is
**not** one row: leave `affectedCount` alone and set `countExact = false`. `EXECUTE_FAILED` is an
error. Other negative counts are an invalid driver response and must raise.

---

## 6. Skript elements

Element classes are named `Sec*` (sections), `Eff*` (effects), and `Expr*` (expressions), and live
under `core/.../skript/elements/`.

Each element keeps its own patterns and exposes a `register(addon)` function, which
`skript/ElementRegistrations.kt` calls for all of them while the plugin enables. That list is the only
place registration happens, because Skript reads its syntax registry while it loads scripts.
`Skript.registerSection`, `registerEffect` and `registerExpression` are deprecated in favour of
registering a `SyntaxInfo` with the addon's `SyntaxRegistry`, which is what `skript/utils/SkriptSyntax`
wraps. Adding an element to that list is part of adding the element: the server test drives every
element it expects, so one that is never registered fails there instead of quietly not existing.

A section is only recognised when its line ends with a colon, so an element whose body is optional is
written with a trailing colon and nothing under it. Skript notes that with `Empty configuration
section!` in the log and runs the section anyway. Those notes are expected, not a defect.

- `SecSelectBase` and `SecWriteBase` hold the shared parsing and dispatch logic.
  `SecCreateConnection` and `SecRegisterTable` are standalone because they do not fit either shape.
- Which connection a statement uses is decided by `ConnectionScope`: the innermost `in connection`
  scope, then a `use connection` in the same event, then the default connection. Scopes are kept per
  event, the shape `last database error` already uses, so a function call inherits them and a
  continuation after a `wait` still sees them.
- `SecInConnection` holds code rather than a payload, so it loads its body with `loadCode`. That is
  also what puts the section into the parser's current sections, which is how `exit` and `stop`
  reaching it are noticed. A `TriggerItem` of its own closes the body, because Skript has no
  end-of-body callback and whether that node is wired in decides whether the scope leaks.
- Every statement that touches the database waits: `DatabaseWork.run` parks the trigger, preserves the
  event continuation and puts failures in `last database error`. `and wait` is still accepted by every
  pattern and read by nobody, because writes once needed it to ask for that.
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

There are four layers, run by four separate Gradle tasks.

**Unit tests** (`core`, `generic-jdbc-implementation`, `mongodb-implementation`) run on every build,
need no network and no Docker, and cover contracts rather than line coverage: SQL rendering, parameter
binding, cursor resource ownership, snapshot semantics, identifier validation, and the global
`Database` lifecycle. JDBC interfaces are mocked with MockK, and the MongoDB module's unit tests need
no server either.

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

`MinimumSkriptVersionTest` covers no code of ours on purpose. It pins Skript's own
`ch.njol.skript.util.Version` ordering, which the version floor in `SkriptOrm.onEnable` depends on:
that floor is a single comparison, and both ways it can be wrong are invisible from this repository.

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
  -Pskriptorm.test.mysql.url="jdbc:mysql://localhost:3306/skriptorm_test"
```

The same settings are read from `SKRIPTORM_TEST_MYSQL_URL`, `SKRIPTORM_TEST_MYSQL_USERNAME`,
`SKRIPTORM_TEST_MYSQL_PASSWORD`, `SKRIPTORM_TEST_MYSQL_DRIVER`, and `SKRIPTORM_TEST_MYSQL_IMAGE`. When
neither Docker nor an external server is available, the MySQL tests abort with a reason instead of
passing silently.

**PostgreSQL tests** (`postgresql-implementation`) run the same way against a real PostgreSQL server,
and they are the only layer that can say whether the dialect's SQL is SQL PostgreSQL accepts:

```bash
./gradlew :postgresql-implementation:integrationTest
```

One `postgres:17-alpine` container is started per test JVM; a server you already have is used instead
when `-Pskriptorm.test.postgres.url` is set, with the same `SKRIPTORM_TEST_POSTGRES_*` environment
equivalents as the MySQL suite. The tests drop and recreate their tables, so that database must be
dedicated to testing, and it has to accept a password over TCP. The SQL itself needs no server and is
pinned by `PgJdbcDialectTest`, which is part of the ordinary `test` task.

**MongoDB tests** (`mongodb-implementation`) split the same way: the converter half is plain unit tests
that run anywhere, and the opt-in half needs a real MongoDB:

```bash
./gradlew :mongodb-implementation:integrationTest
```

With no server configured, the suite starts a standalone `mongo:8` container through Testcontainers.
That is deliberately not the replica set `MongoDBContainer` sets up, because nothing here uses sessions
or transactions, and a standalone container is the shape a script's own server is likely to have. To
reuse a server you already have, point the tests at it instead; that database must be dedicated to
testing, because the tests drop and recreate the collections they use:

```bash
./gradlew :mongodb-implementation:integrationTest \
  -Pskriptorm.test.mongo.url="mongodb://localhost:27017/skriptorm_test"
```

`-Pskriptorm.test.mongo.username`, `-Pskriptorm.test.mongo.password`,
`-Pskriptorm.test.mongo.database` and `-Pskriptorm.test.mongo.image` are optional and override the
defaults. When neither Docker nor an external server is available, the MongoDB tests abort with a reason
instead of passing silently.

The MySQL test classpath is deliberately the plugin runtime without a server: it includes Paper and
Skript, because those are the classes the JDBC type registry resolves when it initializes.
`JdbcRuntimeClasspathIntegrationTest` fails loudly if that ever stops being true. NBT is the exception
that runtime exists to prove: the plugin compiles against no NBT implementation and SkBee is not here,
so the registry has to initialize without one. `NBT_COMPOUND` has no round-trip test for that reason;
the Skript server test, which installs SkBee, is what covers an NBT value.

NBT is the one part of the plugin that depends on another plugin at runtime. SkBee is what gives
scripts a way to write a compound, and it bundles its NBT library under its own package, so the plugin
neither compiles against an NBT implementation nor names one: `NbtSupport` looks SkBee's classes up by
name, links them with `MethodHandle`s once, and moves values in and out through SNBT. That is why
`softdepend` lists SkBee, why registering a table with an `nbtcompound` column is refused with a
message when SkBee is missing, and why the server test installs SkBee rather than the standalone NBT
API, which the plugin does not support.

Write integration test methods as `= runBlocking<Unit> { ... }`. JUnit only discovers `@Test` methods
that return `void`, and helpers such as `assertFailsWith` and `assertNotNull` return a value, so an
expression-bodied test would otherwise be compiled, never run, and never reported.

**Skript server tests** (the root project) boot a real Paper server carrying Skript and the shaded
plugin, then check what the addon did there. This is the only layer that can cover element
registration and the parse phase of a real script, because both need a running Skript:

```bash
./gradlew serverTest
```

Like the MySQL half it is opt-in: it downloads a Paper jar into the Gradle user home, where later runs
find it again, and starts a Minecraft server. The server it runs is the `paper` entry of the version
catalog, so it is the same API line and build the plugin compiles against rather than a merely
compatible one.

`server-test/skript/` drives the addon, one element per file under `elements/`, so a failure names
the element through the file it happened in. Nothing is connected to a database on purpose: every
element is expected to stop at the database lookup and report `No database connected.` through
`last database error`, which runs all of them for real without a database server. Each element
logs a `SKRIPTORM_SELFTEST` line, and the `serverTest` task checks those lines against the list in
`build.gradle.kts`. A statement Skript cannot parse is reported and then skipped, so a pattern that
stops registering shows up as a missing line instead of passing quietly.

`docs/examples/` is copied into the same server, so the Skript on the documentation pages is parsed
as well: a page showing a statement the addon refuses fails a build instead of reaching a reader. Those
files are written as commands rather than as triggers, so nothing in them acts on the test server.

Two properties of a Minecraft server shape the scripts:

- A periodic trigger does the work, because Skript's `on script load` does not fire while the server
  is still starting (SkriptLang/Skript#5754), so nothing driven by script loading can be used here.
- Each element records itself as it runs, and `99-finish.sk` stops the server once they have all
  reported. Its second trigger stops it anyway if that never happens, which is what turns a hung run
  into a failure; it uses no addon syntax, so it also runs when the addon is the thing that broke.
  That is also why `prepareServerTest` deletes Skript's data directory: the guards the elements keep
  live there, and one left behind would make the next run skip an element.

`prepareServerTest` writes the run directory, `build/server-test`: `server.properties`, the scripts,
and `eula.txt`. Writing that last file accepts the Minecraft EULA for this disposable server, which
is why the task and not a developer is what does it.

The same element scripts also run against a database. Passing the properties the integration suite of one
implementation uses — `-Pskriptorm.test.mysql.url`, `username`, `password` for MySQL, or the
`skriptorm.test.postgres.*` equivalents — adds `server-test/database/`: a setup script that
`prepareServerTest` writes with the implementation name and the credentials substituted, which connects
and registers a table of its own, and a round trip that writes a row, reads it back and compares it. The
element scripts then run against the live connection, which is the only way to cover the value conversions
inside `values` and `where`: those happen after the database lookup, so a run without a database never
reaches them.

Which implementation those keys describe is `skriptorm.test.server.type`, the name the scripts write after
`database`. It defaults to `MySQL`; `PostgreSQL` and `MongoDB` run the same scripts against those two
implementations, which is what their own CI jobs do. The name also decides which keys the credentials come
from, so one `-P` set configures both the integration tests and this layer, and a name belonging to
neither fails the build instead of quietly running without a database.

Three consequences shape those scripts. Every element reports whatever its step produced once a
connection exists, so in this mode their expected messages are empty and only their presence is
checked; the round trip is what carries the assertions. The setup and round trip scripts keep a
start guard *as well as* the marker, because the two are not the same thing: their steps wait on the
database, so without the guard a second firing would begin while the first is still working and
register the same table twice. And an implementation without transactions gets the same transaction
script: the section refuses to open one, skips its body and carries on, so the script reaches its end and
reports the refusal instead of a commit — which is what `MongoDB` expects, and the only place two modes
expect different messages for the same script.

### Continuous integration

`.github/workflows/ci.yml` runs seven jobs:

- `test` runs the unit tests — including `:mongodb-implementation:test` — and the Skript tests plus
  `ktlintCheck`, and needs nothing else.
- `mysql-installed` runs the JDBC integration tests against the MySQL that the runner image already
  installs, which it starts with `systemctl`. That costs no image pull at all, and it puts the
  version the image ships (8.0 today) next to the 8.4 the other job pins, so the suite covers two
  servers for one pull. The connection details are passed as `-P` properties rather than environment
  variables, because a Gradle property is delivered with every invocation even when the daemon was
  started earlier.
- `postgres-installed` does the same for PostgreSQL, which the runner image also installs, and then runs
  the Skript server test against that same server: the `postgresql-implementation` tests are the only
  place the PostgreSQL dialect meets a server, and the server test is the only place the addon is driven
  end to end against an implementation other than MySQL.
- `mysql-testcontainers` runs the same suite with no server configured, which is the path a developer
  uses locally, so the Docker detection and the pinned `mysql:8.4` image get exercised as well. It
  runs only on the default branch and on demand, because a container image is the one thing the cache
  cannot help with.
- `mongo-testcontainers` runs the MongoDB integration suite with no server configured, in the same way
  and for the same reason, with the pinned `mongo:8` image. Like `mysql-testcontainers`, it runs only
  on the default branch and on manual runs.
- `mongo-server` runs the Skript server test against MongoDB, which is a service container of the job
  rather than a Testcontainers one: the process under test is the Paper server the build starts, and it
  needs an address that is already answering before the run begins, so the job waits for the server with
  `mongosh` the way the PostgreSQL job waits with `pg_isready`. It is the only job whose transaction lines
  expect the refusal a database without transactions reports. Runs on the default branch and on manual
  runs, like the other container jobs.
- `skript-server` boots the Paper server described above and checks what the plugin did there. It
  needs neither Docker nor a database, so it runs on every change. The server jar it downloads lands
  in the Gradle user home, which the Gradle state cache already covers, so the job needs no cache of
  its own.

Every job installs the toolchain through `.github/actions/prepare-build`, and the Skript server test is
annotated through `.github/actions/annotate-server-test`. Both live under `.github/actions/` for the same
reason: seven jobs repeating the JDK, the cache and the wrapper make the jobs hard to compare and the
version easy to bump in six places out of seven. The checkout stays in each job rather than in the
action, because a local action is read from the workspace and so cannot be the step that creates it.

Two more workflows do the packaging. Neither decides whether the code is correct; `ci.yml` does that.

`build.yml` runs on every branch push and uploads the shaded jar as an artifact of that run, which is
what a tester downloads to try the current state of the code. The artifact belongs to the run: it
expires with it and promises nothing. The version is read from `gradle.properties` only to name the
artifact, so that name and the jar's own name cannot disagree.

`release.yml` is manual, takes no input, and publishes a GitHub release. It reads the version from
`gradle.properties` and refuses anything that is not a plain `x.y.z`, because a release records what
was committed and a snapshot must not be published as one. It then runs the same test layers `ci.yml`
runs — including the real server test — on the commit it is about to tag, checks that the built jar
carries the version being released, writes a `sha256` beside it, and creates the tag and the release
at that exact commit. It is limited to the default branch; releasing from a branch is removing that
one condition.

What the release says about itself is the section `CHANGELOG.md` holds for that version, and a version
without one is refused before the build rather than published undescribed. `./gradlew releaseNotes` is
what takes that section out of the file — the workflow publishes what it writes, and a person correcting a
release that is already published runs the same task:

```bash
./gradlew releaseNotes
gh release edit v1.2.0 --notes-file build/release-notes.md
```

A release is therefore four repository changes first: the version in `gradle.properties`, the changelog
section for it, the push, then the dispatch. That is the point — `1.0-SNAPSHOT` in the repository means
"not a release", and the workflow reads it that way.

`skripthub.yml` publishes the syntax documentation to SkriptHub after a release. It waits for the
completion of `release.yml` rather than for the release event, because a release created with the
repository's own token starts no workflow run of its own; `workflow_run` closes that gap and names the
commit the release was built and tagged from. It generates the JSON with `./gradlew gendocs` on that
commit, keeps it as an artifact for the dashboard's JSON Syntax Import, and then writes what changed
through `scripts/publish-skripthub.mjs`, which diffs the generated file against what SkriptHub holds and
touches only the elements whose pattern, description or since version moved. Examples and supporting
plugins are not part of that write — they belong to the import — so a difference in an example is reported
in the run summary instead of being left for a reader to find. The token is the environment secret
`SKRIPTHUB_TOKEN` of an environment named `skripthub`, because it can write to a public page: the job names
that environment, so no other workflow can read it, and the environment's deployment branch rule is the
default branch, which is the only branch the job is meant to run from. A run without the secret stops at
the first step and says what to add.

#### When CI runs

`push` and `pull_request` ignore changes that cannot affect the build: Markdown, `.gitignore`, and
`.idea/`. Everything else triggers CI, including `.editorconfig`, which looks inert but is what ktlint
reads. `workflow_dispatch` bypasses the filters, so a run can always be forced. `build.yml` uses the
same two filters, so a change that cannot affect the build neither tests nor packages. Both ignore
tags: `release.yml` tags a commit it has already built and tested.

The list is an ignore list on purpose. An allow list of build inputs would silently stop CI from
running the first time a new input type is added, and a check that did not run looks exactly like a
check that passed.

One consequence to keep in mind: when a pull request touches only ignored files, the workflow does
not run at all, so no status is reported. If these checks are ever made *required* in branch
protection, such a pull request would wait for a check that never arrives. The fix at that point is
to run the workflow unconditionally and gate the expensive jobs from inside it, rather than filtering
the trigger.

A database-backed test that only runs when someone remembers to start a container is a test nobody
trusts. Any type, query, or converter that can only be checked against a server belongs in a job that
has one.

Every MySQL test fails with a plain connection error when the configured server is unreachable, which
is how the suite can be checked for execution-order problems without a database:

```bash
./gradlew :generic-jdbc-implementation:integrationTest \
  -Pskriptorm.test.mysql.url="jdbc:mysql://127.0.0.1:1/skriptorm_test"
```

Any failure that is not a connection error is a real defect. This is how a latent order dependency in
the lifecycle tests was found.

Failing tests are also written out as check annotations by `.github/actions/annotate-test-failures`.
A job log needs admin rights to read through the API while annotations are public, and the HTML report
is an artifact that requires authentication to download, so the annotation is what makes a failure
diagnosable without either. Each annotation carries the test name and the assertion message, which is
enough to identify the column or value that disagreed.

#### What CI caches

`gradle/actions/setup-gradle` restores the Gradle User Home between runs: the Gradle wrapper
distribution, every resolved dependency, and the API jars Gradle generates on first use. That is what
stops a run from repeating the installation. Do not add `cache: gradle` to `setup-java` or an
`actions/cache` entry for the Gradle User Home — the action's documentation warns that both interfere
with it.

The cache provider is `enhanced`, the action's default. It builds a cache key per job, so the jobs do
not compete for one entry. The first CI run used `basic`, the MIT provider, and showed why that matters:
`basic` keys the cache only on the build files, so every job computed the same key and every job but the
first failed to save it, reporting "Unable to reserve cache ... another job may be creating this cache".
The one entry that did get saved held only the dependencies of whichever job finished first, so the
other jobs re-downloaded theirs on every run. `enhanced` is a proprietary component, free for public
repositories and in preview for private ones; `cache-provider: basic` is the one-line fallback if that
trade-off ever becomes unacceptable.

Caches are written only from the default branch, and every run restores from it. Each job's summary
reports what was restored and saved.

Two things are deliberately not cached:

- **The Gradle build cache.** Enabling `org.gradle.caching` would also make `Test` tasks cacheable,
  and a test answered from a cache is not a test that ran. Compiling this project is cheap next to
  the downloads.
- **The database container images.** GitHub cannot cache Docker images, so `mysql-testcontainers`
  pulls `mysql:8.4` on every run it takes part in, and `mongo-testcontainers` pulls `mongo:8` the same
  way. This is why the other database job starts the MySQL the runner image already has instead of
  using a service container: that job pulls nothing, and it is also restricted to the default branch
  so that pull requests pay one pull rather than two. `mysql-testcontainers` is additionally marked
  `cache-read-only`, because it resolves the same dependencies as `mysql-installed`, so letting it
  write would only duplicate a large entry and risk evicting the shared ones.

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
   `SkriptOrm.onEnable()`.

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
