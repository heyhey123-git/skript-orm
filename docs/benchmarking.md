# Benchmarking and stress testing

[简体中文](benchmarking.zh-CN.md) | **English**

This page records how this plugin is measured and the decisions taken about it. It is a plan rather than a report: the numbers in the pages beside it — what storing a result costs, where the ceilings came from — were measured by hand, one question at a time. A number measured that way is written here as measured, and a number that has not been measured says so.

## What these numbers mean for a server

Read only this and you know the shape of it. A request too large to store is refused rather than run, because refusing protects the server and because what players notice is a stutter rather than an error message — the ceilings are the protection: a 10 000-row read is refused and stores nothing, and a write past the 30 000-value budget is split rather than refused.

How fast "fast" is, in three scales, each labelled with where it came from:

- **Throughput.** A 5000-row `insert many` measured 8.022 milliseconds on an Intel Xeon Platinum 8573C and 8.241 milliseconds on an AMD EPYC 7763 — the two CI runs still stored in the history, at `dev/bench/intel-xeon-platinum-8573c/data.js` and `dev/bench/amd-epyc-7763-64-core-processor/data.js`. Turned into a rate, that is between roughly 605 000 and 625 000 rows per second through the plugin's own hot path over that range. It is derived from those CI-recorded figures, not a score of its own.
- **The server's own heartbeat.** A tick is 50 milliseconds, the server's own heartbeat, and CI now measures what a request costs it on an AMD EPYC 9V45 runner. Reads of 100 to 5000 rows cost 60 to 90 milliseconds of wall clock and never pushed the server past one tick, and at 5000 and 10 000 rows the tick overrun was zero, so reading does not stall the server. A large write does: 5000 rows cost 220 milliseconds of wall clock and about 90 milliseconds of tick, and 10 000 rows cost 320 and about 100, so a big insert holds the server for roughly two ticks on that host. The limits CI asserts are the protection: a 10 000-row read is refused rather than run and stores nothing, and a write past the 30 000-value budget is split rather than refused. This is what "fast" means where it matters: no request can stall the server, because the sizes that would are refused.
- **The backend contrast, already measured.** The same SQL and the same server took 17.04 seconds against MySQL where PostgreSQL and MariaDB took 1.99, and the server-side count says why — MySQL got 5038 statements, MariaDB got 2. That is a fact, not advice.

There is a fourth comparison this page used to leave blank, and it still carries no timing: the plugin's own path against the equivalent raw SQL, on the same server, the same driver, the same rows and the same backend. What is known about it is a structural fact about the code. The plugin's `insert many` is a JDBC batch of single-row prepared statements, one per row, while the raw element has no batch form, so the only raw equivalent is one multi-row INSERT with the values written in — any timing of the two would therefore measure that batch-versus-multi-row shape, not the mapping layer alone.

A hand-written Skript loop was deliberately not compared: it stores into a plain variable, which is a memory variable against a database, not the same work. The comparison made here is raw SQL through the plugin's own connection, so the only thing that differs is the path, not the rows or the backend.

The tick numbers are the newest, and how they were taken matters more than the numbers: until a `tick` job was added to the benchmark workflow they existed only on a development machine, and the difference is the reason the change was worth making. The same 5000-row write measured about **10 milliseconds** of tick overrun on that machine and **90 milliseconds** on the runner, nine times as much. Anyone reading only the development figure would conclude that a large insert barely touches the tick.

A tick is the server's own heartbeat, 50 milliseconds, and an **overrun** is the longest tick in the window minus that 50 — the part of a tick above a tick's own length, which is how much longer than normal the server was held up. A statement costing 90 milliseconds of overrun made one tick take about 140, and everything the server would otherwise have done inside those 90 milliseconds — entities, redstone, players moving, another plugin's scheduled work — waited. Zero overrun says a statement did not lengthen a tick, not that it was free: a cost that fits inside 50 milliseconds has nothing to lengthen. The script's clock reads in 10 millisecond steps, so every figure in that column is a multiple of ten. Each `read` and `write` row is that statement's **first** run in the server it was measured on; the two `warm` rows repeat the 5000-row write and the 5000-row read at the very end, once the whole curve has already run. That size is the one the write budget and the read ceiling both land on, and doing it twice is what separates a first pass from a steady state — on this runner the first 5000-row write overran a tick by 90 milliseconds where the warm one overran it not at all. The warm rows report under names of their own and are never averaged into the curve.

