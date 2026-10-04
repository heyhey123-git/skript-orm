# Changelog

Changes for server administrators and script authors. The release workflow publishes the section
matching the version in `gradle.properties` and requires that section to exist.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). Add changes under `Unreleased`;
when releasing, rename that section to the version and date, then add a new `Unreleased` section.

## [Unreleased]

## [1.4.1] - 2026-10-03

Includes the 1.4.0 changes, which were not published on skUnity or MineBBS.

- **MariaDB support.** Use the dedicated `"MariaDB"` connection type with MariaDB Connector/J.
- **Better bulk writes.** Read `insert many` list variables across ticks and use parameterized multi-row
  inserts on MySQL, respecting value and packet limits while retaining affected-row counts.
  Keep the source variable unchanged until the write finishes; transaction timeouts also
  include the time spent reading it.
- **Safer reads and transactions.** Preserve local results, correctly replace saved global
  list results, and reject reads above 5000 rows without partial output. Discard a connection
  if rollback fails or a statement is still running when the transaction is aborted.
- **More reliable table definitions.** Improve repeated registration, compatible-column
  checks, and NULL, numeric, and database-specific type handling.
- **Clearer documentation.** Rewrite the bilingual guides and report nanosecond timings
  for SQLite, MySQL, MariaDB, PostgreSQL, and MongoDB.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.3.0...v1.4.1

## [1.4.0] - 2026-10-01

This release adds a dedicated MariaDB connection type, sets limits on large reads and writes,
and improves schema compatibility checks. MariaDB Connector/J sends batches differently from
MySQL Connector/J's tested defaults. Reads over 5000 rows now fail without storing partial
results; writes exceeding 30 000 bound values are split across statements. The fixes below
also cover repeated table registration, NULL and numeric conversion, and database-specific
type names.

### Added

- **MariaDB now has its own connection type, `"MariaDB"`.** It uses the MySQL SQL dialect but
  loads MariaDB Connector/J, which Paper downloads on first startup. Use a
  `jdbc:mariadb://…` URL; a driver and URL mismatch is rejected when connecting.
  In the tested configuration, MariaDB Connector/J sent batch inserts efficiently
  while retaining accurate affected-row counts. MySQL Connector/J sent one insert
  per row unless batch rewriting was enabled; rewriting lost the usable row count.

### Changed

- **Reads over 5000 rows now fail without storing a partial result.** This applies to
  `select many` and `select page`; an oversized page is rejected before the query runs.
  Storing a large Skript list consumes server-thread time. Narrow the result with
  `where`, or read it in pages of at most 5000 rows.
- **Large `insert many` batches are split across statements.** A statement binds at most
  30 000 values, and the affected-row count covers the whole batch when the driver reports
  an exact count. If a later statement fails, earlier statements remain applied unless the
  write runs in a transaction. Wider tables fit fewer rows per statement.

- **Large results are stored more efficiently.** For results above 10 000 values, the
  plugin builds the variable tree off the server thread and attaches it in one step.
  Smaller results use the existing path. If the running Skript version uses an
  incompatible variable layout, the optimization disables itself and logs a notice.

### Fixed

- **Repeated table registration succeeds.** Registering the same definition on a connection
  no longer reports `Table 'users' is already registered.` A changed definition is checked
  against the database so incompatible changes fail during registration.
- **Numbers retain their text representation in `string` columns.** For example, `42`
  is stored as `42`, not `42.0`.
- **SQL `NULL` values read as unset.** Numeric, date, and boolean columns containing
  `NULL` no longer fail during conversion.
- **Schema checks allow extra database columns.** A table may contain columns omitted
  from the script declaration. Declared columns and the declared primary key are still
  checked. An extra `not null` column without a default may still cause an insert to fail.
- **Schema checks allow wider compatible columns.** For example, a `TEXT` column can
  satisfy a `string(255)` declaration. Fixed-width `BINARY(16)` for `uuid` must still
  match exactly, because a wider column returns padded values.
- **MySQL-family boolean columns register under their driver-reported type names.**
  Depending on the `tinyInt1isBit` setting, a `BOOLEAN` column may be reported as
  `BIT`, `BOOLEAN`, or `TINYINT`. The schema check now accepts each form.
- **Schema type aliases are checked by database dialect.** This prevents a name with
  different meanings across databases from matching the wrong storage type. For
  example, PostgreSQL `bit(1)` is a bit string, not a MySQL-family boolean.
  The shared aliases now treat `LONGVARCHAR` as text and accept the standard names
  `BINARY VARYING` and `DOUBLE PRECISION`. H2-specific `FLOAT` mapping is handled
  only for H2.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.3.0...v1.4.0

## [1.3.0] - 2026-09-29

This release adds raw SQL and MongoDB commands for operations that table declarations cannot
express. It also reports unknown connection properties and incompatible table declarations
at the point where they are written.

### Added

- **Raw statements.** `execute query`, `execute update`, and `execute command` send SQL
  or a command document as written. SQL values can be bound to `?` placeholders with
  `with`; the placeholder count is checked before execution. The plugin does not
  validate raw statements against registered tables or translate them between
  implementations. SQL statements require a SQL connection; commands require a
  document database. See [Raw statements](docs/raw-statements.md).

