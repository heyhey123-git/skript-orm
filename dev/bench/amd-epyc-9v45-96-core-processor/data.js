window.BENCHMARK_DATA = {
  "lastUpdate": 1791107495805,
  "repoUrl": "https://github.com/heyhey123-git/skript-orm",
  "entries": {
    "Benchmark": [
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
          "id": "0f3bf2119f791ca9b2cab2dc163e918fa8b47215",
          "message": "fix(mysql): keep null values in multi-row inserts",
          "timestamp": "2026-10-03T08:58:41Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/0f3bf2119f791ca9b2cab2dc163e918fa8b47215"
        },
        "date": 1791018789788,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 4.527162177007761,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 11.162086117863344,
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
          "id": "0f3bf2119f791ca9b2cab2dc163e918fa8b47215",
          "message": "fix(mysql): keep null values in multi-row inserts",
          "timestamp": "2026-10-03T08:58:41Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/0f3bf2119f791ca9b2cab2dc163e918fa8b47215"
        },
        "date": 1791107495215,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 5.033868122022965,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 11.434647023790768,
            "unit": "ns/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}