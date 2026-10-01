# Changelog

What each release changed, written for the people who run the plugin: a server admin deciding whether to
upgrade, and a script author looking for what is new. This file is the source of a release's notes — the
release workflow publishes the section of the version in `gradle.properties`, and refuses a version whose
section is missing, so a release cannot go out undescribed.

The format is [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow
[Semantic Versioning](https://semver.org/spec/v2.0.0.html): a new implementation is a feature, not a fix.
Write into `Unreleased` as work lands; when the version is raised, that heading becomes the version and its
date, and a fresh `Unreleased` heading is added above it. Each released section ends with the compare link
the previous release used, because the notes are the release body and nothing else fills that in.

## [Unreleased]

## [1.4.0] - 2026-10-01

A release about where a batch insert goes, what it reports, and how much one statement may move. MariaDB
becomes a type of its own, because its connector reaches the server's own bulk execute with no configuration,
where Connector/J sends one row at a time — the difference a large `insert many` showed as a MariaDB server
being many times slower than PostgreSQL. One statement now moves at most 5000 rows, and the two directions
treat that ceiling differently: a read past it is refused, and a write past it keeps what fits. Six fixes
come with it: a table declared twice is no longer an error, a number written into a text column keeps its
digits, a column that holds nothing reads back as unset, a table that carries a column the declaration does
not name is registered instead of being refused, a column with more room than the declaration asks for is
registered, and a `boolean` column is recognised under whichever name
its driver reports for it — for which the type names a comparison accepts are now each dialect's own rather
than one list shared by every server.

### Added

- **MariaDB is a type of its own, `"MariaDB"`, because its driver sends a batch insert differently.**
  MariaDB Connector/J is downloaded on the first start, like the PostgreSQL and MongoDB drivers, and the
  type shares the MySQL dialect, since MariaDB accepts the SQL it generates. Write the url as
  `jdbc:mariadb://…`: each connector claims one scheme, and a mismatch is refused when the connection is
  created instead of being handed to the wrong driver. The reason for a separate type is a batch insert:
  Connector/J sends one row at a time unless it is configured to rewrite the batch, and a rewritten batch
  reports no per-row count, while the MariaDB connector reaches the server's own bulk execute with no
  configuration and still counts every row. That is the difference a large `insert many` shows, and the
  reason a MariaDB server reached through `"MySQL"` could be many times slower than PostgreSQL.

### Changed

- **A read that returns more than 5000 rows is refused, and stores nothing.** `select many` and `select page`
  used to store whatever the table held. A hundred thousand rows read into a list variable is seconds of the
  server stopped, and the only sign of it was a watchdog line in the console. The refusal names the ceiling and
  leaves the variable cleared, because the alternative — storing a result that was cut in half — answers every
  later question about rows the script never saw. Narrow the result with a `where` block, or walk it with
  `select page`. A page larger than 5000 rows is refused before anything is sent, for the same reason.
- **`insert many` given more than 5000 rows writes the first 5000, drops the rest and warns.** Refusing would
  throw away rows the script already assembled, so the statement does what it can and says so in the console.
  The affected-row count is the count that was written, which is the other place the drop shows up. Split the
  batch over several statements to write all of it.

### Fixed

- **A table registered twice is no longer an error.** A declaration that matches the table the script
  already registered is ignored, so a reload or a shared script that declares the same table again keeps
  working instead of reporting `Table 'users' is already registered.` and skipping the declaration. A
  declaration that differs is registered instead, which compares it with the table in the database, so a
  change the table does not hold is reported rather than silently ignored. The first registration is the
  one that counts.
- **A number written into a text column is stored as the number it is.** `42` now reaches a `string` column
  as `42`, not `42.0`, and a fractional value keeps its fraction. The digits a script wrote are the digits
  the column holds.
- **A number column that holds nothing reads back as unset instead of failing.** A `NULL` in a column read as
  a number, a date or a boolean is now reported as no value, where it used to reach the converter and fail
  the statement.
- **A table is compared for what the declaration needs, not for what the declaration does not mention.** The
  check that runs after `CREATE TABLE IF NOT EXISTS` used to report a difference in both directions: a
  declared column the table is missing, and a column the table has and the declaration never names. Only the
  first one can fail a statement, so a table shared with another tool, or one an older declaration of the same
  script created with columns this one no longer names, now registers. The key is still compared, and in the
  direction that matters: a table keyed on a column the declaration does not mark as a key is refused, because
  a statement addressing a row by the declared key could then match several rows. A declaration with no key
  asks nothing about identity, and is no longer refused for a table that has one. A column the table carries
  that is `not null` with no default is the one case where a write still fails, and the server names the
  column when it does.
- **A column with more room than the declaration asks for is registered.** The size a comparison asked for was
  the size it wanted back exactly, so a `TEXT` column — 65535 to MySQL and MariaDB — was refused for a `string`
  declared at its default of 255, and a wider `VARBINARY` was refused for a `location`. A declaration asks for a
  storage its values fit, and a wider column is exactly that: every statement the declaration allows runs
  against it. The one storage where more room is not room is the fixed-width `BINARY(16)` a `uuid` is declared
  as, because a server hands a wider one back padded to its full width, so that one still has to match.
- **A `boolean` column is recognised under whichever name its driver reports for it.** MySQL has no boolean
  type of its own — `BOOLEAN` is `TINYINT(1)` — and both MySQL's and MariaDB's driver carry a `tinyInt1isBit`
  property, true by default, that decides whether such a column is reported as `BIT`, as `BOOLEAN` or as
  `TINYINT`. A script that asks for the last one in its connection URL, a common way to get `0` and `1` out of
  these columns, made the schema check read the column as one declared `tinyint` and refuse the registration:
  the metadata gives `TINYINT(3)` then, the same name and the same size that column has. All three names are
  the same one-byte column on these servers and none of them can fail a statement, so a declaration of
  `boolean` is accepted under all three.
- **A type name is read through the dialect whose server it was measured on.** The comparison keeps the names
  the standard and JDBC use — `INT` for an `INTEGER`, `TEXT` and `CHARACTER VARYING` for a `VARCHAR`,
  `BINARY LARGE OBJECT` — and every other spelling now belongs to the dialect that has it: PostgreSQL's
  `int2`, `int4`, `int8`, `float4`, `float8`, `bool` and `bytea`, the MySQL family's `TINYINT`, `BIT`,
  `TINYTEXT`, `MEDIUMTEXT`, `LONGTEXT` and the three `BLOB` sizes, and the legacy spellings (`INT8`,
  `SERIAL`, `BOOL`) an untyped `"JDBC"` server such as SQLite may carry because a table was made elsewhere.
  One list for every server read a name on a server it was never measured on: PostgreSQL's `bit(1)` is a bit
  string, not the one-byte boolean the MySQL family stores under that name, so a declared `boolean` no longer
  takes one. The shared list also held `LONGVARCHAR` as a `BLOB`, which it is not: JDBC defines it as long
  character data whose binary counterpart is `LONGVARBINARY`, H2 lists it among the names of its `VARCHAR`,
  and MySQL turns a declared `LONG VARCHAR` into a `MEDIUMTEXT`. It is a `VARCHAR` now, so a text column can
  no longer be taken for the storage a serialised value is written to, and `BINARY VARYING` — the standard
  spelling of the `VARBINARY` this implementation writes, beside the `CHARACTER VARYING` already accepted for
  a `VARCHAR` — is accepted too. A table this implementation created itself is written with the names on the
  declaration, so this only reaches a table made by something else, and there it is read with the names its
  own server has.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.3.0...v1.4.0

## [1.3.0] - 2026-09-29

A release about what a script can do when the declaration is not enough, and about failing where the mistake
is. Raw statements let a script write SQL or a command document itself, for the migrations, indexes and
vendor-specific statements no declaration can express — and they are unsafe by design, which the documentation
says before anything else. Two refusals join them: a connection property nothing reads is now reported instead
of dropped, and a registration that no longer matches its table is reported on the declaration rather than
later, by the server, against an insert.

### Added

- **Raw statements: SQL and command documents a script writes itself.** `execute query`, `execute update`
  and `execute command` send a statement exactly as written — the plugin does not parse it, check it against
  a registered table, or translate it between implementations — which is what makes migrations, indexes,
  vendor extensions, and filters whose shape is only known while the script runs expressible at all. Values
  are bound to `?` placeholders by a `with` clause rather than pasted, and the count has to match the number
  of placeholders before anything is sent; nothing else is checked, and the documentation says so first. A
  statement the connection cannot take is refused by name before it reaches the server: a SQL connection
  takes `execute query` and `execute update`, a document one takes `execute command`. See
  [Raw statements](docs/raw-statements.md). **This is unsafe by design and has no safety net.**

### Fixed

- **A connection property nothing reads is now refused instead of dropped.** Writing `database: "sicilia_db"`
  on a MySQL connection used to connect to a server with no default database and fail later, at the first
  statement that needed one, naming neither the property nor the connection; a typo in a property name did
  nothing at all and looked like a property that had been honoured. Each implementation now declares the
  names it reads, and `create a connection` checks the body against that set while the script is still on the
  line.
- **A registration that no longer matches the table it names is refused.** Registration is
  `CREATE TABLE IF NOT EXISTS`, so a column added to a script never reached a table that already existed and
  the first statement to mention it failed with the server's `Unknown column ...`, at the insert's line. The
  table is now read back and compared with the declaration — columns, types, sizes, `not null` and the
  primary key — and the failure lands where the declaration is.

### Changed

- **A failure reports itself the way Skript's own effects do.** When a statement fails, the console line
  now comes from Skript's runtime error channel instead of this plugin's logger, so it names the script and
  the syntax, prints the line number and the line itself, and warns the players who watch for runtime
  errors — those holding `skript.see_runtime_errors`. What a script reads through `last database error` is
  unchanged, and stays out of Skript's reach: its per-line frame limits summarise a line that keeps failing
  in the console, never in the script.
- **Startup reports which database implementations loaded.** A server owner who expected a type and does not
  see it in the list has the answer without reading the changelog, and an implementation that failed to load
  is named with the reason its initializer gave rather than passing in silence.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.2.0...v1.3.0

## [1.2.0] - 2026-09-19

A release about what a script can see. A write hands back the number of rows it affected, which is what a
conditional write needs, and every statement now waits for its work, so a failure can no longer reach the
console while `last database error` stays silent.

### Added

- **`store affected rows in {_rows}`**, on every statement that writes: `insert one`, `insert many`,
  `insert ... if absent`, `update`, `upsert` and `delete`, in the section form and the colon-free one
  alike. It keeps the number of rows the statement affected, which is how a script tells "it was already
  there" from "it was written", and how a read-modify-write becomes safe without a transaction: the count
  of a statement whose `where` repeats the value it read is `1` only while nobody else got there first. A
  count of `0` is a real answer, while a variable left unset means no statement answered — the statement
  was refused or skipped, it failed, or the backend could not count it exactly. See
  [Affected rows](docs/affected-rows.md) for the three states, what the number means on each backend, and
  a bounded retry recipe.

### Changed

- **Every statement waits.** A write used to continue immediately unless it said `and wait`: the next line
  ran while it was still in flight, so a read could see the row as it was, and a failure only the database
  could judge went to the console instead of into `last database error`. Writes now behave the way reads
  always have — the lines after a statement run once its work is done, which makes the order of a script
  the order of its statements. **Upgrade note:** a script that relied on carrying on before a write had
  finished will now wait for it, and a write that failed is now reported in `last database error` where it
  used to be logged only. `and wait` is still accepted on every statement and does nothing, so no script
  has to change; it can simply be left out from here on.

### Fixed

- **`insert entity if absent` on MySQL swallowed every error, not only an existing row.** It was
  `INSERT IGNORE`, which turns errors into warnings, so a value too long for its column was stored cut
  short and a `null` in a `not null` column became that column's default — with the script told nothing.
  The insert is now sent as it is and the duplicate key is the one error the statement reads as "the row is
  there", which is what PostgreSQL and MongoDB already did. The count is unchanged: `1` written, `0` it was
  already there, and anything else fails like any other write.
- **A connection that lost the default role with no name of its own was left open for the life of the
  server.** Nothing could resolve to it afterwards — `in connection` and `use connection` need a name, and
  it was no longer the default — so it kept its pool of ten server connections, and neither
  `disconnect from all connections` nor a plugin disable could reach it. `make ... the default` closes it
  now, the same way an unnamed connection that replaces the default already closed the one it replaced. A
  **named** connection keeps running and merely stops being the default. The statement waits for that
  close, so the line after it sees the new default, and it is refused inside a `database transaction`,
  because the connection it closes may be the one the transaction is running on.
- **A read that was refused before it ran left the previous result in its variable.** A read that failed
  at the database cleared the variable, while one refused earlier — no connection, an unknown table, a
  `where` value the column cannot hold, a page number of zero — did not, so the previous read's row was
  still sitting there and a script could not tell "the row is gone" from "the statement did not run". Every
  refusal clears it now, which is the rule the `store affected rows` variable already followed: a
  statement leaves this statement's answer, or nothing.
- **A whole number that did not fit its column was stored as a different number.** Skript converts a
  number into whatever type it is asked for, and every one of those conversions narrows: an `int` column
  turned `5000000000` into `705032704` (the low 32 bits), a `tinyint` column turned `300` into `44`, and
  the database was handed a value that fitted, so nothing anywhere reported it. A number column is now
  read as a number and narrowed by the plugin, which refuses a value the column cannot hold with a
  message naming the column and its range — in a `values` block, in a variable and in a `where` value
  alike. A fraction written into a whole-number column is still cut towards zero, and `float` still keeps
  four bytes: only the case that was silently wrong changed. See
  [Types](docs/types.md#what-a-mismatch-looks-like).
- **A failure the database itself reported did not end the transaction it happened in.** Only a statement
  refused before it was sent made a transaction rollback-only, so a constraint violation, a value the
  server refused, or a dialect refusing a statement let the body carry on, a later statement cleared the
  failure out of `last database error`, and the body ended by committing the half of it that had worked.
  Any statement failure now makes the transaction rollback-only: the statements after it do nothing, the
  body rolls back, and the cause stays readable.
- **Naming the connection a transaction is already running on took the statements after it out of the
  transaction.** The switch was meant to be a no-op and was not: the frame it pushed carried no
  transaction, so the statements under it ran on a pooled connection and committed on their own — and with
  `use connection` the transaction itself was then left for the watchdog to end, dropping the work the
  body had already done. The frame carries the transaction now, which is what makes the switch the no-op
  it claims to be.
- **`rollback database transaction` cleared `last database error`.** The automatic rollback of a failed
  body keeps the cause; the rollback a script writes does the same now, so the line after the section
  still says what went wrong.

### Documentation

- A bilingual page for the row count, and the waiting material rewritten around it: the two troubleshooting
  entries about silent write failures are gone, along with the behaviour they described.
- The transaction timeout is written up in full: the spellings it accepts, the value it needs, when its
  clock starts, that it belongs to the transaction rather than to the connection, and what a long body or
  a statement that runs into the deadline does.
- An audit of every page turned up the places where the docs stopped short of the code, and they are fixed
  throughout: what a pool is and what bounds waiting for one; what a size in a table declaration applies
  to; which statements `store affected rows` answers "was it written" for; what a list variable cannot
  carry; what a ragged `insert many` does; what a `by id` write on a missing row reports; what a limit
  that resolves to nothing does; the order paging uses and why it is not a snapshot; what a refused read
  leaves in the result variable; what a failure does to the rest of the trigger; why a table can be
  "already registered"; and four recipes that could not have worked as written.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.1.0...v1.2.0

## [1.1.0] - 2026-09-18

A release about the databases a script can reach: MongoDB joins the type names, SQLite is documented as
reachable through the generic type, and the jar stops carrying drivers at all. Every layer is exercised in
CI — unit tests, real MySQL, PostgreSQL and MongoDB servers, and a real Paper server running the addon
against each of them.

### Added

- **MongoDB**, as the type name `"MongoDB"`. Everything the syntax says works on it: all fifteen column
  types, `where` clauses with every operator (`all`, `any` and negation included), paging, limits,
  `upsert`, `insert if absent` and the error channel. Its properties are its own: `url` takes a bare
  `host:port` or a whole `mongodb://`/`mongodb+srv://` connection string, `database` names the database to
  use, `auth database` names where the credentials belong, and `username`/`password` are separate
  properties, so a password containing `@`, `:` or `/` needs no escaping. See
  [Compatibility](docs/compatibility.md#mongodb) for what is deliberately different from SQL.
- **`auto increment` on MongoDB** is a counter document per key, in a collection named
  `skript_orm_sequences`. A statement that brings its own key raises the counter past it, and registering a
  table raises it past the highest key the collection already holds, so a key written here is never handed
  out a second time — the behaviour MySQL's `AUTO_INCREMENT` has, and the one PostgreSQL needs `setval`
  for.
- **SQLite**, through the generic `"JDBC"` type and the driver Paper already carries. Nothing the portable
  dialect writes is foreign to it, so a connection to a file works for everything that type supports; what
  it refuses (no auto increment, no `insert ... if absent`, no `upsert`, no limit on a write) stays
  refused.
- **`./gradlew gendocs`** generates the SkriptHub documentation JSON from the annotations on a disposable
  server, and checks it before writing it out. For contributors.

### Changed

- **The jar carries no database driver.** It used to shade and relocate one; now `plugin.yml` declares
  them as `libraries` and Paper downloads them into the server's `libraries/` on the first start, from the
  server's mirror of Maven Central. **Upgrade note:** a server that cannot reach that mirror will not load
  this plugin at all — the entry is a promise Paper treats as fatal. The way out is the mirror setting
  (`PAPER_DEFAULT_CENTRAL_REPOSITORY`, or the `org.bukkit.plugin.java.LibraryLoader.centralURL` system
  property), or copying a `libraries/` directory from a server that has started once. What is downloaded
  follows the modules the jar carries, so a server only ever fetches the drivers it can use.
- **The generic `"JDBC"` type is documented as what it is**: a driver you name yourself, plus portable SQL
  (`"double quoted"` identifiers, `LIMIT 1` for a single row, `LIMIT ? OFFSET ?` for paging) that refuses
  whatever has no portable form rather than guessing at it.
- **PostgreSQL is covered like the other implementations**: an integration suite against a real server, and
  the addon driven end to end against it on a real Paper server.
- **`statement timeout`** is honoured by MongoDB as well, as the driver's socket read timeout: a statement
  that never finishes stops holding a connection instead of waiting forever.

### Fixed

- **A `by id` written as a literal** reached the database unconverted, and reported
  `UnparsedLiterals must be converted before use` instead of reading the row.
- **A PostgreSQL `tinyint` column could not be read back**: the driver refuses a `Byte` from `int2`. It is
  now read as the `SMALLINT` it is stored as.
- **Paging on the generic `"JDBC"` type** binds `LIMIT` before `OFFSET`, the order every server here
  accepts.
- **Value binding on the generic `"JDBC"` type** uses the `setObject` overload every driver has, so
  SQLite's works for every type.
- **Raising the version left `plugin.yml` reporting the old one**, so a `1.1.0` jar announced itself as
  `1.0.0`. Found by the server test, which looks for the version it is testing.

### Documentation

- Bilingual pages for the type names, the JDBC dialect's SQL, SQLite, the column types' storage, and a
  MongoDB section that says what does not survive the move away from SQL — including that a `date` keeps
  the exact moment there, because BSON has no date-only type.
- The generated SkriptHub examples are parsed by the server test, so a documented example that the plugin
  cannot read fails CI rather than reaching a reader.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.0.0...v1.1.0

## [1.0.0] - 2026-09-17

The first release: the syntax for connecting, registering tables, and writing, reading, updating and
deleting rows, with `"MySQL"`, `"PostgreSQL"` and `"JDBC"` as the type names, and the PostgreSQL driver
shaded into the jar. The documentation for all of it is under [docs](docs/README.md).

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/commits/v1.0.0