What CI recorded, from Benchmarks #2 on commit `51aa760`, job `Tick benchmark`, stored per CPU at `dev/bench/amd-epyc-9v45-96-core-processor/tick/data.js`:

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

Read the 500-row write twice, because it is the honest part of the table: it overran a tick by 20 milliseconds while its whole wall clock was 30, which no single statement explains. The observer samples a shared host, and one sample can absorb a stall from somewhere else entirely. That is why these numbers are recorded and never gated, the workflow setting no threshold at all, and why a tick figure here is a shape rather than a precision.

Everything below is how those numbers were taken, what each came from, which machine produced it, and what is deliberately not measured.

## Three layers, and why they stay apart

The cost of one statement does not sit in one place, and the three places are read in two different clocks:

- **A: the JVM alone.** Value conversion, the SQL a dialect builds, how a batch is split, turning a result into rows, the bookkeeping around a transaction. No database and no server: JMH, in a module of its own, so ordinary builds do not pay for it.
- **B: real databases.** Round trips, the server's own parse and execute, the connection pool, commits. Testcontainers, a real instance per backend, with the fixtures the integration tests already use.
- **C: a real Paper server.** What a statement costs the *tick*: reading a batch out of a variable, writing a result into one, the memory a large result holds, MSPT. A disposable server, driven by scripts.

A single harness measuring “the plugin” measures nothing in particular, because the failures worth finding are layer-specific: a driver sending one statement per row (B), a variable store spending part of a tick per value (C), a converter allocating on every call (A).

**A `TimingStart` / `TimingEnd` pair around a statement is wall-clock, and wall-clock does not answer “did the server hitch”.** It measures how long the trigger waited, which is the honest answer to “how fast is my database” and the wrong answer to the question players ask. The database work happens off the server thread, while the tick is spent writing the answer into a variable and reading a batch out of one, on the thread. A faster database can leave a hitch untouched, and a slower one need not cause it. Both numbers are reported, separately, for every case.

## What every case reports

| Metric | Read in | Comes from |
| --- | --- | --- |
| on-thread milliseconds | time on the server thread | C |
| off-thread milliseconds | wall clock | B, C |
| trigger latency | wall clock, as the script sees it | B, C |
| throughput | per value and per row | A, B, C |
| statements and rows handed to the driver | counted, not timed | B |
| statements the server received, and the rows it wrote | counted, not timed | B, in the installed-server jobs |
| allocations per operation | bytes, not time | A, B |
| peak heap | after the case, after a collection | B, C |
| MSPT percentiles (p50, p99, max) | the server's own ticks | C |

Trigger latency is reported next to the two halves rather than instead of them, because a statement can be slow without hitching — the work was off the thread — and can hitch while being fast, because the work was writing values.

### Why statement counts decide, and time only advises

Some metrics are noise and some are not. Wall-clock on a shared machine moves by ten to twenty percent between identical runs; a statement count does not move at all. **The suite therefore treats counts and allocations as findings and time as a hint**, and the reason is a measurement from this session.

A benchmark of `insert many` — 5000 rows of two columns, twenty times, the same script and the same table definition on every backend — measured **17.04 seconds against MySQL and 1.99 seconds against PostgreSQL and MariaDB**, about 170 microseconds per row against 20. The plugin sends MySQL and MariaDB the same SQL, since the two share a dialect, and in that run both connections pointed at the same server. What differed was the driver: unless it is told to rewrite a batch, the batch leaves as one statement per row, so the slow run sent 5000 statements per batch. That count is the server's, not the JDBC layer's: the plugin handed the driver **one** batch and the driver turned it into five thousand statements, so a counter sitting at the JDBC layer would have reported one, correctly, and explained nothing. The count is the whole explanation, and no timing metric could have shown it — the timing metric only said “MySQL is slow”, which was already known.

