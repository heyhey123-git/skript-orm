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
| statements and round trips per operation | counted, not timed | B |
| allocations per operation | bytes, not time | A, B |
| peak heap | after the case, after a collection | B, C |
| MSPT percentiles (p50, p99, max) | the server's own ticks | C |

Trigger latency is reported next to the two halves rather than instead of them, because a statement can be slow without hitching — the work was off the thread — and can hitch while being fast, because the work was writing values.

### Why statement counts decide, and time only advises

Some metrics are noise and some are not. Wall-clock on a shared machine moves by ten to twenty percent between identical runs; a statement count does not move at all. **The suite therefore treats counts and allocations as findings and time as a hint**, and the reason is a measurement from this session.

A benchmark of `insert many` — 5000 rows of two columns, twenty times, the same script and the same table definition on every backend — measured **17.04 seconds against MySQL and 1.99 seconds against PostgreSQL and MariaDB**, about 170 microseconds per row against 20. The plugin sends MySQL and MariaDB the same SQL, since the two share a dialect, and in that run both connections pointed at the same server. What differed was the driver: unless it is told to rewrite a batch, the batch leaves as one statement per row, so the slow run sent 5000 statements per batch. The count is the whole explanation, and no timing metric could have shown it — the timing metric only said “MySQL is slow”, which was already known.

That count is arithmetic today, not a counter: the JDBC path hands rows over with `addBatch` and the driver decides what becomes of them, so the per-backend statement count is **to be measured** in phase 1. The arithmetic and the driver's own source agree that the difference is there; a number the suite can gate on needs the counter. See [Affected rows](affected-rows.md).

## The tools, so nobody builds a wheel

| Purpose | Tool | Why this one |
| --- | --- | --- |
| Microbenchmarks (A) | JMH, wired by hand | the harness that JDK engineers use; warmup, forks and dead-code elimination are its job, not ours |
| Statement and round-trip counts (B) | `datasource-proxy`, or `p6spy` | a JDBC proxy between the pool and the driver counts what was really sent, without changing the code under test |
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
- **The repository carries one `benchmarks/baseline.json`**, marked *reference only, not used for gating*. Its purpose is to give a local run a scale to read against, and to give these pages a single citable source for a number.
- **Absolute numbers are never compared across machines, only ratios.** A laptop and a shared runner differ by more than most regressions do.

Every result records the environment it was measured in, because otherwise a “regression” can be a changed dependency: JDK vendor and version, operating system and CPU, container image digests, driver versions, Paper and Skript versions, the commit, and whether the tree was dirty. The MySQL finding above is the worked example — without the driver's name and version, the same numbers describe three databases rather than two drivers.

## The four phases

- **Phase 0 — the module and the first cases.** A benchmarks module, the tools above wired to it, and cases for the costs the documentation already states: value conversion, storing a result per value and per kind of variable, reading a batch out of a variable, and the statements one `insert many` sends. It delivers JMH JSON, one summary table, and a first `baseline.json`.
- **Phase 1 — the JDBC matrix.** Every backend the plugin names, every statement it has, over a few table shapes, with the statement counter in place. This is the phase that turns “170 microseconds per row” into “5000 statements per batch”.
- **Phase 2 — the tick.** A disposable Paper server, with the library driven directly and through scripts, reporting MSPT percentiles and on-thread milliseconds per case. The 5000-row read ceiling and the 30 000-value write budget are stated in [Reading rows](reading.md#how-many-rows-one-read-may-store) and [Writing rows](writing.md#how-many-rows-one-write-may-send) as the largest size that stays inside a hitch nobody notices; **this phase re-derives both from a curve** — cost against size, against the tick — so the constants cite data instead of a decision.
- **Phase 3 — stress and fault injection.** Concurrency, cancellation, a database killed mid-statement, a plugin reloaded with queries in flight, a result past the ceiling, a batch far past the budget, an exhausted connection pool. These cases **assert** rather than print: nothing leaked, nothing stored that was refused, no pool slot held at rest, MSPT inside its bound, the server still answering.
- **Phase 4 — CI and documentation.** A fast subset on pull requests, the full matrix nightly, results archived, the counts gated and timing warned, and the numbers on these pages generated from a run rather than retyped.

## What this does not cover

- **Players.** Nothing here connects, walks or explores. A statement measured on a quiet server is not the same statement under a hundred chunks of ticking entities.
- **Long soaks.** Cases last seconds, not days; a leak that needs a week to appear is out of scope.
- **Backends the suite cannot start.** The generic JDBC connection reaches drivers this repository does not test, and a result measured on MySQL does not describe them.
- **Absolute numbers across machines**, as above — and **anything not yet measured**: where this page or the pages beside it names no number, the number is still to be measured.
