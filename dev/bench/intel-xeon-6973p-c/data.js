window.BENCHMARK_DATA = {
  "lastUpdate": 1793180790911,
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
          "id": "df4fc19097f0ec98c18da565e2e0412527b71f49",
          "message": "chore(benchmarks): remove obsolete local baseline files",
          "timestamp": "2026-10-07T08:12:11Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/df4fc19097f0ec98c18da565e2e0412527b71f49"
        },
        "date": 1793180790256,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 7.839842154390575,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 12.001011165555889,
            "unit": "ns/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}