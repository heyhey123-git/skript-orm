window.BENCHMARK_DATA = {
  "lastUpdate": 1791107502587,
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
        "date": 1791007671616,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 99224999,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 98007636,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 48007636,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 778881589,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 71556573,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 21556573,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 102880893,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 100299662,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 50299662,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 61349564,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 97216221,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 47216221,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 54386344,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 63034092,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 13034092,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 57720824,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 57579690,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 7579690,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 64157196,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 63970275,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 13970275,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 79779481,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 79626694,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 29626694,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 94172914,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 94082624,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 44082624,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 64934252,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 64857418,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 14857418,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 98796136,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 98530949,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 48530949,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 623383051,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50885682,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 885682,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 137059479,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 80645436,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 30645436,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 167587580,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52022770,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2022770,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 163695350,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 51684685,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 1684685,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 230630977,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 73432820,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 23432820,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 585103286,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 66926809,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 16926809,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1255801227,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 95786030,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 45786030,
            "unit": "ns"
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
        "date": 1791107502358,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 97089974,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 96144669,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 46144669,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 730234089,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 70079616,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 20079616,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 108345419,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 108202111,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 58202111,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 66717406,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 91850890,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 41850890,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 59787748,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 56780416,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 6780416,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 59711440,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 59511465,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 9511465,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 60684558,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 60730995,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 10730995,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 78229702,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 78205215,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 28205215,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 95578494,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 95523109,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 45523109,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 63038175,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 62917929,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 12917929,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 91791957,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 91498475,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 41498475,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 622883176,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50678551,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 678551,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 84878319,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 77888485,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 27888485,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 125822450,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 51829446,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 1829446,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 113900903,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50390211,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 390211,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 296350528,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 56857023,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 6857023,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 582113958,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 70045718,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 20045718,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1283465210,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 118848339,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 68848339,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}