### Fixed

- **Unknown connection properties now fail immediately.** `create a connection` checks
  property names against those supported by the selected implementation, so typos no
  longer go unnoticed.
- **Incompatible table declarations now fail during registration.** After
  `CREATE TABLE IF NOT EXISTS`, the plugin checks the existing table's columns,
  types, sizes, nullability, and primary key against the script declaration.

### Changed

- **Failures use Skript's runtime error channel.** Console errors now include the
  script, syntax, and line number. `last database error` still contains the full
  error for the current event.
- **Startup logs available database implementations.** A failed implementation is
  listed with its initialization error.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.2.0...v1.3.0

## [1.2.0] - 2026-09-19

This release adds affected-row counts and makes every database statement wait for completion,
so scripts can check the result or error on the next line.

### Added

- **`store affected rows in {_rows}`** is available on inserts, updates, upserts,
  and deletes, including their forms without a colon. A value of `0` is a reported
  count; an unset variable means the operation did not provide an exact count or
  did not complete. The count supports conditional writes that detect a concurrent
  change. See [Affected rows](docs/affected-rows.md) for backend differences and
  an example with bounded retries.

### Changed

- **Every statement now waits for completion.** The next line can read the result
  or `last database error`. Scripts that relied on writes continuing in the
  background will now wait. `and wait` remains valid for compatibility but no
  longer changes behavior.

### Fixed

- **MySQL `insert entity if absent` now suppresses only duplicate-key errors.**
  It no longer uses `INSERT IGNORE`, which could silently truncate values or
  replace invalid `NULL` values with defaults. The count remains `1` for an
  insert and `0` when the row already exists; other errors fail the statement.
- **Replacing an unnamed default connection now closes it.** Previously it could
  remain open without a name or a way to select it. Named connections remain open.
  Changing the default waits for the old connection to close and is refused during
  a transaction.
- **Failed reads now clear the result variable even when rejected before execution.**
  A missing connection, unknown table, invalid filter value, or invalid page number
  no longer leaves an earlier query's result in place.
- **Out-of-range integers now fail instead of wrapping.** For example, an `int`
  column no longer stores `5000000000` as `705032704`. The error names the column
  and valid range, whether the value came from `values`, a variable, or `where`.
  Fractions in integer columns are still truncated toward zero. See
  [Types](docs/types.md#what-a-mismatch-looks-like).
- **Database-reported errors now mark a transaction for rollback.** Later database
  statements in the section are skipped, the transaction rolls back, and the
  original error remains available to the script.
- **Selecting a transaction's existing connection now keeps the transaction active.**
  Previously, later statements could run outside it and commit separately.
- **`rollback database transaction` now preserves `last database error`.** The
  script can still inspect the failure after leaving the transaction section.

### Documentation

- Added bilingual affected-row documentation and updated the waiting guide.
- Documented transaction timeout syntax and behavior, including when its timer starts.
- Corrected examples and clarified connection pools, table sizes, list variables,
  batch inserts, missing rows, pagination, failed reads, and repeated registration.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.1.0...v1.2.0

## [1.1.0] - 2026-09-18

This release adds MongoDB support, documents SQLite through the generic JDBC type, and
moves database drivers out of the plugin jar. CI covers unit tests, real databases,
and the addon running on Paper.

### Added

- **MongoDB support** under the `"MongoDB"` type. It supports all fifteen column
  types, `where` operators, paging, limits, upserts, conditional inserts, and error
  reporting. Connection properties include `url`, `database`, `auth database`,
  `username`, and `password`. See [Compatibility](docs/compatibility.md#mongodb)
  for differences from SQL backends.
- **MongoDB `auto increment`** uses counter documents in `skript_orm_sequences`.
  Explicit keys and existing collection data advance the counter to avoid reusing keys.
- **SQLite support** through the generic `"JDBC"` type and Paper's driver. Features
  without a portable SQL form remain unsupported: auto increment, conditional
  insert, upsert, and write limits.
- **`./gradlew gendocs`** generates and validates SkriptHub documentation JSON
  from the annotations on a temporary server.

### Changed

- **Database drivers are no longer bundled in the jar.** Paper downloads the
  drivers listed in `plugin.yml` into `libraries/` on first startup. Servers
  without access to the configured Maven mirror must provide those libraries
  separately or configure a reachable mirror.
- **The generic `"JDBC"` type** uses portable SQL and reports unsupported
  operations rather than guessing at database-specific syntax.
- **PostgreSQL integration tests** now run against a real database and Paper server.
- **MongoDB honors `statement timeout`** through the driver's socket read timeout.

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

- Added bilingual documentation for type names, JDBC and SQLite behavior, column
  storage, and MongoDB differences.
- Server tests now parse generated SkriptHub examples to catch invalid syntax.

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/compare/v1.0.0...v1.1.0

## [1.0.0] - 2026-09-17

The first release: the syntax for connecting, registering tables, and writing, reading, updating and
deleting rows, with `"MySQL"`, `"PostgreSQL"` and `"JDBC"` as the type names, and the PostgreSQL driver
shaded into the jar. The documentation for all of it is under [docs](docs/README.md).

**Full Changelog**: https://github.com/heyhey123-git/skript-orm/commits/v1.0.0
