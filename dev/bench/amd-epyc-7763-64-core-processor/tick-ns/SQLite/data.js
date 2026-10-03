window.BENCHMARK_DATA = {
  "lastUpdate": 1791007665904,
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
          "id": "6476c9ba73b156f030f675a9cca1b530f4323aa5",
          "message": "perf(benchmarks): record nanosecond timings across database backends",
          "timestamp": "2026-10-03T05:32:19Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/6476c9ba73b156f030f675a9cca1b530f4323aa5"
        },
        "date": 1791007665464,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 112968835,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 111960953,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 61960953,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 727519482,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 72818038,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 22818038,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 104414372,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 101809983,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 51809983,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 17148948,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 74953135,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 24953135,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 58004882,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 54994133,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 4994133,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 59896233,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 59676100,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 9676100,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 67549943,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 67474913,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 17474913,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 80379548,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 80329405,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 30329405,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 103186998,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 103066121,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 53066121,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 61525218,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 61378362,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 11378362,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 90776084,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 90641071,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 40641071,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 779342861,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50628368,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 628368,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 88589291,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 65956918,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 15956918,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 177222592,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52232844,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2232844,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 125425940,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 54194572,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 4194572,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 289938740,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 62923515,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 12923515,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 735828866,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 65767643,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 15767643,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1501908676,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 94670715,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 44670715,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}