Both counts are measured now, at the two levels that answer different questions. The JDBC layer is the counting driver wrapper in the benchmarks module, and on H2 in memory, over the generic JDBC path, a call of 5000 rows of six columns submitted **one** statement, the plugin reported 5000 affected rows with an exact count, and the table held 5000 rows once the call returned. The same benchmark measured 8.022 milliseconds on an Intel Xeon Platinum 8573C and 8.241 milliseconds on an AMD EPYC 7763 — the two CI runs still stored in the history, under `dev/bench/intel-xeon-platinum-8573c/` and `dev/bench/amd-epyc-7763-64-core-processor/`.

The server side is `ServerSideCountIntegrationTest`, which runs inside the jobs that already start a real server — `mysql-installed`, `postgres-installed` and the container jobs — so it needs no Docker of its own and adds no job. On the MySQL family it reads the server's own `Questions` counter either side of one plugin call, subtracts what the two probes themselves cost by running the same pair of reads around an empty block, and reads `Innodb_rows_inserted` beside it; `Com_insert` and `Com_stmt_execute` say which shape each path took. **The ratio is the answer**: 5000 rows carried by a handful of statements is a rewritten batch, and 5000 rows carried by 5000 statements is a batch sent one row at a time. Which of the two a driver does is the driver's decision and its version's, so the number is reported with the fingerprint it was measured under and never gates. Those counters are server-wide, so the case checks first that no other user is connected; when one is, it prints the numbers with that fact and aborts instead of asserting, because a delta that includes somebody else's writes is not this plugin's measurement.

**The table: the same batch, five thousand statements on one driver and two on the other.** The run that first put these numbers into the job log measured the same 5000-row insert many, through the same batch call, against five servers of the same plugin code. MySQL — 8.0.46 installed and 8.4.11 in a container — recorded **5038 statements for 5000 rows, a ratio of 1.0076 statements per row**, with `comInsert=5000` and `comStmtExecute=0`, so the batch leaves as one statement per row. MariaDB 11.4.13 recorded **2 statements** for the same work, `comInsert=1` beside `comStmtExecute=1`, one multi-row insert; it reports no inserted-row count to that user, so its rows-per-statement figure is **unavailable** and no ratio is stated for it rather than an invented one. PostgreSQL 16.15 is the third answer: its own counters cannot attribute rows to statements at all, so that half is **unavailable** with its reason printed beside it, and what it does report is here — 5000 rows recorded for the table, 5057 for the database, after waiting 814 milliseconds for views that fill asynchronously. Every backend reported the plugin's own count as 5000 and exact, and every table held 5000 rows.

That is the difference the timings pointed at — the same SQL, the same server, seventeen seconds against two — and it lives in the driver, which is why the shape is reported and never gated, and why the assertion that used to bound it was wrong and was removed after failing on this number. It is **one run on one host per backend**: a single point is a point and not a band. The fingerprint travelled on the same line as the number, so it can be quoted here: `os=Linux 6.17.0-1022-azure arch=amd64 cores=4 java=25.0.4.1 commit=81526798498f5ad30068fcb3299f2085062f868d`. The cpu field is the one to read twice — the MySQL and MariaDB lines say `Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz` while the PostgreSQL line, from the same run on the same label, says `AMD EPYC 7763 64-Core Processor`, so one run's numbers did not all come from one machine.

**PostgreSQL will not give the statement half.** Its shipped statistics count rows and transactions — `pg_stat_user_tables.n_tup_ins` and `pg_stat_database.tup_inserted`, which the same case reports and polls for, because those views are filled asynchronously — and nothing shipped says how many statements produced them, so no statement count appears there rather than one that means something else. Closing it needs `pg_stat_statements` loaded through `shared_preload_libraries` and a server restart, or a proxy that counts the protocol's statement messages as they go past. See [Affected rows](affected-rows.md).

## The tools, so nobody builds a wheel

