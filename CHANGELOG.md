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
