window.BENCHMARK_DATA = {
  "lastUpdate": 1791107504296,
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
        "date": 1791018802360,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 83490921,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 82518525,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 32518525,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 635400357,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 65079375,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 15079375,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 50845474,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 55049356,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 5049356,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 54195043,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 53874817,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 3874817,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 56027412,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 56255120,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 6255120,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 65142457,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 64317462,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 14317462,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 70410997,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 70178761,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 20178761,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 58127920,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 57882426,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 7882426,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 78183603,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 77918226,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 27918226,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 684682903,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50617578,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 617578,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 96092877,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 61180062,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 11180062,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 90907190,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 51368213,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 1368213,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 184826105,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 51781366,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 1781366,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 371781478,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51128353,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1128353,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 605627012,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50805849,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 805849,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1251776400,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50741212,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 741212,
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
        "date": 1791107504060,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 78031421,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 77161971,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 27161971,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 685848190,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 64556486,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 14556486,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 50915896,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 56377834,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 6377834,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 55261340,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 55137530,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 5137530,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 57421686,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 57149311,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 7149311,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 65295800,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 63506131,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 13506131,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 80358585,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 78968010,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 28968010,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 60907068,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 60593714,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 10593714,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 73758532,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 73564102,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 23564102,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 683561848,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50538972,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 538972,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 45903844,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 55463319,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 5463319,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 39159682,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 50122426,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 122426,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 186054315,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 55088621,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 5088621,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 274881476,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 51120293,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 1120293,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 712980658,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 51030937,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 1030937,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1234568058,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 66568029,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 16568029,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}