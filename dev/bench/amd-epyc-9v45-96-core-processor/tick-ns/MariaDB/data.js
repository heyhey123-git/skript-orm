window.BENCHMARK_DATA = {
  "lastUpdate": 1793439950767,
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
          "id": "76dcc7e09a5fe7e97073e3d9a5b80d06d8e80b2d",
          "message": "perf(benchmarks): compare variable scopes and report processing phases",
          "timestamp": "2026-10-04T13:19:17Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/76dcc7e09a5fe7e97073e3d9a5b80d06d8e80b2d"
        },
        "date": 1791120626999,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 81606404,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 81905146,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 31905146,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 699836833,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50470375,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 470375,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 69435926,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 69856122,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 19856122,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 49816977,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50261429,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 261429,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 51661766,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 52577681,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 2577681,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 55148990,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 56118816,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 6118816,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 56909843,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 57477091,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 7477091,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 70805768,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 71374238,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 21374238,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 75822950,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 76335655,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 26335655,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 57263183,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 57794867,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 7794867,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 83557357,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 84144273,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 34144273,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 700069976,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50463965,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 463965,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 100166930,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 52934261,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 2934261,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 49721242,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 50633752,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 633752,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 199506323,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 54222904,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 4222904,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 299843110,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 50377625,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 377625,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 627244008,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50787066,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 787066,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1207324490,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50689455,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 689455,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 50002091,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50365996,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 18022,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 18022,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 172951,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 18022,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 1955376,
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
            "value": 49948891,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50296366,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 15974,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 15974,
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
            "value": 15974,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 297995,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 1001003,
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
            "value": 99999130,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50318539,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 201850,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 184879,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 184879,
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
            "value": 16415,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 1783852,
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
            "value": 50561922,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 50852497,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 632436,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 629371,
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
            "value": 632436,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2489,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 987043,
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
            "value": 50003658,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50298183,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 19023,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 19023,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 813200,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 19023,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 3223713,
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
            "value": 49969411,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50278538,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 16239,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 16239,
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
            "value": 16239,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 1560134,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1273884,
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
            "value": 200041385,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50843437,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1192903,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 745663,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1175282,
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
            "value": 17141,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 2056624,
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
            "value": 53469929,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 53751575,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 3491049,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 3488155,
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
            "value": 3491049,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3445,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1296380,
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
            "value": 50029800,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50227414,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 11452,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 11452,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1491114,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 11452,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 4058635,
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
            "value": 49985885,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50250870,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 16275,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 16275,
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
            "value": 16275,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 2679742,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1712248,
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
            "value": 400035662,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50880191,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2434049,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 777906,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2417409,
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
            "value": 15999,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 2638815,
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
            "value": 55762635,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 55953148,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 5717651,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 5715724,
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
            "value": 5717651,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3545,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1484943,
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
            "value": 50027120,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50249912,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 17611,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 17611,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 3828394,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 17611,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 8383878,
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
            "value": 49943913,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50207857,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 16680,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 16680,
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
            "value": 16680,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 7311007,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3559988,
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
            "value": 1000028616,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50811210,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 5772790,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 761261,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 5755769,
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
            "value": 19154,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 4768131,
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
            "value": 58051739,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 58323916,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 8028482,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 8025337,
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
            "value": 8028482,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 4825036,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3605137,
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
            "value": 50068590,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50309630,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 22284,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 22284,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 7756324,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 22284,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 15882428,
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
            "value": 50005949,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50250288,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 17637,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 17637,
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
            "value": 17637,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 15140212,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 6702987,
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
            "value": 1845764052,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50874572,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 11410188,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 762919,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 11394570,
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
            "value": 16755,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 8414249,
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
            "value": 66299079,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 66890665,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 15888968,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 15886059,
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
            "value": 15888968,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 10001375,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 6411231,
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
            "value": 50072187,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50283326,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 21307,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 21307,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 7203904,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 21307,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 15988119,
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
            "value": 50027425,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50227423,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 8442,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 8442,
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
            "value": 8442,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 19280541,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 6402383,
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
            "value": 1863856266,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50884176,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 11827893,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 783961,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 11785674,
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
            "value": 21377,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 8612990,
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
            "value": 71063411,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 71246117,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 24290609,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 24288766,
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
            "value": 24290609,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 10047761,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 6418674,
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
            "value": 49324118,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 49998281,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 18067,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 18067,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 6565569,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 18067,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 15149020,
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
            "value": 63154224,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 63384382,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 13178581,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 13176808,
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
            "value": 13178581,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 9696834,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 6410793,
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
            "value": 1856924943,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50901808,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 11681658,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 788643,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 11664437,
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
            "value": 18117,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 8458212,
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
            "value": 49986675,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50185378,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 8542,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 8542,
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
            "value": 8542,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 14530841,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 6365371,
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
            "value": 300373585,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51469834,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 7269278,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1895149,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1684361,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 7231556,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 55058,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 260136203,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 241074639,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 142991,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 449839210,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 52125599,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 14436414,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1939399,
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
            "value": 14428327,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 9008,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2325466,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 3568955,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 378501256,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 212416,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 584330456,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51674712,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 9499585,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1890912,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2480922,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 6931363,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 51748,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 210949851,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 193087297,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 148485,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 448441535,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 95115534,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 59773574,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 45820466,
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
            "value": 12616426,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 45822263,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 10425,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 3536863,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 332721337,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 206247,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 400388850,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51490357,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 10406546,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1932433,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1738764,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 10350036,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 52990,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 359498517,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 336960754,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 210643,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 548371487,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 52015857,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 18625459,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1956401,
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
            "value": 18616140,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 11762,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3844850,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 4016885,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 470550955,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 386384,
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
        "date": 1793091942810,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 72536849,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 72861135,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 22861135,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 699953376,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50530314,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 530314,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 85098511,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 85405861,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 35405861,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 50134245,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50460414,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 460414,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 52007668,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 52829791,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 2829791,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 54865786,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 55547599,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 5547599,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 56993682,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 57647055,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 7647055,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 84078184,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 84771036,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 34771036,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 76876279,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 77414779,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 27414779,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 59292511,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 59776109,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 9776109,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 73708511,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 74387123,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 24387123,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 682440775,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50754986,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 754986,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 100190245,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 52575379,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 2575379,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 99862684,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52121949,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2121949,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 199771412,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 54100691,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 4100691,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 399970035,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51510493,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1510493,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 681066297,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 51000355,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 1000355,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1236598777,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50928567,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 928567,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49988462,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50394678,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 18442,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 18442,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 233281,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 18442,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 2065481,
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
            "value": 49938978,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50341573,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 16009,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 16009,
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
            "value": 16009,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 350642,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 1101050,
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
            "value": 99961380,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50346231,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 236791,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 218759,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 218759,
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
            "value": 17967,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 1928559,
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
            "value": 50526808,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 50920581,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 635270,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 633002,
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
            "value": 635270,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2283,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 1106558,
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
            "value": 50009565,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50306411,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 18232,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 18232,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 837834,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 18232,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 3617919,
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
            "value": 49950534,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50289046,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 17075,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 17075,
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
            "value": 17075,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 1701172,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1426214,
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
            "value": 200028189,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50819783,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1200024,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 768444,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1183674,
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
            "value": 15428,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 2288265,
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
            "value": 52895714,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 53242635,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 2943609,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 2941276,
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
            "value": 2943609,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3585,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1423479,
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
            "value": 50010793,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50303358,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 17321,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 17321,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1635458,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 17321,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 4406291,
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
            "value": 49979922,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50307509,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 20721,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 20721,
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
            "value": 20721,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 2775001,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1918864,
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
            "value": 400061832,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50905943,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2419862,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 785220,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2400894,
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
            "value": 18628,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 3015366,
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
            "value": 56137801,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 56453925,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 6083239,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 6080946,
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
            "value": 6083239,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3580,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1938350,
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
            "value": 50015522,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50306085,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 17336,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 17336,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 4210228,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 17336,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 9091406,
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
            "value": 49980166,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50284017,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 17110,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 17110,
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
            "value": 17110,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 7696658,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3851382,
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
            "value": 994670045,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50917339,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 6041716,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 814107,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 6020590,
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
            "value": 18868,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 5185567,
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
            "value": 58206440,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 58500376,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 8222335,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 8219676,
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
            "value": 8222335,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 5089308,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3904109,
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
            "value": 44754112,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 49737458,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 21592,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 21592,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 8129322,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 21592,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 18565190,
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
            "value": 50071238,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50335184,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 19950,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 19950,
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
            "value": 19950,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 16054609,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 7320909,
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
            "value": 1843715911,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50948178,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 11736922,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 785058,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 11713387,
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
            "value": 22639,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 9132418,
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
            "value": 65956500,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 66267813,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 15996338,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 15993680,
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
            "value": 15996338,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 10445795,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 7078423,
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
            "value": 46936450,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 49789766,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 21181,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 21181,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 9563686,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 21181,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 18374790,
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
            "value": 49999898,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50262024,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 9038,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 9038,
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
            "value": 9038,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 20986376,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7171597,
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
            "value": 1831543724,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50941538,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 12318419,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 812825,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 12299039,
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
            "value": 22438,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 9624423,
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
            "value": 73646297,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 73875636,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 30933725,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 30932033,
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
            "value": 30933725,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 10591567,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 7246645,
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
            "value": 41422144,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 49769421,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 23499,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 23499,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 9722159,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 23499,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 22479925,
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
            "value": 66169994,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 66755713,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 15846672,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 15844244,
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
            "value": 15846672,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 10503982,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7126513,
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
            "value": 1854966290,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50923978,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 12298732,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 853130,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 12274225,
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
            "value": 27927,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 9363414,
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
            "value": 49940128,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50188521,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 8663,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 8663,
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
            "value": 8663,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 16762927,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 7176560,
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
            "value": 325135227,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51450235,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 7928520,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1893567,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1791635,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 7876888,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 24747,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 283090772,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 263649763,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 75498,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 450442865,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 51992632,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 15845403,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1934798,
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
            "value": 15836645,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 8593,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2313906,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 3201342,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 427489182,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 130921,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 598222707,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51581105,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 10839502,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1895984,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2971352,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 7489965,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 23530,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 206022725,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 193052582,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 74236,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 450075237,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 92697364,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 57622544,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 44121721,
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
            "value": 13353654,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 44123298,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 9188,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 4271097,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 333876101,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 168913,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 425202171,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51464765,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 11720578,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1929062,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1730659,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 11676067,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 29925,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 383307060,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 360717820,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 214712,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 575162246,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 52016032,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 19776730,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1949718,
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
            "value": 19767967,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 8968,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3758316,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 3409251,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 519227683,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 334431,
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
          "id": "1f525d31eca1f3f171ad9bf199c0d583c5397db7",
          "message": "ci: use MongoDB thread caches on hosted runners",
          "timestamp": "2026-10-30T04:24:36Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/1f525d31eca1f3f171ad9bf199c0d583c5397db7"
        },
        "date": 1793354832420,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 76702540,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 76986188,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 26986188,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 700025369,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 51019636,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 1019636,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 76275150,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 76521913,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 26521913,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 49988491,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50379390,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 379390,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 52691100,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 53477166,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 3477166,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 55256860,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 55938277,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 5938277,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 57029818,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 57622362,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 7622362,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 75880120,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 76562670,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 26562670,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 89544451,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 90056183,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 40056183,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 57584880,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 58066055,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 8066055,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 77936181,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 78344246,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 28344246,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 687777942,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50636292,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 636292,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 99436239,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 51539244,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 1539244,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 99352207,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52042663,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2042663,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 199669045,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 52313491,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 2313491,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 399697432,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51315637,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1315637,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 678812295,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50541769,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 541769,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1218212545,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50506005,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 506005,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 50001153,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50274630,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 12954,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 12954,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 165209,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 12954,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 1989153,
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
            "value": 49987942,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50263305,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 15143,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 15143,
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
            "value": 15143,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 311445,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 925721,
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
            "value": 99976581,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50241099,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 178474,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 169065,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 169065,
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
            "value": 8979,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 1730803,
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
            "value": 50624902,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 50882899,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 648047,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 645584,
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
            "value": 648047,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2278,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 822851,
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
            "value": 50003825,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50242179,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 14156,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 14156,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 753912,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 14156,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 3184228,
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
            "value": 49968005,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50229741,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 15653,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 15653,
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
            "value": 15653,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 1613443,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1245939,
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
            "value": 199991855,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50811338,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1023258,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 691998,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1009828,
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
            "value": 12158,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 2174716,
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
            "value": 53736615,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 53968760,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 3758639,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 3756265,
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
            "value": 3758639,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3220,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1277862,
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
            "value": 50004748,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50279239,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 15423,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 15423,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1415144,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 15423,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 4106189,
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
            "value": 49985911,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50220470,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 15288,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 15288,
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
            "value": 15288,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 2696262,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1760481,
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
            "value": 400042624,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50831557,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2106047,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 704333,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2092987,
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
            "value": 15228,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 2949183,
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
            "value": 59667992,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 59901184,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 9739201,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 9736302,
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
            "value": 9739201,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3450,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1745432,
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
            "value": 50016486,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50283609,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 18472,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 18472,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 3747092,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 18472,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 8333135,
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
            "value": 49937620,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50237558,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 18949,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 18949,
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
            "value": 18949,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 7390679,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3686493,
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
            "value": 996905859,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50821309,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 5179359,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 736586,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 5158307,
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
            "value": 20230,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 4978847,
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
            "value": 59671935,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 59964115,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 9754324,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 9750608,
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
            "value": 9754324,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 4560093,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3719536,
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
            "value": 46748537,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 49767878,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 21928,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 21928,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 7849284,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 21928,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 17216783,
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
            "value": 49948431,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50217776,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 20235,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 20235,
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
            "value": 20235,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 15299370,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 6881310,
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
            "value": 1845968432,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50851243,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 10239255,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 724213,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 10213406,
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
            "value": 21743,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 8557260,
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
            "value": 68785326,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 69065253,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 18849819,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 18846704,
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
            "value": 18849819,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 9762784,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 6768491,
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
            "value": 45619566,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 49790673,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 21132,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 21132,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 7341069,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 21132,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 16209928,
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
            "value": 50006415,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50240518,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 9539,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 9539,
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
            "value": 9539,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 20478671,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 6660998,
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
            "value": 1849303377,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50840711,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 10197795,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 757988,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 10177584,
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
            "value": 20706,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 9135134,
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
            "value": 79859346,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 80081871,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 32885092,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 32883115,
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
            "value": 32885092,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 10138595,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 6823190,
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
            "value": 44148166,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 49736902,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 19189,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 19189,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 7758610,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 19189,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 16774008,
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
            "value": 67361393,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 67649593,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 17370388,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 17368240,
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
            "value": 17370388,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 9910270,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 6745701,
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
            "value": 1837900045,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50876472,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 10511146,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 729806,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 10490844,
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
            "value": 20380,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 9049139,
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
            "value": 50011351,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50242560,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 9194,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 9194,
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
            "value": 9194,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 15750002,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 6798005,
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
            "value": 300484545,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51474948,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 7606588,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1891613,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1672027,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 7589207,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 47957,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 260503200,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 241176936,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 148103,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 475157851,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 52179590,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 15676450,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1937528,
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
            "value": 15667347,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 9143,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2526782,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 3467854,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 426628081,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 202625,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 618007960,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51681585,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 10033747,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1888039,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2383731,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 7578631,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 40541,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 254136526,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 240896991,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 148529,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 452172284,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 101018372,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 65127720,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 52131889,
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
            "value": 13086132,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 52133706,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 10611,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 3238976,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 334199437,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 171168,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 400441450,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51538639,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 11673303,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1929488,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1725362,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 11619062,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 48087,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 363092091,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 337382198,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 237358,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 550328923,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 52332684,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 19610247,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1955608,
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
            "value": 19599796,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 8618,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3636877,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 3569502,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 498385320,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 386839,
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
          "id": "1f525d31eca1f3f171ad9bf199c0d583c5397db7",
          "message": "ci: use MongoDB thread caches on hosted runners",
          "timestamp": "2026-10-30T04:24:36Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/1f525d31eca1f3f171ad9bf199c0d583c5397db7"
        },
        "date": 1793439950378,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 76068827,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 76378047,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 26378047,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 700015636,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50325787,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 325787,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 76650031,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 77058337,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 27058337,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 48244380,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 49460542,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 52438161,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 53101997,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 3101997,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 55421272,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 55897399,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 5897399,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 57610944,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 58137937,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 8137937,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 65842936,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 66410229,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 16410229,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 76648661,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 77148294,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 27148294,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 57650438,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 58095619,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 8095619,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 84959412,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 85443241,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 35443241,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 689466170,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50332884,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 332884,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 99963464,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 51750008,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 1750008,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 99767728,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 54542066,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 4542066,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 199421357,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50693750,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 693750,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 399685034,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51277427,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1277427,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 633630558,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50686579,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 686579,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1254071574,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50543396,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 543396,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 50010747,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50313885,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 14476,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 14476,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 209821,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 14476,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 2026334,
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
            "value": 49970737,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50260202,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 15428,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 15428,
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
            "value": 15428,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 317556,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 930987,
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
            "value": 100044647,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50283807,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 207768,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 188915,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 188915,
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
            "value": 17465,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 1849542,
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
            "value": 50591531,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 50883044,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 640016,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 637532,
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
            "value": 640016,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2569,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 977106,
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
            "value": 50010325,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50259561,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 11927,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 11927,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 726292,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 11927,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 3034873,
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
            "value": 49901263,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50162498,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 16033,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 16033,
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
            "value": 16033,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 1683112,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1279322,
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
            "value": 200018178,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50788330,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1183496,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 734570,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1170597,
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
            "value": 12253,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 2020563,
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
            "value": 53980580,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 54212150,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 3968560,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 3966402,
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
            "value": 3968560,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3320,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1213231,
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
            "value": 50011366,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50246455,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 12934,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 12934,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1410225,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 12934,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 4137563,
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
            "value": 49974079,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50171478,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 15383,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 15383,
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
            "value": 15383,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 2772822,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1651993,
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
            "value": 400037997,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50776521,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2193938,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 693974,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2162231,
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
            "value": 11837,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 2712227,
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
            "value": 57080548,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 57241492,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 7077401,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 7076004,
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
            "value": 7077401,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3265,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1502314,
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
            "value": 50011372,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50188078,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 12843,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 12843,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 3582175,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 12843,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 7941745,
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
            "value": 49980478,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50164104,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 15943,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 15943,
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
            "value": 15943,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 7319231,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3383277,
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
            "value": 999930715,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50773470,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 5691771,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 717511,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 5679248,
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
            "value": 13089,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 4689749,
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
            "value": 60337097,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 60516177,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 10344244,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 10343262,
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
            "value": 10344244,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 4610756,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3512725,
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
            "value": 50022511,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50222315,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 18783,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 18783,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 7505854,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 18783,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 15419035,
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
            "value": 50063496,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50284547,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 17781,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 17781,
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
            "value": 17781,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 15168216,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 6350310,
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
            "value": 1840444744,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50809282,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 11636565,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 715148,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 11595464,
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
            "value": 19414,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 8059539,
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
            "value": 67424278,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 67684645,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 17252194,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 17249570,
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
            "value": 17252194,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 9704865,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 6741326,
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
            "value": 50018189,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50218503,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 16134,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 16134,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 6525466,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 16134,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 14522024,
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
            "value": 50049329,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50253440,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 7661,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 7661,
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
            "value": 7661,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 17888010,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 6032511,
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
            "value": 1846883047,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50748454,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 11443361,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 700781,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 11427983,
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
            "value": 16609,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 8622876,
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
            "value": 77579360,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 77782947,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 29478678,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 29476875,
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
            "value": 29478678,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 9624018,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 6369794,
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
            "value": 46348194,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 49801615,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 16148,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 16148,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 7256270,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 16148,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 15432051,
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
            "value": 65598349,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 65807139,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 15606329,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 15604391,
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
            "value": 15606329,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 9451740,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 6681661,
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
            "value": 1851254591,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50810568,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 11766718,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 721852,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 11737318,
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
            "value": 15548,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 8403481,
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
            "value": 50030865,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50257528,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 7221,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 7221,
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
            "value": 7221,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 15107415,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 6693621,
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
            "value": 300024891,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51584589,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 7191270,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1895103,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1595540,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 7172091,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 19038,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 255741932,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 241339880,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 105191,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 401110010,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 52014242,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 14014167,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1935924,
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
            "value": 14003066,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 7210,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2119403,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 2851311,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 376660995,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 114199,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 587128375,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51585943,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 9727913,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1893149,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2764027,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 6942755,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 17501,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 205236888,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 193144571,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 63944,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 448125520,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 99297944,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 61193895,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 49194102,
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
            "value": 11923567,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 49195770,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 8332,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 2934544,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 334193875,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 111926,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 400011719,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51591372,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 10245944,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1928391,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1634583,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 10195495,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 21411,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 355022455,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 337010849,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 162026,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 499956858,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 52016848,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 17187915,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1949174,
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
            "value": 17178181,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 7701,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3342240,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 2930253,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 427928024,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 326425,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}