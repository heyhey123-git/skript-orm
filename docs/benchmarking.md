# Benchmarking and stress testing

[简体中文](benchmarking.zh-CN.md) | **English**

This page describes the benchmark setup, recorded results, and planned tests. Results identify their
test environment; work that has not yet been measured is marked as planned.

## What these numbers mean for a server

Reads that exceed the 5000-row result limit fail without storing a partial result. Writes that exceed
30 000 bound values are split across statements. These limits protect against oversized operations,
although a permitted large write can still lengthen a server tick.
For `insert many` from a list variable, the addon reads the source across ticks, with a
budget of roughly 2 ms or 4096 steps per pass. This reduces the main-thread work in any one
pass, but the operation may take more ticks to finish. The script must leave the source variable
unchanged until the write completes.

The measurements cover three different costs:

- **Plugin throughput.** A 5000-row `insert many` took 8.022 ms on an Intel Xeon Platinum 8573C
  and 8.241 ms on an AMD EPYC 7763. Those CI runs are stored in
  `dev/bench/intel-xeon-platinum-8573c/data.js` and
  `dev/bench/amd-epyc-7763-64-core-processor/data.js`.
- **Historical server ticks.** On an AMD EPYC 9V45 CI runner, the older script reported a 5000-row write at 220 ms overall and
  lengthened the longest tick by 90 ms beyond its 50 ms budget. A 10 000-row write took 320 ms
  overall and overran a tick by 100 ms. The 5000-row read had no tick overrun in that run;
  the 10 000-row read was refused. The table below contains the other measurements.
- **Historical database driver behavior.** In a separate benchmark, twenty 5000-row inserts took 17.04 s
  with MySQL and 1.99 s with PostgreSQL or MariaDB. For one batch, the MySQL server counted
  5038 statements, including 5000 inserts; MariaDB counted two statements, including one
  multi-row insert. These runs predate the current MySQL insert path and describe only those
  driver configurations.

The current server benchmark logs a 5000-row comparison between `insert many` and hand-written
SQL for SQLite, MySQL, MariaDB, and PostgreSQL. MySQL now uses a parameterized multi-row
`INSERT` by default, with at most 30 000 bound values per statement; the other JDBC paths use
prepared batches. The raw comparison uses one literal multi-row `INSERT`, built before its
timer starts. The measured difference includes reading the plugin's source variable, parameter
binding, and the two write shapes; it cannot be attributed to ORM mapping alone. MongoDB runs
the plugin write and read cases, then explicitly skips the SQL comparison.

A Skript loop that writes to a regular variable would measure in-memory storage, so it is not
a useful comparison with database inserts.

That older 5000-row run reported about 10 ms of tick overrun on a development machine
and 90 ms on the CI runner. The benchmark workflow records the host alongside each result.

The current server benchmark uses skript-reflect to call `System.nanoTime()` around each
operation and at the start of each observed tick. It logs elapsed nanoseconds (`wallNs`)
immediately when the operation returns, before validation queries or counting result rows.
It also logs the longest interval between observed tick starts (`gapNs`). The report shows
milliseconds to six decimal places; the JSON report and CI history retain the original integer
nanosecond values. Nanoseconds are the timer's unit, not a guarantee of nanosecond accuracy:
scheduler delays, other plugins, GC,
database work, and Skript's resume tick all affect the result.

**Tick overrun** is the longest observed tick-start gap minus 50 ms, floored at zero.
It reveals a delayed tick but does not directly measure the operation's main-thread CPU
time. Work that fits within a normal tick is invisible to this metric. Each `read` and
`write` row is the first run at that size; `warm read` and `warm write` repeat 5000 rows
after the other cases and are reported separately.

## Current CI coverage

The tick job runs plugin writes and reads at 100, 500, 1000, 2500, 5000, and 10 000 rows,
followed by a warm 5000-row write and read, for every backend below. It also runs a separate
5000-row plugin-versus-raw-SQL comparison where SQL is available. The input rows populate only
the `id` column; this keeps the comparison focused on row transport and storage rather than a
particular six-column value mix.

