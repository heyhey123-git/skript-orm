window.BENCHMARK_DATA = {
  "lastUpdate": 1791007673999,
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
        "date": 1791007673602,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 78850687,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 77418317,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 27418317,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 685494020,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 64970142,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 14970142,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 51431199,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 53362898,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 3362898,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 53585529,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 53323278,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 3323278,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 54680455,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 54556974,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 4556974,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 65127712,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 65100253,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 15100253,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 78761947,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 78579400,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 28579400,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 57773138,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 57570011,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 7570011,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 77584306,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 77418781,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 27418781,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 683646486,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50583169,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 583169,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 96438164,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 56046435,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 6046435,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 88708499,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 51428902,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 1428902,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 187602522,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 51383216,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 1383216,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 277177680,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 50634858,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 634858,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 717433104,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50968606,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 968606,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1196824500,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 53798503,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 3798503,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}