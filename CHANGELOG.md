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
