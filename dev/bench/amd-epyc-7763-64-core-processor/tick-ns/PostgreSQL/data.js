window.BENCHMARK_DATA = {
  "lastUpdate": 1791888140213,
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
        "date": 1791456103443,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 81331333,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 81547475,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 31547475,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 799986082,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50408427,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 408427,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 121627568,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 122173012,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 72173012,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 46015193,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 49681916,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 53881108,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 54867212,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 4867212,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 61549351,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 62540043,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 12540043,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 56624761,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 57285460,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 7285460,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 88186135,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 88932723,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 38932723,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 107602956,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 108621890,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 58621890,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 62423086,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 63323921,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 13323921,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 83047586,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 83568514,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 33568514,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 644081646,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50517972,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 517972,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 200113285,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 53009334,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 3009334,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 149335100,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 51733804,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 1733804,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 267573359,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 52165267,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 2165267,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 367019287,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51599053,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1599053,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 658484536,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50913257,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 913257,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1197907130,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 59040994,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 9040994,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49947954,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50220652,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 6968,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 6968,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 230343,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 6968,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 2474807,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 wallNs median",
            "value": 49942220,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50206451,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 9197,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 9197,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultMainNs median",
            "value": 9197,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 544206,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 756278,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 wallNs median",
            "value": 99968450,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50290095,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 215551,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 206614,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 206614,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultMainNs median",
            "value": 8490,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 2474376,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 wallNs median",
            "value": 50772688,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 50997121,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 825891,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 825034,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultMainNs median",
            "value": 825891,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2109,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 748381,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 wallNs median",
            "value": 49987473,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50256898,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 7209,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 7209,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 916414,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 7209,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 5750818,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 wallNs median",
            "value": 49968389,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50211835,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 8130,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 8130,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultMainNs median",
            "value": 8130,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2855170,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1284743,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 wallNs median",
            "value": 200010824,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50788777,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 956063,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 733013,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 948439,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultMainNs median",
            "value": 7785,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 5024780,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 wallNs median",
            "value": 54064702,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 54216930,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 4071743,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 4070821,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultMainNs median",
            "value": 4071743,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 2715,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1273141,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 wallNs median",
            "value": 49979239,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50202609,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 8205,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 8205,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1708672,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 8205,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 10895168,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 wallNs median",
            "value": 49943968,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50159542,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 7934,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 7934,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultMainNs median",
            "value": 7934,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 4915201,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 2107752,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 wallNs median",
            "value": 400013999,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50833856,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2100032,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 773072,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2090949,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultMainNs median",
            "value": 10465,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 9987409,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 wallNs median",
            "value": 57960706,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 58157132,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 8005854,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 8005163,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultMainNs median",
            "value": 8005854,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 2900,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 2069814,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 wallNs median",
            "value": 49970615,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50213401,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 11250,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 11250,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 4056279,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 11250,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 25777215,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 wallNs median",
            "value": 49955593,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50206709,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 9357,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 9357,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultMainNs median",
            "value": 9357,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 12145349,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 4076127,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 wallNs median",
            "value": 981184711,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50866230,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 5152612,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 811724,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 5140945,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultMainNs median",
            "value": 12042,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 22944059,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 wallNs median",
            "value": 61745925,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 62006605,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 11833634,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 11831966,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultMainNs median",
            "value": 11833634,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 8529148,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 4554375,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 wallNs median",
            "value": 77320978,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50210267,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 12047,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 12047,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 7237189,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 12047,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 49365051,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 wallNs median",
            "value": 49961298,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50190865,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 11156,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 11156,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultMainNs median",
            "value": 11156,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 25969330,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 7685392,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 wallNs median",
            "value": 1800377656,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50967540,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 12255070,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 911685,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 12243584,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultMainNs median",
            "value": 12428,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 42909097,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 wallNs median",
            "value": 64882860,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 65051464,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 14872126,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 14870759,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultMainNs median",
            "value": 14872126,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 18143500,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 8322394,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 wallNs median",
            "value": 80744264,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50151954,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 12774,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 12774,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 6720496,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 12774,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 49348425,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 wallNs median",
            "value": 49990182,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50222884,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 5119,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 5119,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultMainNs median",
            "value": 5119,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 27548698,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7972042,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 wallNs median",
            "value": 1809804326,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50995431,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 12747882,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 942312,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 12730119,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultMainNs median",
            "value": 13054,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 42767095,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 wallNs median",
            "value": 83660347,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 72056575,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 27368192,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 27367175,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultMainNs median",
            "value": 27368192,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 18106353,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 8108101,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 wallNs median",
            "value": 75333895,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50193509,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 12799,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 12799,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 8357592,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 12799,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 51077553,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 wallNs median",
            "value": 65944819,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 66142205,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 16566414,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 16563144,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultMainNs median",
            "value": 16566414,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 18155388,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7988234,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 wallNs median",
            "value": 1804922954,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50995717,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 12693901,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 970252,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 12674565,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultMainNs median",
            "value": 17498,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 42469124,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 wallNs median",
            "value": 49969674,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50200378,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 3652,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 3652,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultMainNs median",
            "value": 3652,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 26359320,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 8432261,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 wallNs median",
            "value": 450007827,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51260480,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 12344804,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1922768,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 2159883,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 12333463,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 12197,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 412344252,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 384079180,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 65572,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 649946725,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 51969219,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 22987145,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1958897,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 conversionMainNs median",
            "value": 22983002,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 2780,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2975940,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 3358534,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 571540219,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 97476,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 659241986,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51783549,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 14473583,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1921201,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2074284,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 12244973,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 12438,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 363151826,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 336850000,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 66438,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 699633936,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 164017247,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 135219748,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 115795361,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 conversionMainNs median",
            "value": 20113297,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 115795997,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 9502,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 3217232,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 526188636,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 113621,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 636872495,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51269248,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 20152908,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1961791,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 2146501,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 20133597,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 12233,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 599883624,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 562794356,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 154221,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 875000170,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 51990610,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 31711807,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1974437,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 conversionMainNs median",
            "value": 31708686,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 2905,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3574122,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 3423716,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 783650418,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 258168,
            "unit": "ns"
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
        "date": 1791626201882,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 109773067,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 110345177,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 60345177,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 800056115,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50488586,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 488586,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 89308532,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 89539362,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 39539362,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 82106238,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50268266,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 268266,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 53765810,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 54960401,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 4960401,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 62303451,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 63186590,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 13186590,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 61409233,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 62109521,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 12109521,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 91289120,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 91903256,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 41903256,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 117497821,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 118018603,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 68018603,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 63526685,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 64792636,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 14792636,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 116775772,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 117356387,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 67356387,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 724006120,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50765052,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 765052,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 200124076,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 53469284,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 3469284,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 149879127,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52467575,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2467575,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 183846835,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50585228,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 585228,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 417880833,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 50931878,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 931878,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 722430354,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50887636,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 887636,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1306822313,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50801605,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 801605,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49987095,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50359674,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 10014,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 10014,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 191232,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 10014,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 2128745,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 wallNs median",
            "value": 49921027,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50216036,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 13054,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 13054,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultMainNs median",
            "value": 13054,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 544066,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 787169,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 wallNs median",
            "value": 99958509,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50264572,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 198681,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 186533,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 186533,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultMainNs median",
            "value": 9818,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 2051736,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 wallNs median",
            "value": 51516499,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 51846231,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 1601716,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 1599852,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultMainNs median",
            "value": 1601716,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2054,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 798735,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 wallNs median",
            "value": 49963928,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50204711,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 7905,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 7905,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 798691,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 7905,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 5682873,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 wallNs median",
            "value": 49949224,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50170677,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 9688,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 9688,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultMainNs median",
            "value": 9688,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2826318,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1225331,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 wallNs median",
            "value": 199984881,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50608105,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 721768,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 542803,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 712700,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultMainNs median",
            "value": 9052,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 5026111,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 wallNs median",
            "value": 54666989,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 54837988,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 4711483,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 4710581,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultMainNs median",
            "value": 4711483,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3066,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1236567,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 wallNs median",
            "value": 49993701,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50304228,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 13700,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 13700,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1581267,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 13700,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 10783808,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 wallNs median",
            "value": 49931847,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50196230,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 14682,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 14682,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultMainNs median",
            "value": 14682,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 4983506,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 2007883,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 wallNs median",
            "value": 400011764,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50892883,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2447800,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 816049,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2433353,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultMainNs median",
            "value": 14181,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 9561829,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 wallNs median",
            "value": 57851484,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 58126936,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 7881953,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 7879970,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultMainNs median",
            "value": 7881953,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3100,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 2065185,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 wallNs median",
            "value": 49978639,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50279195,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 15989,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 15989,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 3971286,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 15989,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 26776531,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 wallNs median",
            "value": 49970507,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50234546,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 10128,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 10128,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultMainNs median",
            "value": 10128,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 12783777,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 4469884,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 wallNs median",
            "value": 985282884,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50900910,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 6080999,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 840960,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 6067914,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultMainNs median",
            "value": 14332,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 22652795,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 wallNs median",
            "value": 60112970,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 60429682,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 10287771,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 10286128,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultMainNs median",
            "value": 10287771,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 8608801,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 4571871,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 wallNs median",
            "value": 76561675,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50189392,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 18505,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 18505,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 9375923,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 18505,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 51288247,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 wallNs median",
            "value": 49944384,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50246484,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 7519,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 7519,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultMainNs median",
            "value": 7519,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 27463419,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 8133447,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 wallNs median",
            "value": 1804758065,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50990418,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 12302515,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 880669,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 12285293,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultMainNs median",
            "value": 17943,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 42951921,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 wallNs median",
            "value": 63495198,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 63766726,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 13520077,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 13519135,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultMainNs median",
            "value": 13520077,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 18202501,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 8183368,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 wallNs median",
            "value": 77388201,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50279661,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 20478,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 20478,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 9468532,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 20478,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 51610517,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 wallNs median",
            "value": 49970225,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50266107,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 6006,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 6006,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultMainNs median",
            "value": 6006,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 30783050,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7697125,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 wallNs median",
            "value": 1785170524,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 51083175,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 11664001,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 893799,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 11643217,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultMainNs median",
            "value": 23504,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 42743368,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 wallNs median",
            "value": 69124450,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 69319705,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 23750806,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 23749519,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultMainNs median",
            "value": 23750806,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 18202229,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 7698748,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 wallNs median",
            "value": 72076373,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50236243,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 18128,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 18128,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 9746667,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 18128,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 52279641,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 wallNs median",
            "value": 67953685,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 68226599,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 18092793,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 18091605,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultMainNs median",
            "value": 18092793,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 18402090,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7793601,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 wallNs median",
            "value": 1808596103,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 51022027,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 12902071,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 893203,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 12884583,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultMainNs median",
            "value": 18484,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 42533756,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 wallNs median",
            "value": 49900511,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50217601,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 6307,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 6307,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultMainNs median",
            "value": 6307,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 27281164,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 7802602,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 wallNs median",
            "value": 447479323,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51071192,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 12073647,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1918538,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1794904,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 12050839,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 19436,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 409758127,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 381397538,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 71488,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 650616818,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 51946210,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 23978729,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1960323,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 conversionMainNs median",
            "value": 23972211,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 6673,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 3110523,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 3281407,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 618711482,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 120318,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 623689920,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51585390,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 14200670,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1916152,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2840180,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 11366455,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 19130,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 314539809,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 288481256,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 66284,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 688811903,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 142063191,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 113214692,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 92768990,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 conversionMainNs median",
            "value": 20378944,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 92770183,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 6707,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 3460080,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 525653290,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 119727,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 614896023,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51046281,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 19578058,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1956812,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1801231,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 19559073,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 17873,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 582004949,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 542534168,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 183121,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 875635422,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 51991294,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 32519318,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1972877,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 conversionMainNs median",
            "value": 32513041,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 6261,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3961653,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 3446937,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 810698410,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 158120,
            "unit": "ns"
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
        "date": 1791888139956,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 81121739,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 81370085,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 31370085,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 800010935,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50513538,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 513538,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 92884188,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 93093372,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 43093372,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 93448049,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50220687,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 220687,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 53413564,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 54632626,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 4632626,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 67635316,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 68734392,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 18734392,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 61790985,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 62622148,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 12622148,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 94671432,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 95540777,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 45540777,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 86181582,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 86742988,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 36742988,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 62433819,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 62959308,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 12959308,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 92958737,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 93572190,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 43572190,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 706065037,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50933925,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 933925,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 199980840,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 53622387,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 3622387,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 98313411,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 50659717,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 659717,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 171483820,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50677493,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 677493,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 384004740,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51619134,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1619134,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 698811282,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50747876,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 747876,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1287999489,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50805694,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 805694,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49912633,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50382658,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 16185,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 16185,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 190658,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 16185,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 2395790,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 wallNs median",
            "value": 49942431,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50350307,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 15463,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 15463,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultMainNs median",
            "value": 15463,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 521355,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 960931,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 wallNs median",
            "value": 99933357,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50373481,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 271256,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 256022,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 256022,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultMainNs median",
            "value": 16135,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 2371420,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 wallNs median",
            "value": 51553054,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 51947025,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 1608745,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 1606731,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultMainNs median",
            "value": 1608745,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2424,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 882865,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 wallNs median",
            "value": 49944963,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50291212,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 16836,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 16836,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 898609,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 16836,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 6029488,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 wallNs median",
            "value": 49927851,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50342244,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 15840,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 15840,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultMainNs median",
            "value": 15840,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2854597,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1450927,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 wallNs median",
            "value": 200005364,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50951068,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1350544,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 845059,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1332470,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultMainNs median",
            "value": 17036,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 5599294,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 wallNs median",
            "value": 53963203,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 54303442,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 3969649,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 3967515,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultMainNs median",
            "value": 3969649,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3416,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1518121,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 wallNs median",
            "value": 49995863,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50348594,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 15149,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 15149,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1662150,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 15149,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 11148429,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 wallNs median",
            "value": 49895212,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50254266,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 15523,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 15523,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultMainNs median",
            "value": 15523,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 5016343,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 2162638,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 wallNs median",
            "value": 400040208,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 51011815,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2703012,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 914695,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2689026,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultMainNs median",
            "value": 17277,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 9855669,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 wallNs median",
            "value": 57867633,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 58240311,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 7940321,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 7938513,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultMainNs median",
            "value": 7940321,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3601,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 2287283,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 wallNs median",
            "value": 49977477,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50299707,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 19256,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 19256,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 4777179,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 19256,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 28191736,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 wallNs median",
            "value": 49954263,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50276925,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 18043,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 18043,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultMainNs median",
            "value": 18043,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 12438456,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 4250810,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 wallNs median",
            "value": 970713865,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50987186,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 6666226,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 921273,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 6646038,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultMainNs median",
            "value": 20919,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 24089189,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 wallNs median",
            "value": 60857565,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 61200124,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 10895825,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 10893396,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultMainNs median",
            "value": 10895825,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 8640895,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 4593799,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 wallNs median",
            "value": 73232299,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50312715,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 20714,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 20714,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 9144917,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 20714,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 52312974,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 wallNs median",
            "value": 49958955,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50238146,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 17022,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 17022,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultMainNs median",
            "value": 17022,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 26539996,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 7579439,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 wallNs median",
            "value": 1800845347,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 51078316,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 13209775,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 1030939,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 13191024,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultMainNs median",
            "value": 18495,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 43742469,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 wallNs median",
            "value": 63886856,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 64183319,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 13958957,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 13956688,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultMainNs median",
            "value": 13958957,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 18501388,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 7863074,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 wallNs median",
            "value": 74633271,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50249097,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 17913,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 17913,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 10052509,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 17913,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 51693292,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 wallNs median",
            "value": 49997653,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50269909,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 6072,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 6072,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultMainNs median",
            "value": 6072,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 29810168,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7972782,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 wallNs median",
            "value": 1801446379,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 51061948,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 12216281,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 939086,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 12192862,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultMainNs median",
            "value": 18074,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 44732925,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 wallNs median",
            "value": 71923095,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 71553355,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 28079769,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 28078572,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultMainNs median",
            "value": 28079769,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 18599315,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 7849863,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 wallNs median",
            "value": 71151747,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50232496,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 18114,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 18114,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 10145354,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 18114,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 53225780,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 wallNs median",
            "value": 62852503,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 63060519,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 12887795,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 12886512,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultMainNs median",
            "value": 12887795,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 18591868,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7966045,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 wallNs median",
            "value": 1801653297,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50952237,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 13007708,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 954746,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 12973519,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultMainNs median",
            "value": 19427,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 43823832,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 wallNs median",
            "value": 49984200,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50210360,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 6562,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 6562,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultMainNs median",
            "value": 6562,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 26786815,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 7806976,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 queueWaitNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 largestConversionNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 wallNs median",
            "value": 449978178,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51122284,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 12254327,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1919642,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1930322,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 12237310,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 18339,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 412813590,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 382954004,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 102467,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 649910866,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 51962961,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 23161255,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1965728,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 conversionMainNs median",
            "value": 23154331,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 6622,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 3035751,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 3382087,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 571256249,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 154400,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 614908393,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51567254,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 14545756,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1917449,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2958881,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 11539621,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 18705,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 346353075,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 312180640,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 93836,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 700820122,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 161371394,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 132022514,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 111878374,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 conversionMainNs median",
            "value": 20267685,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 111879651,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 6416,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 3565069,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 525713545,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 138861,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 591279218,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51222481,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 19264292,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1953028,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1481120,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 19231100,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 18013,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 555393726,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 519911170,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 197767,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 899930261,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 51972934,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 32818255,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1975055,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 conversionMainNs median",
            "value": 32811983,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 6271,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3946232,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 4034764,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 810289785,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 167063,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}