# Benchmarks and load testing

[简体中文](benchmarking.zh-CN.md) | **English**

This page describes the current benchmark and its results. All scores come from [Run #7](https://github.com/heyhey123-git/skript-orm/actions/runs/37205411783), which tested commit `76dcc7e` on 2026-10-04. Server tables show milliseconds with six decimal places; JSON retains integer nanoseconds. JMH cases state their time unit per operation separately.

A server normally updates 20 times per second. Each update is a tick, with a target interval of about 50 ms. The scripts call `System.nanoTime()` through skript-reflect. Nanoseconds are the timing unit, not a guarantee of nanosecond accuracy: scheduling, JVM garbage collection, database execution and trigger resumption affect the measurements.

## What these numbers mean for a server

Local inputs owned by the paused trigger can be read and checked in the background. Global inputs are read on the server thread across ticks, with at most 4096 processing steps per slice and a target duration of about 2 ms. Shared default variables fall back to that thread. Ordinary local results can also be stored in the background; the local context is restored before the trigger continues.

Conversions that need server APIs, including items and locations, share one queue with a target budget of 2 ms per tick. A single expensive conversion cannot be interrupted, so this is not a hard limit. Final publication of global results still runs on the server thread and is outside that conversion budget. Prefer local results and smaller pages for large reads.

One read stores at most 5000 rows. Larger results are refused without keeping a partial result. A write statement binds at most 30,000 values; larger batches are split into multiple statements. Splitting alone does not guarantee that all statements succeed or roll back together. See [Reading rows](reading.md), [Writing rows](writing.md) and [Transactions](transactions.md).

## CI coverage and environment

Plugin reads and writes passed on all five databases, as did JMH and report publication. The server used Paper 26.2 build 124, Skript 2.16.2, skript-reflect 2.6.3 and Java 25. This table checks coverage, not speed. **Backend** names the database; **Plugin cases** covers the size curve, repeated cases and scope cases; **Raw SQL comparison** says whether handwritten SQL ran; **CPU / database** identifies the job environment. MongoDB uses document commands and does not support SQL, so only its SQL comparison is skipped.

| Backend | Plugin cases | Raw SQL comparison | CPU / database |
| --- | --- | --- | --- |
| SQLite | Completed | Completed | AMD EPYC 9V74 / SQLite |
| MySQL | Completed | Completed | AMD EPYC 7763 / `mysql:8.4` |
| MariaDB | Completed | Completed | AMD EPYC 9V45 / `mariadb:11.4` |
| PostgreSQL | Completed | Completed | AMD EPYC 9V74 / `postgres:17` |
| MongoDB | Completed | Not applicable: SQL is unsupported | Intel Xeon Platinum 8573C / `mongo:8` |

Jobs used different CPUs, so absolute timings cannot rank databases. Temporary `bench::` variables are excluded from Skript variable files to avoid a save-queue backlog. The benchmark still includes in-memory variable handling and ORM database operations.

**Total time** runs from the start of the statement until the trigger resumes, including variable handling, database work and scheduling waits. Input generation, later count queries, validation and cleanup are outside this window. **Maximum tick gap** is the largest interval between consecutive observer executions from statement start to one tick after it returns; it is not exclusive addon time on the server thread. **Tick excess** subtracts 50 ms from that gap, floored at zero. **Operation** and **Requested rows** identify the task and its size.

## 5000-row reads and writes across databases

These cases fill only `id`; the other five columns are NULL. Inputs and results use global variables. Each database handles the same data structure. Showing total time beside tick gaps helps distinguish a long wait from a delayed update. Each case has one sample, which cannot establish a stable performance difference.

| Backend | Operation | Requested rows | Total time | Maximum tick gap | Tick excess |
| --- | --- | ---: | ---: | ---: | ---: |
| SQLite | Write | 5000 | 738.019777 ms | 51.039040 ms | 1.039040 ms |
| SQLite | Read | 5000 | 101.214641 ms | 101.797048 ms | 51.797048 ms |
| MySQL | Write | 5000 | 653.115830 ms | 50.743193 ms | 0.743193 ms |
| MySQL | Read | 5000 | 99.480507 ms | 100.044641 ms | 50.044641 ms |
| MariaDB | Write | 5000 | 627.244008 ms | 50.787066 ms | 0.787066 ms |
| MariaDB | Read | 5000 | 75.822950 ms | 76.335655 ms | 26.335655 ms |
| PostgreSQL | Write | 5000 | 680.279059 ms | 50.846258 ms | 0.846258 ms |
| PostgreSQL | Read | 5000 | 106.822833 ms | 107.407271 ms | 57.407271 ms |
| MongoDB | Write | 5000 | 605.432622 ms | 50.700463 ms | 0.700463 ms |
| MongoDB | Read | 5000 | 95.601344 ms | 95.916557 ms | 45.916557 ms |

## Larger writes and repeated writes

These are the same id-only cases with global variables. **10,000-row write time / Maximum tick gap** describe the larger batch; **Repeated 5000-row write time / Maximum tick gap** describe another batch after the six size cases. Compare the repeated write with the first 5000-row write above, not with the 10,000-row batch. Repetition does not guarantee warmed caches.

| Backend | 10,000-row write time | Maximum tick gap | Repeated 5000-row write time | Repeated maximum tick gap |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 1416.792130 ms | 50.810946 ms | 786.039922 ms | 50.662504 ms |
| MySQL | 1208.194044 ms | 50.795271 ms | 676.070701 ms | 50.894886 ms |
| MariaDB | 1207.324490 ms | 50.689455 ms | 700.069976 ms | 50.463965 ms |
| PostgreSQL | 1294.077597 ms | 50.748823 ms | 654.585784 ms | 50.558487 ms |
| MongoDB | 1349.871031 ms | 50.522086 ms | 645.084583 ms | 50.512251 ms |

## Plugin statements and raw SQL

This comparison runs separately. Each path processes 5000 rows with only `id` filled, using global inputs and results. **Plugin write / read** time `insert many` and `select many`; **Raw SQL write / read** time `execute update` and `execute query`. All four columns show complete statement time. SQL strings are built before timing, whereas plugin writes include input preparation, so their ratio is not ORM mapping overhead. Comparing both paths within a database shows the difference in complete call costs.

**Not applicable (SQL is unsupported)** means MongoDB cannot execute these SQL statements. Its plugin cases still passed, and missing SQL results do not mean zero elapsed time. MySQL plugin writes use parameterized multi-row inserts; other JDBC paths use driver batching.

| Backend | Plugin write | Raw SQL write | Plugin read | Raw SQL read |
| --- | ---: | ---: | ---: | ---: |
| SQLite | 749.999370 ms | 50.010275 ms | 94.916647 ms | 75.878837 ms |
| MySQL | 750.014611 ms | 81.068476 ms | 109.862134 ms | 92.715736 ms |
| MariaDB | 699.836833 ms | 49.816977 ms | 81.606404 ms | 69.435926 ms |
| PostgreSQL | 750.100345 ms | 94.180613 ms | 85.262522 ms | 112.546390 ms |
| MongoDB | 699.951095 ms | Not applicable (SQL is unsupported) | 84.042793 ms | Not applicable (SQL is unsupported) |

## MariaDB: row counts and repeated cases

This expands the MariaDB results from the same run, on AMD EPYC 9V45 with `mariadb:11.4`. Rows still fill only `id`, with global inputs and results. Holding the database and data structure fixed while changing row counts shows how task size relates to waiting and tick gaps. Repeated 5000-row cases also show the effect of execution order.

**Result** states whether the operation succeeded. A 10,000-row read exceeds the limit: its time measures refusal and result clearing, not a successful 10,000-row read. “Repeated” means another execution after the size cases; it does not guarantee warmed caches.

| Operation | Requested rows | Total time | Maximum tick gap | Tick excess | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| Write | 100 | 100.166930 ms | 52.934261 ms | 2.934261 ms | Succeeded |
| Read | 100 | 51.661766 ms | 52.577681 ms | 2.577681 ms | Succeeded |
| Write | 500 | 49.721242 ms | 50.633752 ms | 0.633752 ms | Succeeded |
| Read | 500 | 55.148990 ms | 56.118816 ms | 6.118816 ms | Succeeded |
| Write | 1000 | 199.506323 ms | 54.222904 ms | 4.222904 ms | Succeeded |
| Read | 1000 | 56.909843 ms | 57.477091 ms | 7.477091 ms | Succeeded |
| Write | 2500 | 299.843110 ms | 50.377625 ms | 0.377625 ms | Succeeded |
| Read | 2500 | 70.805768 ms | 71.374238 ms | 21.374238 ms | Succeeded |
| Write | 5000 | 627.244008 ms | 50.787066 ms | 0.787066 ms | Succeeded |
| Read | 5000 | 75.822950 ms | 76.335655 ms | 26.335655 ms | Succeeded |
| Write | 10000 | 1207.324490 ms | 50.689455 ms | 0.689455 ms | Succeeded |
| Read | 10000 | 57.263183 ms | 57.794867 ms | 7.794867 ms | Refused: above the 5000-row read limit |
| Write (repeated) | 5000 | 700.069976 ms | 50.463965 ms | 0.463965 ms | Succeeded |
| Read (repeated) | 5000 | 83.557357 ms | 84.144273 ms | 34.144273 ms | Succeeded |

## Revised server benchmark

The additional cases fill all six columns and compare local inputs / local results with global inputs / global results. They also cover mixed scopes, replacement of existing results, items and locations. Inputs are generated before timing, every case gets a fresh primary-key range, and queries read only that range. Generation, validation, counting and cleanup are outside the timed windows.

Each additional case has three warmups and ten measured samples. Tables show the median of those ten samples; warmups are excluded. Each backend attachment contains 442 raw records and 34 summaries, including p95 and maximum values. With the nearest-rank method and ten measured samples, p95 equals the maximum; this provides limited evidence about rare delays. The id-only curve and SQL comparison have one sample each and are not included in these medians.

| Case | Rows | Purpose |
| --- | --- | --- |
| Six populated numeric columns: local and global scopes | 100, 500, 1000, 2500, 5000 | Hold database work fixed and compare input preparation and result storage |
| Local input / global result and global input / local result | 5000 | Observe input and result scope separately |
| Replace existing local or global results | 5000 | Include old-result clearing and check stale rows and columns are removed |
| Four numbers, one item and one location | 1000 | Measure ordinary values mixed with server-thread object conversions |
| Named item with lore and a location; replace local results | 1000 | Check object properties and result replacement |

### Measured results: Run #7

Each read returns 5000 rows with all six numeric columns populated. **Operation** identifies a local or global result. **Server-thread processing time** is the median sum of timed server-thread processing intervals for that operation. It is not a whole tick and does not cover every small statement overhead. **Total time** and **Maximum tick gap** each report a median across ten samples. **Tick excess** subtracts 50 ms from the displayed median gap, floored at zero; it does not describe the worst observed delay. Showing complete time beside server-thread processing reveals where the work goes. Scopes ran within the same server run and can be compared within a database.

| Backend | Operation | Requested rows | Total time | Maximum tick gap | Tick excess | Server-thread processing time |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| SQLite | Read into locals | 5000 | 49.925225 ms | 50.208298 ms | 0.208298 ms | 0.018713 ms |
| SQLite | Read into globals | 5000 | 65.414039 ms | 65.743510 ms | 15.743510 ms | 15.483043 ms |
| MySQL | Read into locals | 5000 | 49.923762 ms | 50.204954 ms | 0.204954 ms | 0.015720 ms |
| MySQL | Read into globals | 5000 | 86.510248 ms | 87.054996 ms | 37.054996 ms | 36.381696 ms |
| MariaDB | Read into locals | 5000 | 50.005949 ms | 50.250288 ms | 0.250288 ms | 0.017637 ms |
| MariaDB | Read into globals | 5000 | 66.299079 ms | 66.890665 ms | 16.890665 ms | 15.888968 ms |
| PostgreSQL | Read into locals | 5000 | 49.942947 ms | 50.217965 ms | 0.217965 ms | 0.018167 ms |
| PostgreSQL | Read into globals | 5000 | 69.211086 ms | 69.465648 ms | 19.465648 ms | 19.242586 ms |
| MongoDB | Read into locals | 5000 | 49.950872 ms | 50.178014 ms | 0.178014 ms | 0.014090 ms |
| MongoDB | Read into globals | 5000 | 71.939851 ms | 72.119047 ms | 22.119047 ms | 21.958078 ms |

The local path moves the main result-storage work into the background. Complete reads still take about one tick because Skript resumes on the server thread. This compares scopes in the current implementation, not plugin versions or other addons.

## Items and locations: work within each tick

Each of the 1000 rows contains four numbers, one item and one location. **Total time** and **Maximum tick gap** report medians across ten samples. **Tick excess** subtracts 50 ms from the displayed median gap, floored at zero. **Busiest-tick processing time** takes the largest sum of timed processing intervals within one tick for each operation, then reports the median of those ten maxima. It belongs to that operation, not the server’s complete MSPT. Showing it beside complete time exposes the tradeoff between less work in each tick and more time waiting for sliced conversion.

| Backend | Operation | Requested rows | Total time | Maximum tick gap | Tick excess | Busiest-tick processing time |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| SQLite | Read into locals | 1000 | 499.969523 ms | 52.032646 ms | 2.032646 ms | 1.947452 ms |
| SQLite | Read into globals | 1000 | 521.950934 ms | 112.811151 ms | 62.811151 ms | 64.061272 ms |
| MySQL | Read into locals | 1000 | 699.948048 ms | 52.012222 ms | 2.012222 ms | 1.960147 ms |
| MySQL | Read into globals | 1000 | 775.946264 ms | 175.476904 ms | 125.476904 ms | 126.033681 ms |
| MariaDB | Read into locals | 1000 | 449.839210 ms | 52.125599 ms | 2.125599 ms | 1.939399 ms |
| MariaDB | Read into globals | 1000 | 448.441535 ms | 95.115534 ms | 45.115534 ms | 45.820466 ms |
| PostgreSQL | Read into locals | 1000 | 599.943042 ms | 52.004495 ms | 2.004495 ms | 1.948226 ms |
| PostgreSQL | Read into globals | 1000 | 634.938087 ms | 134.294649 ms | 84.294649 ms | 84.902243 ms |
| MongoDB | Read into locals | 1000 | 499.969330 ms | 52.020147 ms | 2.020147 ms | 1.956798 ms |
| MongoDB | Read into globals | 1000 | 515.116861 ms | 114.311325 ms | 64.311325 ms | 65.141387 ms |

Local results use about 2 ms of timed work in the busiest tick, but the complete read waits across more ticks. Final global publication can still take tens of milliseconds and is outside the shared conversion budget.

## MariaDB: row counts with six populated columns

These cases use the same MariaDB job with all six numeric columns populated. **Operation** identifies the read or write and its scope; **Requested rows** gives its size. **Total time** and **Maximum tick gap** report medians across ten measured samples. **Tick excess** subtracts 50 ms from the displayed median gap, floored at zero. Changing size and scope within one environment shows their effect on waiting and tick gaps. Global writes include sliced input reads across ticks; a long total does not mean every tick was blocked. The data differs from the id-only cases, so subtracting those results would not measure an optimization gain.

| Operation | Requested rows | Total time | Maximum tick gap | Tick excess |
| --- | ---: | ---: | ---: | ---: |
| Local write | 100 | 50.002091 ms | 50.365996 ms | 0.365996 ms |
| Local read | 100 | 49.948891 ms | 50.296366 ms | 0.296366 ms |
| Global write | 100 | 99.999130 ms | 50.318539 ms | 0.318539 ms |
| Global read | 100 | 50.561922 ms | 50.852497 ms | 0.852497 ms |
| Local write | 500 | 50.003658 ms | 50.298183 ms | 0.298183 ms |
| Local read | 500 | 49.969411 ms | 50.278538 ms | 0.278538 ms |
| Global write | 500 | 200.041385 ms | 50.843437 ms | 0.843437 ms |
| Global read | 500 | 53.469929 ms | 53.751575 ms | 3.751575 ms |
| Local write | 1000 | 50.029800 ms | 50.227414 ms | 0.227414 ms |
| Local read | 1000 | 49.985885 ms | 50.250870 ms | 0.250870 ms |
| Global write | 1000 | 400.035662 ms | 50.880191 ms | 0.880191 ms |
| Global read | 1000 | 55.762635 ms | 55.953148 ms | 5.953148 ms |
| Local write | 2500 | 50.027120 ms | 50.249912 ms | 0.249912 ms |
| Local read | 2500 | 49.943913 ms | 50.207857 ms | 0.207857 ms |
| Global write | 2500 | 1000.028616 ms | 50.811210 ms | 0.811210 ms |
| Global read | 2500 | 58.051739 ms | 58.323916 ms | 8.323916 ms |
| Local write | 5000 | 50.068590 ms | 50.309630 ms | 0.309630 ms |
| Local read | 5000 | 50.005949 ms | 50.250288 ms | 0.250288 ms |
| Global write | 5000 | 1845.764052 ms | 50.874572 ms | 0.874572 ms |
| Global read | 5000 | 66.299079 ms | 66.890665 ms | 16.890665 ms |

## JVM microbenchmarks

JMH ran on AMD EPYC 7763 in the same Run #7, with two forks, two warmup iterations and three measured iterations per fork. **Case** names the work; **Time per operation** is JMH’s aggregate; **Measured work** defines its scope. These cases examine code and database calls without Paper, not complete statement time on a game server.

| Case | Time per operation | Measured work |
| --- | ---: | --- |
| 5000-row batch insert | 9.871089 ms/op | In-memory H2, value preparation, parameter binding and database execution |
| Rows-per-statement limit | 15.115187 ns/op | Limit calculation without database access |

[JMH records](https://github.com/heyhey123-git/skript-orm/blob/375c13b/dev/bench/amd-epyc-7763-64-core-processor/data.js)

## Phase timings

Scripts record complete statements and observer gaps. Addon phase instrumentation is enabled only on benchmark servers. The script captures it immediately after the statement, before later queries overwrite it. **Field** names the JSON key; **Meaning** defines its measurement boundary. Some phases overlap and must not all be added together.

| Field | Meaning |
| --- | --- |
| `wallNs` | Statement start to trigger resumption, excluding later validation |
| `gapNs` | Maximum observer gap from statement start to one tick after return |
| `mainNs` | Sum of timed server-thread processing intervals for this operation |
| `mainTickMaxNs` | Largest sum of those intervals within one tick for this operation |
| `prepareMainNs`, `prepareAsyncNs` | Input preparation recorded on the server thread and in the background |
| `conversionMainNs` | Server-thread value conversion time |
| `resultMainNs`, `resultAsyncNs` | Result storage recorded on the server thread and in the background |
| `executionNs` | Asynchronous query-task time; may include preparation, conversion waits, database operations and cursor reads, not pure database time |
| `queueWaitNs` | Time waiting for the server-thread conversion queue |
| `syncConversions` | Recorded server-thread conversion count |
| `largestConversionNs` | Time of the slowest individual server-thread conversion |

## Reports and reproduction

The run’s `tick-report-*` attachments contain `pipeline-results.json`, `tick-results.json` and environment records. The pipeline file keeps raw samples, including warmups, and median / p95 / maximum summaries. The tick file contains points for CI history. **Report** identifies the database; **Data source** links to this run’s published records for checking numbers, not ranking databases.

| Report | Data source |
| --- | --- |
| SQLite | [Nanosecond records](https://github.com/heyhey123-git/skript-orm/blob/375c13b/dev/bench/amd-epyc-9v74-80-core-processor/tick-ns/SQLite/data.js) |
| MySQL | [Nanosecond records](https://github.com/heyhey123-git/skript-orm/blob/375c13b/dev/bench/amd-epyc-7763-64-core-processor/tick-ns/MySQL/data.js) |
| MariaDB | [Nanosecond records](https://github.com/heyhey123-git/skript-orm/blob/375c13b/dev/bench/amd-epyc-9v45-96-core-processor/tick-ns/MariaDB/data.js) |
| PostgreSQL | [Nanosecond records](https://github.com/heyhey123-git/skript-orm/blob/375c13b/dev/bench/amd-epyc-9v74-80-core-processor/tick-ns/PostgreSQL/data.js) |
| MongoDB | [Nanosecond records](https://github.com/heyhey123-git/skript-orm/blob/375c13b/dev/bench/intel-xeon-platinum-8573c/tick-ns/MongoDB/data.js) |

Run `./gradlew serverBenchmark` locally for SQLite. Other backends use `-Pskriptorm.benchmark.server.type=<type>` and connection properties. Results are written under `build/benchmarks/`. Run `./gradlew :benchmarks:jmh` for JVM benchmarks; its output is `benchmarks/build/benchmarks/results.json`. Jobs record the commit, CPU, JDK, operating system, database image, driver, Paper and Skript versions.

CI fails for script errors, wrong affected or stored row counts, missing samples and other correctness problems. Timing changes are reported without failing the build. Benchmark history lives in `gh-pages` under `dev/bench/<CPU>/`; server results are also grouped by backend.

## Interpretation and remaining gaps

- Processing intervals record elapsed time, not CPU time. Not every small statement overhead is instrumented.
- Do not add `executionNs` to every other phase: it may already contain them.
- Maximum tick gaps can reveal delayed updates, but cannot provide a complete MSPT distribution. Allocation, peak memory and retained memory need separate measurements.
- An idle server does not represent many online players and active entities. Short runs cannot rule out long-term leaks.
- Generic JDBC can use drivers outside this test matrix; these scores do not describe those drivers.
- Version comparisons need the same script, data, machine and database settings. CI results from different CPUs cannot directly establish an optimization percentage.