| Purpose | Tool | Why this one |
| --- | --- | --- |
| Microbenchmarks (A) | JMH, wired by hand | the harness that JDK engineers use; warmup, forks and dead-code elimination are its job, not ours |
| Statements and rows handed to the driver (B) | `datasource-proxy`, or a counting driver wrapper | counts the plugin's own splitting without changing the code under test — but at this level a batch is one call whether it carries one row or five thousand |
| Statements and round trips the server received (B) | the database's own session counters, or a packet capture | the only level at which the driver's batching is visible, which is where the MySQL difference lived; it follows the driver version and its options, so it is reported with each run rather than gated |
| Percentiles over tick samples (C) | HdrHistogram | JMH reports its own confidence intervals; a sampling loop needs a histogram of its own |
| Real databases (B) | Testcontainers, with reuse enabled | the integration tests already start real instances; reuse stops every run paying for startup |
| History, comparison, pull-request comment, failure | `benchmark-action/github-action-benchmark` | stores each result, compares it against the history, comments on the pull request and fails the job past a threshold |
| Allocations and pauses | JMH `-prof gc`, JFR, async-profiler | bytes per operation, plus GC and safepoint detail when a case is slower than expected |
| MSPT (C) | Paper's own tick sampling | the tick measured without a plugin of ours having to be trusted |

JMH is wired by hand rather than through the `me.champeau.jmh` plugin: that plugin supports Gradle 8 and below, and this repository is on Gradle 9.1.0. The benchmarks module declares `jmh-core` and the annotation processor itself, runs them from a `JavaExec` task, and the report is JMH's JSON. That task is `./gradlew :benchmarks:jmh`, which writes `build/benchmarks/results.json`; `-Pbenchmarks.filter=<regex>` narrows a run to matching cases, and `-Pbenchmarks.forks=1` makes a local run quick. **JMH's annotation processor generates Java, so the benchmark classes are Java.** A Kotlin project can reach JMH through kapt or through kotlinx-benchmark, but neither buys anything here: these classes are a thin layer around the library, and a build plugin that has to be worked around is a wheel of its own.

## What a gate is

A gate is a check that fails and blocks the merge. A report nobody reads is not a gate, and a check that fails for reasons unrelated to the change is worse than none, because it gets forced through or switched off.

This repository already has gates: compilation, unit tests, ktlint, and compiling the integration tests. Performance gates are the same thing pointed at the metrics above, with one rule that follows from the noise: **only zero-noise metrics are hard gates.** Statements per operation, round trips and allocations per operation fail the build. Timing is reported with a warning threshold and does not fail it, because a threshold on shared runners that turns one run in ten red regardless of the change would be deleted within a month — and the count gates would go with it.

## Where the baseline lives

A regression is a comparison, so something has to hold the previous number, measured on comparable hardware.

- **The history lives in gh-pages**, managed by the CI action, and it is kept per CPU. That last clause corrects what this bullet used to say — that gates and trends compare results from *one runner model* against each other — because `ubuntu-latest` is a label over a pool, not a machine: two attempts of this workflow on the same commit with the same settings landed on an AMD EPYC 9V74 and an Intel Xeon Platinum 8573C, and the second reported 1.32 times the insert case and 1.21 times the row-limit case of the first with nothing but the host changed. That is hardware, and it is a larger move than most regressions this suite exists to catch, so a history that is not segmented by CPU cannot tell a slower machine from slower code — a jump on the chart may be a different runner rather than a change in the code. The action now writes each result under `dev/bench/<slug>`, the slug derived from the CPU model line the environment step already records, so a Xeon run is compared against Xeon points and an EPYC run against EPYC points. The measurement run that produced the server-side statement-count table above makes the same point from inside a single run: its MySQL and MariaDB jobs ran on an `Intel(R) Xeon(R) Platinum 8370C` while its PostgreSQL job, on the same label in the same run, ran on an `AMD EPYC 7763`. The pool hands out hosts per job rather than per run, so even the numbers of one run are not all from one machine.
- **That branch is storage rather than a site.** Nothing is published from it and there is no dashboard to browse: its tip holds a README and the action's data file, `dev/bench/<slug>/data.js`, which is a JavaScript assignment carrying raw JSON, written for the charting tool and for whoever eventually sets the alert threshold once there are enough nightly runs to read the spread off. The names inside it are that action's defaults rather than choices made here — the branch is called `gh-pages` because that is the default of `benchmark-action/github-action-benchmark` and follows GitHub Pages' convention that a branch publishing a static site carries that name, while what this project needed was storage belonging to no source branch. It is created by hand rather than by a run, because creating a branch is a push and a run that pushed its own storage into place would be writing outside the change it was run for. **Reading JSON is not a price of entry for trusting this addon**: a reader who wants the numbers should read the table in the README or the resource description, and the data file is there for the tooling.
- **The repository carries one `benchmarks/baseline.json`**, marked *reference only, not used for gating*, with the machine that produced it written beside it in `benchmarks/baseline.environment.txt`. Its purpose is to give a local run a scale to read against. The pair is written at the moment it is measured and from a clean tree, because a baseline whose commit is unknown describes nobody's code.
- **Absolute numbers are never compared across machines, only ratios.** A laptop and a shared runner differ by more than most regressions do.

