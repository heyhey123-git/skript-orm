window.BENCHMARK_DATA = {
  "lastUpdate": 1791120627425,
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
      }
    ]
  }
}