window.BENCHMARK_DATA = {
  "lastUpdate": 1791107497571,
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
        "date": 1791018792800,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 79439856,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 78337652,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 28337652,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 726286386,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 74137782,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 24137782,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 83839986,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 83685387,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 33685387,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 24869002,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 82647897,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 32647897,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 55398530,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 56923874,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 6923874,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 58448368,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 58044856,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 8044856,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 63566663,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 63148310,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 13148310,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 78351719,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 78295004,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 28295004,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 98013317,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 97921115,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 47921115,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 59868982,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 59703244,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 9703244,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 84425564,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 84328133,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 34328133,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 775702782,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50575107,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 575107,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 35743442,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 68103251,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 18103251,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 121799776,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 50790954,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 790954,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 203582502,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 51234781,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 1234781,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 398007897,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 52846555,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 2846555,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 669695650,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 84183203,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 34183203,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1462680762,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 88312139,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 38312139,
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
        "date": 1791107497271,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 99909546,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 98395312,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 48395312,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 731260486,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 69235212,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 19235212,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 101125263,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 100620514,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 50620514,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 15645983,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 91276017,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 41276017,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 53548009,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 56540528,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 6540528,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 60247030,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 60136935,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 10136935,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 63306113,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 63079240,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 13079240,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 76421853,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 76351653,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 26351653,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 100194984,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 100128530,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 50128530,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 62164940,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 61637096,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 11637096,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 100804977,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 99874942,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 49874942,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 773215783,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 51346298,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 1346298,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 186778220,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 66519062,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 16519062,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 176285218,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 52167444,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 2167444,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 95828587,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 62833894,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 12833894,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 392240625,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 61940848,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 11940848,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 546103018,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 56222486,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 6222486,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1270392111,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 80051464,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 30051464,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}