Every result records the environment it was measured in, because otherwise a “regression” can be a changed dependency: JDK vendor and version, operating system and CPU, container image digests, driver versions, Paper and Skript versions, the commit, and whether the tree was dirty. The MySQL finding above is the worked example — without the driver's name and version, the same numbers describe three databases rather than two drivers.

## The four phases

- **Phase 0 — the module and the first cases.** A benchmarks module, the tools above wired to it, and cases for the costs the documentation already states: value conversion, storing a result per value and per kind of variable, reading a batch out of a variable, and the statements one `insert many` sends. It delivers JMH JSON, one summary table, and a first `baseline.json`.
- **Phase 1 — the JDBC matrix.** Every backend the plugin names, every statement it has, over a few table shapes, with the statement counter in place. This is the phase that turns “170 microseconds per row” into “5000 statements per batch”.
- **Phase 2 — the tick. Measured.** `./gradlew serverBenchmark` boots a real Paper server carrying Skript and this plugin — the same Paper build and the same plugin jars the tests boot, without the SkBee the test harness installs for the NBT interop — and drives it from scripts kept in a directory of their own, `server-benchmark/skript`, so that the checker demanding a declaration for every self-test name is never asked about a benchmark name. A statement is timed inside the script, in the tick the effect runs on: the ticks it spanned, and the longest tick inside that window. The scripts report on `SKRIPTORM_BENCH` lines, and the task fails the build on a `FAIL` line or on a write whose rows the table did not gain, so a run that measured nothing cannot pass for one that measured a fast write. What GitHub Actions asserts through the server-test suite are the ceilings, not the timings: a 5000-row write stores every row, a 10 000-row read is refused and stores nothing, and a write past the 30 000-value budget is split rather than refused with no step in the curve where the budget is. The timings are taken by CI too now: a `tick` job runs the same task nightly and on demand, prints every number with its machine on the same line, and stores the curve per CPU under `dev/bench/<slug>/tick/`, recorded and never gated. See [Reading rows](reading.md#how-many-rows-one-read-may-store) and [Writing rows](writing.md#how-many-rows-one-write-may-send). **No MSPT number is reported, because none was measured**, and one would have to be invented: percentiles over tick durations need an observer inside the tick loop, which is a plugin in the server rather than a script, and the script-side clock cannot stand in for it — it resolves to 10 milliseconds, it is quantized by the tick a parked trigger resumes on, and a longest-tick figure carries the observer's own cost.
- **Phase 3 — stress and fault injection.** Concurrency, cancellation, a database killed mid-statement, a plugin reloaded with queries in flight, a result past the ceiling, a batch far past the budget, an exhausted connection pool. These cases **assert** rather than print: nothing leaked, nothing stored that was refused, no pool slot held at rest, MSPT inside its bound, the server still answering. They assert where the tests already assert, using the same server harness that checks the log a run wrote against a list of expected lines — so a case here is a script plus the line it must produce, or must not.
- **Phase 4 — CI and documentation.** A fast subset on pull requests, the full matrix nightly, results archived, the counts gated and timing warned, and the numbers on these pages generated from a run rather than retyped.

## What this does not cover

- **Players.** Nothing here connects, walks or explores. A statement measured on a quiet server is not the same statement under a hundred chunks of ticking entities.
- **Long soaks.** Cases last seconds, not days; a leak that needs a week to appear is out of scope.
- **Backends the suite cannot start.** The generic JDBC connection reaches drivers this repository does not test, and a result measured on MySQL does not describe them.
- **The statement count on PostgreSQL.** Its statistics count rows and transactions rather than statements, so the server-side half of the count above exists for the MySQL family only. What PostgreSQL will say is how many rows it recorded, and that is what is reported there — as a row count, not as a statement count.
- **Absolute numbers across machines**, as above — and **anything not yet measured**: where this page or the pages beside it names no number, the number is still to be measured.
