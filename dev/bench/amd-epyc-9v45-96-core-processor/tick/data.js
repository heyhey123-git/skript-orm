window.BENCHMARK_DATA = {
  "lastUpdate": 1790913452106,
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
          "id": "51aa7602f141a9ce4062eaa28e1dff9cbb2e76a2",
          "message": "test(mongo): measure the rows MongoDB holds, not its commands\n\nThe case read serverStatus opcounters, and those counters are global to the mongod, so it first had to prove that no other client wrote inside the window before the number could be read as its own work. In a shared test JVM that proof cannot hold: other test classes in the same worker keep connection pools open, so connections.current never falls to this test's own two and the guard fires every run. CI showed exactly that - the job was green while the case aborted as unattributable, which means MongoDB had no number at all rather than a wrong one.\n\nRows need no guard. The collection's own document count is per-collection by construction, so the server is answering about this test's work and nothing else, and the count is taken through the verification client rather than the implementation, so it is MongoDB's answer about its own store rather than the plugin agreeing with itself. The opcounters sampling, the empty control block, the delta and the attribution guard are gone, and the fingerprint now reads buildInfo instead of serverStatus so that no global counter appears in the test at all. The assertion is unchanged in spirit: every row the plugin was handed must be in the collection, while the shape of the command the driver chose is neither reported nor asserted, because that belongs to the driver.",
          "timestamp": "2026-10-02T03:49:04Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/51aa7602f141a9ce4062eaa28e1dff9cbb2e76a2"
        },
        "date": 1790913451190,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "read 100rows wall",
            "value": 60,
            "unit": "ms"
          },
          {
            "name": "read 100rows overrun",
            "value": 10,
            "unit": "ms"
          },
          {
            "name": "read 500rows wall",
            "value": 60,
            "unit": "ms"
          },
          {
            "name": "read 1000rows wall",
            "value": 60,
            "unit": "ms"
          },
          {
            "name": "read 2500rows wall",
            "value": 100,
            "unit": "ms"
          },
          {
            "name": "read 5000rows wall",
            "value": 90,
            "unit": "ms"
          },
          {
            "name": "read 5000rows overrun",
            "value": 0,
            "unit": "ms"
          },
          {
            "name": "read 10000rows wall",
            "value": 60,
            "unit": "ms"
          },
          {
            "name": "read 10000rows overrun",
            "value": 0,
            "unit": "ms"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 70,
            "unit": "ms"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 0,
            "unit": "ms"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 190,
            "unit": "ms"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 0,
            "unit": "ms"
          },
          {
            "name": "write 100rows wall",
            "value": 90,
            "unit": "ms"
          },
          {
            "name": "write 100rows overrun",
            "value": 10,
            "unit": "ms"
          },
          {
            "name": "write 500rows wall",
            "value": 30,
            "unit": "ms"
          },
          {
            "name": "write 500rows overrun",
            "value": 20,
            "unit": "ms"
          },
          {
            "name": "write 1000rows wall",
            "value": 80,
            "unit": "ms"
          },
          {
            "name": "write 1000rows overrun",
            "value": 30,
            "unit": "ms"
          },
          {
            "name": "write 2500rows wall",
            "value": 70,
            "unit": "ms"
          },
          {
            "name": "write 2500rows overrun",
            "value": 30,
            "unit": "ms"
          },
          {
            "name": "write 5000rows wall",
            "value": 220,
            "unit": "ms"
          },
          {
            "name": "write 5000rows overrun",
            "value": 90,
            "unit": "ms"
          },
          {
            "name": "write 10000rows wall",
            "value": 320,
            "unit": "ms"
          },
          {
            "name": "write 10000rows overrun",
            "value": 100,
            "unit": "ms"
          }
        ]
      }
    ]
  }
}