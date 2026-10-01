# Benchmarking and stress testing

[简体中文](benchmarking.zh-CN.md) | **English**

This page records how this plugin is measured and the decisions taken about it. It is a plan rather than a report: the numbers in the pages beside it — what storing a result costs, where the ceilings came from — were measured by hand, one question at a time. A number measured that way is written here as measured, and a number that has not been measured says so.

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
| statements and round trips the server received | counted, not timed | B |
| allocations per operation | bytes, not time | A, B |
| peak heap | after the case, after a collection | B, C |
| MSPT percentiles (p50, p99, max) | the server's own ticks | C |

Trigger latency is reported next to the two halves rather than instead of them, because a statement can be slow without hitching — the work was off the thread — and can hitch while being fast, because the work was writing values.

### Why statement counts decide, and time only advises

Some metrics are noise and some are not. Wall-clock on a shared machine moves by ten to twenty percent between identical runs; a statement count does not move at all. **The suite therefore treats counts and allocations as findings and time as a hint**, and the reason is a measurement from this session.

A benchmark of `insert many` — 5000 rows of two columns, twenty times, the same script and the same table definition on every backend — measured **17.04 seconds against MySQL and 1.99 seconds against PostgreSQL and MariaDB**, about 170 microseconds per row against 20. The plugin sends MySQL and MariaDB the same SQL, since the two share a dialect, and in that run both connections pointed at the same server. What differed was the driver: unless it is told to rewrite a batch, the batch leaves as one statement per row, so the slow run sent 5000 statements per batch. That count is the server's, not the JDBC layer's: the plugin handed the driver **one** batch and the driver turned it into five thousand statements, so a counter sitting at the JDBC layer would have reported one, correctly, and explained nothing. The count is the whole explanation, and no timing metric could have shown it — the timing metric only said “MySQL is slow”, which was already known.

The count at the JDBC layer is measured now; the count the server receives is not. A counting driver wrapper sits at that layer in the benchmarks module, and on H2 in memory, over the generic JDBC path, a call of 5000 rows of six columns submitted **one** statement, the plugin reported 5000 affected rows with an exact count, the table held 5000 rows once the call returned, and the same run measured **9.960 ± 0.384 milliseconds per call** — that is the reference run in `benchmarks/baseline.json`, and the counter lines it recorded are left in `benchmarks/build/benchmarks/insert-many-counters.txt` by a run. What is still unmeasured is the per-backend count on the **server** side: MySQL, MariaDB and PostgreSQL each need a real instance, and this machine has no Docker, so no number of that kind is written here or in the baseline. See [Affected rows](affected-rows.md).

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

- **The history lives in gh-pages**, managed by the CI action. Gates and trends compare results from one runner model against each other, which is the only comparison that means anything.
- **The repository carries one `benchmarks/baseline.json`**, marked *reference only, not used for gating*, with the machine that produced it written beside it in `benchmarks/baseline.environment.txt`. Its purpose is to give a local run a scale to read against, and to give these pages a single citable source for a number. The pair is written at the moment it is measured and from a clean tree, because a baseline whose commit is unknown describes nobody's code.
- **Absolute numbers are never compared across machines, only ratios.** A laptop and a shared runner differ by more than most regressions do.

Every result records the environment it was measured in, because otherwise a “regression” can be a changed dependency: JDK vendor and version, operating system and CPU, container image digests, driver versions, Paper and Skript versions, the commit, and whether the tree was dirty. The MySQL finding above is the worked example — without the driver's name and version, the same numbers describe three databases rather than two drivers.

## The four phases

- **Phase 0 — the module and the first cases.** A benchmarks module, the tools above wired to it, and cases for the costs the documentation already states: value conversion, storing a result per value and per kind of variable, reading a batch out of a variable, and the statements one `insert many` sends. It delivers JMH JSON, one summary table, and a first `baseline.json`.
- **Phase 1 — the JDBC matrix.** Every backend the plugin names, every statement it has, over a few table shapes, with the statement counter in place. This is the phase that turns “170 microseconds per row” into “5000 statements per batch”.
- **Phase 2 — the tick. Measured.** `./gradlew serverBenchmark` boots a real Paper server carrying Skript and this plugin — the same Paper build and the same plugin jars the tests boot, without the SkBee the test harness installs for the NBT interop — and drives it from scripts kept in a directory of their own, `server-benchmark/skript`, so that the checker demanding a declaration for every self-test name is never asked about a benchmark name. On the machines that ran it the server booted in roughly 10 to 14 seconds — 10.4 to 11.4 seconds in one session and 12.9 to 13.6 in another — which is one machine's boot cost on the day rather than a property of the plugin. A statement is timed inside the script, in the tick the effect runs on: the ticks it spanned, and the longest tick inside that window. The scripts report on `SKRIPTORM_BENCH` lines, and the task fails the build on a `FAIL` line or on a write whose rows the table did not gain, so a run that measured nothing cannot pass for one that measured a fast write. Cost against size came out a curve rather than a point: a write of 100 rows of a six-column table overran its tick by 10 milliseconds and one of 10 000 rows by 100 to 110; 5000 rows, the size both constants land on, overran by 70 to 90 milliseconds cold, and on the JVM's second pass by about 30 milliseconds in one run while another kept the whole write inside a tick and so reported no overhang at all — a cost under a tick's 50 milliseconds has nothing to lengthen. Both constants held: 100 to 5000 rows stored every row and never lengthened a tick, 10 000 was refused and stored nothing, and a write past the 30 000-value budget is split rather than refused with no step in the curve where the budget is — see [Reading rows](reading.md#how-many-rows-one-read-may-store) and [Writing rows](writing.md#how-many-rows-one-write-may-send). **No MSPT number is reported, because none was measured**, and one would have to be invented: percentiles over tick durations need an observer inside the tick loop, which is a plugin in the server rather than a script, and the script-side clock cannot stand in for it — it resolves to 10 milliseconds, it is quantized by the tick a parked trigger resumes on, and a longest-tick figure carries the observer's own cost. The reference numbers these pages cite are in `benchmarks/baseline.json`, measured on the machine named in `benchmarks/baseline.environment.txt`.
- **Phase 3 — stress and fault injection.** Concurrency, cancellation, a database killed mid-statement, a plugin reloaded with queries in flight, a result past the ceiling, a batch far past the budget, an exhausted connection pool. These cases **assert** rather than print: nothing leaked, nothing stored that was refused, no pool slot held at rest, MSPT inside its bound, the server still answering. They assert where the tests already assert, using the same server harness that checks the log a run wrote against a list of expected lines — so a case here is a script plus the line it must produce, or must not.
- **Phase 4 — CI and documentation.** A fast subset on pull requests, the full matrix nightly, results archived, the counts gated and timing warned, and the numbers on these pages generated from a run rather than retyped.

## What this does not cover

- **Players.** Nothing here connects, walks or explores. A statement measured on a quiet server is not the same statement under a hundred chunks of ticking entities.
- **Long soaks.** Cases last seconds, not days; a leak that needs a week to appear is out of scope.
- **Backends the suite cannot start.** The generic JDBC connection reaches drivers this repository does not test, and a result measured on MySQL does not describe them.
- **Absolute numbers across machines**, as above — and **anything not yet measured**: where this page or the pages beside it names no number, the number is still to be measured.
