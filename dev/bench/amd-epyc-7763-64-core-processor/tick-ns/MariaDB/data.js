window.BENCHMARK_DATA = {
  "lastUpdate": 1791007670029,
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
        "date": 1791007669629,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 91608649,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 90582464,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 40582464,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 628867037,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 71377285,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 21377285,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 89937610,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 89914688,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 39914688,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 74916475,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 80306922,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 30306922,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 58978086,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 54904413,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 4904413,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 59719091,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 59571377,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 9571377,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 59743000,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 59761403,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 9761403,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 79123322,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 79081254,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 29081254,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 95789059,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 95813175,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 45813175,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 59551810,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 59517446,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 9517446,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 82868479,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 82686620,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 32686620,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 675078706,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50746686,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 746686,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 183019540,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 77813136,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 27813136,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 125585622,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 50931863,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 931863,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 122445042,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50273827,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 273827,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 139899172,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 64122445,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 14122445,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 685122311,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 66066614,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 16066614,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1149905662,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 101135769,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 51135769,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}