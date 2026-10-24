window.BENCHMARK_DATA = {
  "lastUpdate": 1792829658844,
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
        "date": 1791120622695,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 94916647,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 95158680,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 45158680,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 749999370,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50457879,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 457879,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 75878837,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 76059289,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 26059289,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 50010275,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50311329,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 311329,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 53021541,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 53726792,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 3726792,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 58305036,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 59016476,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 9016476,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 59559231,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 60230541,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 10230541,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 77790949,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 78421727,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 28421727,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 101214641,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 101797048,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 51797048,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 60002879,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 60525024,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 10525024,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 82264777,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 82712780,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 32712780,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 786039922,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50662504,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 662504,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 99909149,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 52387508,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 2387508,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 199878587,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52203431,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2203431,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 199831937,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 52481048,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 2481048,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 393048146,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 52141308,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 2141308,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 738019777,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 51039040,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 1039040,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1416792130,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50810946,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 810946,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49983502,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50352943,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 16595,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 16595,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 207251,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 16595,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 3154113,
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
            "value": 49956548,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50326134,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 16119,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 16119,
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
            "value": 16119,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 405119,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 595490,
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
            "value": 99943152,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50315262,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 231348,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 215379,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 215379,
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
            "value": 15979,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 3060993,
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
            "value": 50581981,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 50900868,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 638700,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 636627,
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
            "value": 638700,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2323,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 592025,
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
            "value": 49955846,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50275807,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 17591,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 17591,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 812637,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 17591,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 12411645,
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
            "value": 49949159,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50272765,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 16750,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 16750,
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
            "value": 16750,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2136086,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1011441,
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
            "value": 200005622,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 51040850,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1477817,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 933413,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1458187,
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
            "value": 17946,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 11629023,
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
            "value": 54150068,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 54452307,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 4172444,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 4170336,
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
            "value": 4172444,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3450,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1016338,
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
            "value": 49975422,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50247972,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 17526,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 17526,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1532144,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 17526,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 24206061,
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
            "value": 49954882,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50255638,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 16370,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 16370,
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
            "value": 16370,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 3543854,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1611292,
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
            "value": 400030735,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 51105827,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2992951,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 974010,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2976826,
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
            "value": 18062,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 22624562,
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
            "value": 58893789,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 59169447,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 8920563,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 8918546,
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
            "value": 8920563,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3641,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1619305,
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
            "value": 100014184,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50211024,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 20831,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 20831,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 4317832,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 20831,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 60320598,
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
            "value": 49939368,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50226524,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 17501,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 17501,
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
            "value": 17501,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 9821043,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3839313,
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
            "value": 1036130303,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 51041400,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 7331566,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 961746,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 7311455,
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
            "value": 21026,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 55857618,
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
            "value": 60168032,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 60445164,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 10239919,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 10236979,
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
            "value": 10239919,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 6623165,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3852848,
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
            "value": 134594207,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50247330,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 21087,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 21087,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 9686962,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 21087,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 121528078,
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
            "value": 49925225,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50208298,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 18713,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 18713,
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
            "value": 18713,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 21002937,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 7604158,
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
            "value": 1920645508,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 51078932,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 14714617,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 985542,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 14677166,
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
            "value": 22173,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 111617330,
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
            "value": 65414039,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 65743510,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 15483043,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 15480559,
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
            "value": 15483043,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 14182090,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 7556884,
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
            "value": 140590226,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50167437,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 17586,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 17586,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 8576087,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 17586,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 121826452,
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
            "value": 50014785,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50216583,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 5247,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 5247,
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
            "value": 5247,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 23364422,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7438511,
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
            "value": 1927569523,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 51023115,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 14489322,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 949709,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 14469842,
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
            "value": 19114,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 111242879,
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
            "value": 68940240,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 69159489,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 29675597,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 29674245,
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
            "value": 29675597,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 13863690,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 6998956,
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
            "value": 136915336,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50168688,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 17075,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 17075,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 6169374,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 17075,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 117452111,
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
            "value": 64880162,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 65243718,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 14730790,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 14728902,
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
            "value": 14730790,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 14393362,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7494611,
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
            "value": 1936381166,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 51026565,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 14566011,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 950024,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 14549667,
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
            "value": 19264,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 111212978,
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
            "value": 49949382,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50164031,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 5202,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 5202,
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
            "value": 5202,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 20668270,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 7505758,
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
            "value": 349991120,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51289064,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 9550776,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1925528,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1877033,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 9533810,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 16579,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 325340439,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 288433714,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 63500,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 499969523,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 52032646,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 17593693,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1947452,
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
            "value": 17589292,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 4386,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2535988,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 5131041,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 426320276,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 101913,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 618209631,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51562598,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 12421963,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1926816,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 3153131,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 9178905,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 16064,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 275589359,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 240856872,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 62178,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 521950934,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 112811151,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 79284503,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 64061272,
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
            "value": 15394731,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 64062334,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 6735,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 5236269,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 380757359,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 106069,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 500016238,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51318704,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 14844315,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1947892,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1810082,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 14821977,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 15343,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 475497407,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 431715934,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 173546,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 600632815,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 51992689,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 21821599,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1959959,
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
            "value": 21815945,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 4797,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3510539,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 5054446,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 569155198,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 159259,
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
        "date": 1792138664078,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 97880042,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 98171271,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 48171271,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 800013043,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50494589,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 494589,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 76946747,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 77163524,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 27163524,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 50051992,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 50333767,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 333767,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 52858957,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 53750561,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 3750561,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 57593654,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 58395443,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 8395443,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 61394377,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 62247953,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 12247953,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 74818062,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 75467281,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 25467281,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 90994947,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 91456453,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 41456453,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 58359562,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 58856481,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 8856481,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 83330974,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 83805419,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 33805419,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 774952946,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50689689,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 689689,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 100085163,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 52464693,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 2464693,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 199791316,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 54857589,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 4857589,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 199891369,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 52569293,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 2569293,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 443200677,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 50835921,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 835921,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 746607786,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50993017,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 993017,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1409408792,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50866687,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 866687,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49960159,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50319511,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 17616,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 17616,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 228139,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 17616,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 3276869,
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
            "value": 49918020,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50319842,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 16475,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 16475,
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
            "value": 16475,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 403914,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 593534,
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
            "value": 99965292,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50336952,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 229811,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 211944,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 211944,
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
            "value": 17712,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 3185540,
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
            "value": 50694201,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 51011018,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 686054,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 683830,
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
            "value": 686054,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2373,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 599679,
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
            "value": 49963481,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50282877,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 18062,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 18062,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 854017,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 18062,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 12531983,
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
            "value": 49981042,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50289637,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 16920,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 16920,
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
            "value": 16920,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2166560,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1051314,
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
            "value": 199965811,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 51058266,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1429019,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 949671,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1406865,
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
            "value": 17957,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 11630199,
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
            "value": 53449074,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 53754444,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 3470055,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 3467792,
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
            "value": 3470055,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3365,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1024960,
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
            "value": 49978118,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50258442,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 18398,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 18398,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1627608,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 18398,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 24331423,
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
            "value": 49971983,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50268055,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 17732,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 17732,
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
            "value": 17732,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 3821174,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1602760,
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
            "value": 400021210,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 51090620,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2828102,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 966166,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2810586,
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
            "value": 18478,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 22580223,
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
            "value": 62015176,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 62347201,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 12052967,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 12050583,
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
            "value": 12052967,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 4016,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1601374,
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
            "value": 100014609,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50242621,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 21292,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 21292,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 4149389,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 21292,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 59644252,
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
            "value": 49926715,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50225035,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 17306,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 17306,
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
            "value": 17306,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 9964749,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3782510,
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
            "value": 1038642787,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 51021095,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 6630427,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 946411,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 6607373,
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
            "value": 21487,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 55983550,
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
            "value": 59804363,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 60089573,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 9863586,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 9860492,
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
            "value": 9863586,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 6614595,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3796407,
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
            "value": 136648139,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50202450,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 19429,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 19429,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 10007850,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 19429,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 120479891,
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
            "value": 49916394,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50205229,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 17912,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 17912,
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
            "value": 17912,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 21200064,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 7508167,
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
            "value": 1926462932,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 51022930,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 12997569,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 937488,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 12975532,
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
            "value": 21297,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 111253262,
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
            "value": 65965143,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 66244846,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 16098625,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 16096046,
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
            "value": 16098625,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 13992544,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 7504223,
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
            "value": 136501833,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50163878,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 19824,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 19824,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 9422538,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 19824,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 120103164,
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
            "value": 50014565,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50247864,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 7115,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 7115,
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
            "value": 7115,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 25117638,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7529075,
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
            "value": 1919414792,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50996498,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 10191443,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 905851,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 10165443,
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
            "value": 22874,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 110669128,
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
            "value": 76371234,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 76599168,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 26850318,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 26848736,
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
            "value": 26850318,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 14015713,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 7445600,
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
            "value": 132796448,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50170134,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 19028,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 19028,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 9722932,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 19028,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 120327045,
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
            "value": 64673187,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 64910425,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 14735810,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 14734253,
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
            "value": 14735810,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 14046529,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7453453,
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
            "value": 1931991808,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 51051992,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 12423088,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 961138,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 12402612,
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
            "value": 21612,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 111019445,
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
            "value": 49932789,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50154323,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 7191,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 7191,
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
            "value": 7191,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 21431562,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 7437979,
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
            "value": 349984296,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51207210,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 9867281,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1923078,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1847004,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 9850670,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 18659,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 325969516,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 288542565,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 107286,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 500081968,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 52019882,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 17543972,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1946396,
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
            "value": 17537007,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 6850,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2727336,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 5137580,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 448846106,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 124367,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 622016709,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51607984,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 12322387,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1922820,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 3043513,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 9224153,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 18388,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 275299394,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 240892752,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 62734,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 526128233,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 110467373,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 77773778,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 62233429,
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
            "value": 15538826,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 62234951,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 8633,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 4984650,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 380837422,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 110021,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 500070331,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51206220,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 15483270,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1951738,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 2245920,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 15464332,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 21057,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 475407516,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 432676591,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 234714,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 601589700,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 51994163,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 23029746,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1963860,
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
            "value": 23022870,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 6810,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3758076,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 5022927,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 569544586,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 213346,
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
        "date": 1792829658405,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 96688923,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 96966993,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 46966993,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 749976331,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50409726,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 409726,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 82476026,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 82675979,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 32675979,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 48838432,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 49726128,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 52410666,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 53339534,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 3339534,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 58081028,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 58745948,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 8745948,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 59466202,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 60125464,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 10125464,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 83247989,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 84618130,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 34618130,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 90736931,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 91219219,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 41219219,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 59133639,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 59625231,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 9625231,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 78841424,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 79376621,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 29376621,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 790491294,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50652393,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 652393,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 99952229,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 52456156,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 2456156,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 199967392,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 54598668,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 4598668,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 149854772,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50275114,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 275114,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 443755609,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51258394,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1258394,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 754066574,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 51231322,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 1231322,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1369571950,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50672343,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 672343,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49964117,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50301280,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 13951,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 13951,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 170923,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 13951,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 3218643,
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
            "value": 49943856,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50262301,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 15964,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 15964,
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
            "value": 15964,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 393113,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 569273,
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
            "value": 99992948,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50322397,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 218138,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 201939,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 201939,
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
            "value": 15969,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 3095973,
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
            "value": 50897419,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 51252865,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 1025471,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 1022967,
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
            "value": 1025471,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2434,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 635143,
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
            "value": 49975454,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50253410,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 15007,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 15007,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 866056,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 15007,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 12370890,
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
            "value": 49946413,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50219250,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 16505,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 16505,
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
            "value": 16505,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2131621,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1018700,
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
            "value": 199983996,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50971252,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1368471,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 875838,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1352868,
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
            "value": 15764,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 11554790,
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
            "value": 55659161,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 55933073,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 5660969,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 5658320,
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
            "value": 5660969,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 3185,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1009286,
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
            "value": 49986145,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50219696,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 14697,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 14697,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1631200,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 14697,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 24292653,
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
            "value": 49969785,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50228159,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 15953,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 15953,
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
            "value": 15953,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 3602013,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 1546252,
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
            "value": 400037958,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 51017284,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2809276,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 915821,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2793473,
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
            "value": 15814,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 22577240,
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
            "value": 56476007,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 56739939,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 6469566,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 6466892,
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
            "value": 6469566,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3670,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 1556783,
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
            "value": 100014609,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50191414,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 18383,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 18383,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 3975373,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 18383,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 59530276,
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
            "value": 49952891,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50203768,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 17526,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 17526,
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
            "value": 17526,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 9663656,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 3790781,
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
            "value": 1037729292,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 51007846,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 6994321,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 923648,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 6976213,
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
            "value": 18738,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 55967827,
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
            "value": 59867559,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 60128617,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 9916923,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 9914259,
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
            "value": 9916923,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 6530867,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 3824552,
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
            "value": 139270843,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50189458,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 18222,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 18222,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 8665316,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 18222,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 119715220,
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
            "value": 49955632,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50216589,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 17296,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 17296,
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
            "value": 17296,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 20372754,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 7434008,
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
            "value": 1943562229,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 51010262,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 13781750,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 929847,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 13762230,
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
            "value": 20030,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 111738517,
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
            "value": 64459269,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 64707788,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 14493177,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 14490698,
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
            "value": 14493177,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 14167787,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 7520565,
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
            "value": 141002377,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50163566,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 17736,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 17736,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 7723802,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 17736,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 119163852,
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
            "value": 50013063,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50214793,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 6660,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 6660,
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
            "value": 6660,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 23538883,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 7390552,
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
            "value": 1930388027,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50985291,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 11980663,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 925571,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 11960898,
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
            "value": 18293,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 111746680,
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
            "value": 74659778,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 74856911,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 24605858,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 24604205,
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
            "value": 24605858,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 14292015,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 7383243,
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
            "value": 139096079,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50147690,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 17186,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 17186,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 8232583,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 17186,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 119540606,
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
            "value": 64749039,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 64989152,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 14785673,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 14783415,
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
            "value": 14785673,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 14034977,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 7401376,
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
            "value": 1915031411,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 51005267,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 13808714,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 933624,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 13786625,
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
            "value": 17927,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 111416277,
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
            "value": 49956092,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50162194,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 6284,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 6284,
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
            "value": 6284,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 20501859,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 7396890,
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
            "value": 349946890,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51223379,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 9547349,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1928890,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 1868278,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 9532191,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 16615,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 325221904,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 288330435,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 56751,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 475965699,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 52012172,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 17223668,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1945579,
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
            "value": 17217284,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 6524,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 2479777,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 5039356,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 426341011,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 104733,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 602226293,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51594050,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 12096296,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1930200,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 3012602,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 9061352,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 16665,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 275327491,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 240906209,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 58283,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 517449722,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 111388177,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 78280779,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 62920753,
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
            "value": 15361861,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 62922491,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 6930,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 5027648,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 380403657,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 100947,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 470413191,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51296226,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 14001660,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1946495,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1624645,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 13986081,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 15783,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 447461714,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 403607181,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 153551,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 600738202,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 52005833,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 22183496,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1959443,
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
            "value": 22177222,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 6725,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3475542,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 5094634,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 569512257,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 139676,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}