window.BENCHMARK_DATA = {
  "lastUpdate": 1790872409749,
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
          "id": "d0d86a66cfd15437b6ba3123b383f9d8887cf0f2",
          "message": "Add the pool stress case the last commit described\n\nThis is the integration test the previous commit's message describes and did not include: sixty-four statements over a smaller pool, each returning its own row, with the pool required to be at exactly zero active connections afterwards and the database still answering. It fails when no endpoint is configured rather than being skipped, so a machine without Docker reports a failure instead of a silent pass.\n\nCommitted on its own because my path list for that commit missed it. The file is here rather than folded into the commit whose message already describes it, because a commit that has been described should not change underneath its description.",
          "timestamp": "2026-10-01T16:10:13Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/d0d86a66cfd15437b6ba3123b383f9d8887cf0f2"
        },
        "date": 1790872409045,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.InsertManyBenchmark.insertManyOf5000",
            "value": 8.022279333105631,
            "unit": "ms/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "io.github.heyhey123.skriptorm.benchmarks.RowLimitBenchmark.rowsPerStatement",
            "value": 16.15484359752377,
            "unit": "ns/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}