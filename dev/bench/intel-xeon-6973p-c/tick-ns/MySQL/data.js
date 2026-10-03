window.BENCHMARK_DATA = {
  "lastUpdate": 1791007668034,
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
        "date": 1791007667595,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 76245264,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 75149075,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 25149075,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 586972838,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 63473026,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 13473026,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 91635107,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 92240474,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 42240474,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 46793973,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 107120380,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 57120380,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 52405875,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 55647843,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 5647843,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 55251022,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 55070614,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 5070614,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 56301096,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 56190250,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 6190250,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 68286874,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 68204822,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 18204822,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 78302884,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 78218777,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 28218777,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 58942094,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 58812703,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 8812703,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 76869853,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 76763973,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 26763973,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 684881529,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50515912,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 515912,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 87280061,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 72639296,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 22639296,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 88872144,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 51272705,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 1272705,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 184717654,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 53576832,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 3576832,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 280281225,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 50776767,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 776767,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 462517747,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50395059,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 395059,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1398534310,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 51944607,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 1944607,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}