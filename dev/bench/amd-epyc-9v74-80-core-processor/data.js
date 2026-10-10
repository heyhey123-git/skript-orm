window.BENCHMARK_DATA = {
  "lastUpdate": 1791626193364,
  "repoUrl": "https://github.com/heyhey123-git/skript-orm",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "name": "heyhey123-git",
            "username": "heyhey123-git",
            "email": "156066831+heyhey123-git@users.noreply.github.com"
          },
          "committer": {
            "name": "heyhey123-git",
            "username": "heyhey123-git",
            "email": "156066831+heyhey123-git@users.noreply.github.com"
          },
          "id": "949945cbf10251b4eea1aa432cfb7040bb299338",
          "message": "docs(benchmarking): say what the warm rows are\n\nThe tick table's read and write rows are each that statement's first run in the server it was measured on, and the two warm rows repeat the 5000-row write and the 5000-row read at the very end, once the whole curve has already run. 5000 is the size the write budget and the read ceiling both land on.\n\nDoing that size twice is what separates a first pass from a steady state: on this runner the first 5000-row write overran a tick by 90 milliseconds where the warm one overran it not at all. The warm rows report under names of their own and are never averaged into the curve, which the script says in its own comment.\n\nThe pages now define the vocabulary of the table where the numbers are, rather than leaving a reader to infer it from column headings.",
          "timestamp": "2026-10-02T04:07:52Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/949945cbf10251b4eea1aa432cfb7040bb299338"
        },
        "date": 1790934600513,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 6.362828045406946,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 13.375057356591944,
            "unit": "ns/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Radiation-pi",
            "username": "Radiation-pi",
            "email": "pi1243039811@outlook.com"
          },
          "committer": {
            "name": "Radiation-pi",
            "username": "Radiation-pi",
            "email": "pi1243039811@outlook.com"
          },
          "id": "857a0a67879ae82658e858269eb24be8b9c7f1b6",
          "message": "chore(release): prepare version 1.4.2",
          "timestamp": "2026-10-04T15:44:43Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/857a0a67879ae82658e858269eb24be8b9c7f1b6"
        },
        "date": 1791196334973,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 9.826853289265452,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 17.240864676202715,
            "unit": "ns/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "heyhey123-git",
            "username": "heyhey123-git",
            "email": "156066831+heyhey123-git@users.noreply.github.com"
          },
          "committer": {
            "name": "heyhey123-git",
            "username": "heyhey123-git",
            "email": "156066831+heyhey123-git@users.noreply.github.com"
          },
          "id": "df4fc19097f0ec98c18da565e2e0412527b71f49",
          "message": "chore(benchmarks): remove obsolete local baseline files",
          "timestamp": "2026-10-07T08:12:11Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/df4fc19097f0ec98c18da565e2e0412527b71f49"
        },
        "date": 1791626191956,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 7.043608661499889,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 13.369540544223186,
            "unit": "ns/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}