| Backend | Plugin curve and warm case | Raw SQL comparison | New CI results |
| --- | --- | --- | --- |
| SQLite | Configured | Configured | [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500), AMD EPYC 7763, SQLite |
| MySQL | Configured | Configured | [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500), Intel Xeon 6973P-C, `mysql:8.4` |
| MariaDB | Configured | Configured | [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500), AMD EPYC 7763, `mariadb:11.4` |
| PostgreSQL | Configured | Configured | [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500), AMD EPYC 7763, `postgres:17` |
| MongoDB | Configured | Skipped: SQL is unavailable | [Run #4](https://github.com/heyhey123-git/skript-orm/actions/runs/37101757500), Intel Xeon 6973P-C, `mongo:8` |

The SQL comparison logs `pluginwrite`, `pluginread`, `rawwrite`, and `rawread` as
`SKRIPTORM_BENCH=COMPARE` lines. MongoDB logs its plugin cases and an explicit
`SKRIPTORM_BENCH=SKIP` for the raw SQL cases.

Run #4 completed successfully on 2026-10-03, on commit `6476c9b`, with Paper `26.2 build 124`, Skript `2.16.2`,
Java `25.0.4.1`, Linux `6.17.0-1022-azure`, and four visible CPUs. The following 5000-row
measurements are the values from that run. `wallNs` is shown here in milliseconds for readability;
the uploaded JSON and CI history retain nanoseconds. Each value is one run on a shared GitHub
runner, so it describes that host and database image rather than a general ranking.

| Backend | Write wall | Write gap | Read wall | Read gap |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 735.828866 ms | 65.767643 ms | 103.186998 ms | 103.066121 ms |
| MySQL | 462.517747 ms | 50.395059 ms | 78.302884 ms | 78.218777 ms |
| MariaDB | 685.122311 ms | 66.066614 ms | 95.789059 ms | 95.813175 ms |
| PostgreSQL | 585.103286 ms | 66.926809 ms | 94.172914 ms | 94.082624 ms |
| MongoDB | 717.433104 ms | 50.968606 ms | 78.761947 ms | 78.579400 ms |

Larger writes and the repeated 5000-row write from the same curve:

| Backend | 10 000-row write wall | 10 000-row write gap | Warm 5000-row write wall | Warm write gap |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 1501.908676 ms | 94.670715 ms | 779.342861 ms | 50.628368 ms |
| MySQL | 1398.534310 ms | 51.944607 ms | 684.881529 ms | 50.515912 ms |
| MariaDB | 1149.905662 ms | 101.135769 ms | 675.078706 ms | 50.746686 ms |
| PostgreSQL | 1255.801227 ms | 95.786030 ms | 623.383051 ms | 50.885682 ms |
| MongoDB | 1196.824500 ms | 53.798503 ms | 683.646486 ms | 50.583169 ms |

The following comparison runs separately from the curve above. All four columns are elapsed
time for 5000 rows. The plugin's write includes sliced input reading; the raw SQL string is
already built when its timer starts. These are different write paths, so their elapsed-time
ratio does not measure ORM overhead alone.

| Backend | Plugin write | Raw SQL write | Plugin read | Raw SQL read |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 727.519482 ms | 17.148948 ms | 112.968835 ms | 104.414372 ms |
| MySQL | 586.972838 ms | 46.793973 ms | 76.245264 ms | 91.635107 ms |
| MariaDB | 628.867037 ms | 74.916475 ms | 91.608649 ms | 89.937610 ms |
| PostgreSQL | 778.881589 ms | 61.349564 ms | 99.224999 ms | 102.880893 ms |
| MongoDB | 685.494020 ms | Not applicable | 78.850687 ms | Not applicable |

The benchmark also completed the 100, 500, 1000, 2500, and 10 000-row cases, the warm 5000-row
cases, affected/stored-row checks, and the expected refusal of a 10 000-row read. The current
report does not treat the gap as plugin CPU time: it is the largest observed interval between
tick starts, and it includes scheduler and host activity. The run predates the later MySQL
`max_allowed_packet` fix in commit `21898af`; repeat the benchmark after that fix is pushed if
the exact current MySQL path is needed.

The following historical run used Skript's coarse `now` clock and measured some writes
through their subsequent count query. These values are retained as context, not as a
baseline for the new timer. They come from Benchmarks #2 on commit `51aa760`, job
`Tick benchmark`, stored per CPU at `dev/bench/amd-epyc-9v45-96-core-processor/tick/data.js`:

| What | Rows | Wall clock | Tick overrun |
| --- | --- | --- | --- |
| read | 100 | 60 ms | 10 ms |
| read | 500 | 60 ms | — |
| read | 1000 | 60 ms | — |
| read | 2500 | 100 ms | — |
| read | 5000 | 90 ms | 0 ms |
| read | 10 000 | 60 ms | 0 ms |
| warm read | 5000 | 70 ms | 0 ms |
| write | 100 | 90 ms | 10 ms |
| write | 500 | 30 ms | 20 ms |
| write | 1000 | 80 ms | 30 ms |
| write | 2500 | 70 ms | 30 ms |
| write | 5000 | 220 ms | 90 ms |
| write | 10 000 | 320 ms | 100 ms |
| warm write | 5000 | 190 ms | 0 ms |

The 500-row write shows the limits of this measurement: it took 30 ms overall, yet the
longest tick overran by 20 ms. Activity elsewhere on the shared host may have contributed.
Tick timings are reported for comparison, but the workflow does not fail on them.
Do not compare this table directly with runs using `wallNs` and `gapNs`.

The sections below distinguish the available measurements from planned ones and show
where each metric can be collected.

## Measurement layers

The benchmarks use three layers to help locate the source of a slowdown:

- **A: JVM benchmarks without Paper.** JMH cases live in a separate module. Pure code
  paths can be measured without a database; the current insert case uses in-memory H2,
  so its result includes database work as well as addon code.
- **B: real databases.** Testcontainers starts each backend using the integration-test
  configuration. This layer captures round trips, parsing and execution, connection-pool
  behavior, and commits.
- **C: Paper server.** Scripts run on a disposable server and record elapsed time and tick
  gaps while the addon reads source variables and stores results. Memory use and MSPT
  percentiles require separate measurements.

Separating the layers helps identify the source of a slowdown: value conversion in A, driver
batching in B, or Skript variable access on the server thread in C.

Trigger latency includes database work, main-thread variable handling, and the wait until
Skript resumes the trigger. A long tick gap shows a delay, but cannot divide that time
among those causes.

## What the current server cases report

The Paper scripts currently log `wallNs`, `gapNs`, tick count, and affected or stored
row counts. The report derives tick overrun from `gapNs` and validates the counts.
It does not isolate main-thread CPU time, database time, allocations, or MSPT percentiles.

### Additional measurements planned

The following are targets for the separate measurement layers, not fields emitted by
every current server case:

| Metric | Planned measurement | Layer |
| --- | --- | --- |
| main-thread time | time spent on the server thread during an operation | C |
| background time | time spent doing database work off the server thread | B, C |
| trigger latency | elapsed time from statement start until Skript resumes | B, C |
| throughput | rows and values processed per second | A, B, C |
| statements and rows passed to the driver | calls and rows counted at the JDBC boundary | B |
| statements received and rows written by the database | database-side counters | B |
| allocations per operation | bytes allocated per call | A, B |
| peak heap | maximum heap use sampled during an operation | B, C |
| retained heap | heap use after the case and a collection | B, C |
| MSPT distribution (p50, p99, max) | server tick durations | C |

Separating trigger latency from database and main-thread time will require additional
instrumentation. The current `wallNs` reading includes all three.

### Counts and timings

Timings on shared CI hosts have varied by 10% to 20% between identical runs. Statement counts
are more stable, so CI can check counts directly. Timings are reported without failing a build.

The historical twenty-batch test above used the same script and table definition for each database.
MySQL and MariaDB share a SQL dialect, but their drivers sent the batch differently.
At the JDBC layer, the plugin submitted one batch at that time. The server-side count showed that
Connector/J sent its 5000 rows as 5000 inserts. Timing alone would not identify that cause.
The current MySQL path instead builds a parameterized multi-row insert; the counts below
describe the earlier path and should not be used as current expectations.

The benchmarks module also counts calls at the JDBC layer. With in-memory H2 and the generic
JDBC path, a 5000-row, six-column insert submitted one batch. The plugin reported exactly
5000 affected rows, and the table contained 5000 rows after the call.

`ServerSideCountIntegrationTest` measures what a real database receives. For MySQL-family
servers, it reads `Questions` before and after a plugin call and subtracts the probes'
own cost. It also reads `Innodb_rows_inserted`, `Com_insert`, and `Com_stmt_execute`.
Because these counters cover the whole server, the test checks for other connections first;
if it finds any, it reports the measurements without asserting them. Driver versions and
options affect these counts, so they are recorded with the environment and do not gate CI.

In one run of the 5000-row test, MySQL 8.0.46 and 8.4.11 each recorded **5038 total
statements**, including 5000 inserts (`comInsert=5000`, `comStmtExecute=0`). MariaDB
11.4.13 recorded **2 statements**, including one multi-row insert (`comInsert=1`,
`comStmtExecute=1`). MariaDB did not expose an inserted-row count to the test account.
PostgreSQL 16.15 reported 5000 inserted rows for the table and 5057 for the database,
but its available counters could not provide a statement count. The PostgreSQL views
took 814 ms to update. In all cases, the plugin reported 5000 affected rows and the
table contained 5000 rows.

These are single runs per backend, not a range. Their logs include
`os=Linux 6.17.0-1022-azure arch=amd64 cores=4 java=25.0.4.1 commit=81526798498f5ad30068fcb3299f2085062f868d`.
Even within that workflow run, MySQL and MariaDB ran on an Intel Xeon Platinum 8370C,
while PostgreSQL ran on an AMD EPYC 7763. Compare timings only with the host recorded
for each job.

The test reads PostgreSQL's `pg_stat_user_tables.n_tup_ins` and
`pg_stat_database.tup_inserted`, polling until these asynchronous statistics update.
They count rows, not the statements that inserted them. A statement count would need
`pg_stat_statements` configured and the server restarted, or a protocol-level counter.

## Tools and options

JMH, Testcontainers, and benchmark history are part of the current setup. The other
tools below are options for measurements that are not yet collected.

| Purpose | Tool | Role |
| --- | --- | --- |
| Microbenchmarks (A) | JMH | provides warmup, separate forks, and protection against dead-code elimination |
| Statements and rows passed to the driver (B) | `datasource-proxy` or a counting driver | counts JDBC calls; the number of rows within a batch needs separate tracking |
| Statements and round trips seen by the database (B) | session counters or packet capture | shows what the driver sends; results depend on driver version and connection settings |
| Percentiles over tick samples (C) | HdrHistogram | computes a distribution from repeated tick samples |
| Real databases (B) | Testcontainers | starts test databases; instance reuse reduces repeated startup time |
| History and comparison | `benchmark-action/github-action-benchmark` | stores results by CPU and backend without failing the job on timing changes |
| Allocations and pauses | JMH `-prof gc`, JFR, async-profiler | examines allocations, GC, and safepoint pauses |
| MSPT (C) | Paper's tick sampling | records tick durations from the server itself |

The benchmarks module declares JMH and its annotation processor directly because the available
Gradle plugin does not support this project's Gradle version. Run `./gradlew :benchmarks:jmh`
to write `build/benchmarks/results.json`. Use `-Pbenchmarks.filter=<regex>` to select cases
or `-Pbenchmarks.forks=1` for a shorter local run. Benchmark classes are Java because JMH's
annotation processor generates Java code.

## CI checks

Compilation, unit tests, ktlint, and integration-test compilation already run in CI.

The current job fails on incorrect row counts or script errors, not timing changes.
Other stable, reproducible checks can be added separately from timing trends, which
vary on shared CI hosts.

## Where the baseline lives

Benchmark results are compared only with runs from comparable hardware.

- **CI history** is stored by CPU model under `dev/bench/<slug>/` on `gh-pages`.
  The `ubuntu-latest` label covers different processors: two runs of the same commit
  landed on an AMD EPYC 9V74 and an Intel Xeon Platinum 8573C, with insert timings
  differing by a factor of 1.32. Each CI job records its own CPU model. Current tick
  history is separated further by backend at `dev/bench/<slug>/tick-ns/<backend>/`.
- **The `gh-pages` branch** stores data for the benchmark action; this project does not
  publish a dashboard from it. JMH results are written to
  `dev/bench/<slug>/data.js`; tick results use the backend path above.
- **`benchmarks/baseline.json`** is a reference for local runs, not a CI threshold.
  `benchmarks/baseline.environment.txt` records the machine used to produce it.

Results also record the JDK, operating system, CPU, container images, database drivers,
Paper and Skript versions, commit, and whether the worktree had local changes.

## Test plan

- **Phase 0: microbenchmarks.** Add JMH cases for value conversion, result storage,
  batch input, and the statements sent by `insert many`. Store the JSON results and
  a reference baseline.
- **Phase 1: database matrix.** Measure each supported backend and operation with
  statement counters and several table shapes.
- **Phase 2: server ticks (configured across five backends).** `./gradlew serverBenchmark` starts Paper with
  Skript, skript-reflect, and this addon, then runs scripts from `server-benchmark/skript`.
  The scripts report `SKRIPTORM_BENCH` lines with nanoseconds as the timer unit and the
  longest observed tick-start gap. The task
  fails if a write stores the wrong number of rows or a script reports `FAIL`.
  CI checks the 5000-row read limit and 30 000-value write budget separately; a
  nightly and on-demand `tick` matrix records results under `dev/bench/<slug>/tick-ns/<backend>/`.
  See [Reading rows](reading.md#how-many-rows-one-read-may-store) and
  [Writing rows](writing.md#how-many-rows-one-write-may-send). MSPT percentiles
  are not available from the script-side timer.
- **Phase 3: stress and failure tests (planned).** Test concurrent work,
  cancellation, interrupted database connections, plugin reloads during queries,
  oversized results and batches, and exhausted connection pools.
- **Phase 4: further CI reporting (planned).** Add the remaining metrics above and keep
  stable correctness checks separate from timing trends.

## What this does not cover

- **Active players.** Measurements on a quiet server do not predict the same operation under heavy player and entity load.
- **Long-running tests.** Current cases last seconds, so they cannot detect leaks that take days to appear.
- **Untested drivers.** Generic JDBC can use drivers outside this repository's test matrix; MySQL results do not represent them.
- **PostgreSQL statement counts.** The available statistics count rows and transactions, not individual statements.
- **Cross-machine timing comparisons.** Timings are meaningful only with their test environment.
