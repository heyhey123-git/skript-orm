window.BENCHMARK_DATA = {
  "lastUpdate": 1791196338663,
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
          "id": "857a0a67879ae82658e858269eb24be8b9c7f1b6",
          "message": "chore(release): prepare version 1.4.2",
          "timestamp": "2026-10-04T15:44:43Z",
          "url": "https://github.com/heyhey123-git/skript-orm/commit/857a0a67879ae82658e858269eb24be8b9c7f1b6"
        },
        "date": 1791196338091,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "pluginread 5000rows wall",
            "value": 112396130,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows gap",
            "value": 112716139,
            "unit": "ns"
          },
          {
            "name": "pluginread 5000rows overrun",
            "value": 62716139,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows wall",
            "value": 749973516,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows gap",
            "value": 50431109,
            "unit": "ns"
          },
          {
            "name": "pluginwrite 5000rows overrun",
            "value": 431109,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows wall",
            "value": 112801071,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows gap",
            "value": 112999132,
            "unit": "ns"
          },
          {
            "name": "rawread 5000rows overrun",
            "value": 62999132,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows wall",
            "value": 35878874,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows gap",
            "value": 49747933,
            "unit": "ns"
          },
          {
            "name": "rawwrite 5000rows overrun",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "read 100rows wall",
            "value": 54584582,
            "unit": "ns"
          },
          {
            "name": "read 100rows gap",
            "value": 55600867,
            "unit": "ns"
          },
          {
            "name": "read 100rows overrun",
            "value": 5600867,
            "unit": "ns"
          },
          {
            "name": "read 500rows wall",
            "value": 60914926,
            "unit": "ns"
          },
          {
            "name": "read 500rows gap",
            "value": 61997745,
            "unit": "ns"
          },
          {
            "name": "read 500rows overrun",
            "value": 11997745,
            "unit": "ns"
          },
          {
            "name": "read 1000rows wall",
            "value": 62539739,
            "unit": "ns"
          },
          {
            "name": "read 1000rows gap",
            "value": 63441679,
            "unit": "ns"
          },
          {
            "name": "read 1000rows overrun",
            "value": 13441679,
            "unit": "ns"
          },
          {
            "name": "read 2500rows wall",
            "value": 97136898,
            "unit": "ns"
          },
          {
            "name": "read 2500rows gap",
            "value": 97935455,
            "unit": "ns"
          },
          {
            "name": "read 2500rows overrun",
            "value": 47935455,
            "unit": "ns"
          },
          {
            "name": "read 5000rows wall",
            "value": 89332891,
            "unit": "ns"
          },
          {
            "name": "read 5000rows gap",
            "value": 89854419,
            "unit": "ns"
          },
          {
            "name": "read 5000rows overrun",
            "value": 39854419,
            "unit": "ns"
          },
          {
            "name": "read 10000rows wall",
            "value": 61357645,
            "unit": "ns"
          },
          {
            "name": "read 10000rows gap",
            "value": 61943163,
            "unit": "ns"
          },
          {
            "name": "read 10000rows overrun",
            "value": 11943163,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows wall",
            "value": 84679199,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows gap",
            "value": 85295825,
            "unit": "ns"
          },
          {
            "name": "warmread 5000rows overrun",
            "value": 35295825,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows wall",
            "value": 774591664,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows gap",
            "value": 50799381,
            "unit": "ns"
          },
          {
            "name": "warmwrite 5000rows overrun",
            "value": 799381,
            "unit": "ns"
          },
          {
            "name": "write 100rows wall",
            "value": 100059323,
            "unit": "ns"
          },
          {
            "name": "write 100rows gap",
            "value": 52351499,
            "unit": "ns"
          },
          {
            "name": "write 100rows overrun",
            "value": 2351499,
            "unit": "ns"
          },
          {
            "name": "write 500rows wall",
            "value": 200036122,
            "unit": "ns"
          },
          {
            "name": "write 500rows gap",
            "value": 55542468,
            "unit": "ns"
          },
          {
            "name": "write 500rows overrun",
            "value": 5542468,
            "unit": "ns"
          },
          {
            "name": "write 1000rows wall",
            "value": 187259709,
            "unit": "ns"
          },
          {
            "name": "write 1000rows gap",
            "value": 50240743,
            "unit": "ns"
          },
          {
            "name": "write 1000rows overrun",
            "value": 240743,
            "unit": "ns"
          },
          {
            "name": "write 2500rows wall",
            "value": 361648357,
            "unit": "ns"
          },
          {
            "name": "write 2500rows gap",
            "value": 52259296,
            "unit": "ns"
          },
          {
            "name": "write 2500rows overrun",
            "value": 2259296,
            "unit": "ns"
          },
          {
            "name": "write 5000rows wall",
            "value": 551043873,
            "unit": "ns"
          },
          {
            "name": "write 5000rows gap",
            "value": 50644487,
            "unit": "ns"
          },
          {
            "name": "write 5000rows overrun",
            "value": 644487,
            "unit": "ns"
          },
          {
            "name": "write 10000rows wall",
            "value": 1460226797,
            "unit": "ns"
          },
          {
            "name": "write 10000rows gap",
            "value": 50922170,
            "unit": "ns"
          },
          {
            "name": "write 10000rows overrun",
            "value": 922170,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 wallNs median",
            "value": 49964914,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 gapNs median",
            "value": 50297577,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainNs median",
            "value": 10379,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 mainTickMaxNs median",
            "value": 10379,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 prepareAsyncNs median",
            "value": 224661,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultMainNs median",
            "value": 10379,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 100 executionNs median",
            "value": 3624283,
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
            "value": 49974107,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 gapNs median",
            "value": 50354890,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainNs median",
            "value": 12959,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 mainTickMaxNs median",
            "value": 12959,
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
            "value": 12959,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 resultAsyncNs median",
            "value": 569442,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 100 executionNs median",
            "value": 686968,
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
            "value": 99978309,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 gapNs median",
            "value": 50282830,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainNs median",
            "value": 214507,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 mainTickMaxNs median",
            "value": 202379,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 prepareMainNs median",
            "value": 202379,
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
            "value": 11296,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 100 executionNs median",
            "value": 3633705,
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
            "value": 51428527,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 gapNs median",
            "value": 51777200,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainNs median",
            "value": 1567928,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 mainTickMaxNs median",
            "value": 1565874,
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
            "value": 1567928,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 resultAsyncNs median",
            "value": 2044,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 100 executionNs median",
            "value": 616505,
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
            "value": 49989395,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 gapNs median",
            "value": 50240206,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainNs median",
            "value": 8827,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 mainTickMaxNs median",
            "value": 8827,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 prepareAsyncNs median",
            "value": 792359,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultMainNs median",
            "value": 8827,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 500 executionNs median",
            "value": 13876091,
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
            "value": 49971592,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 gapNs median",
            "value": 50210970,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainNs median",
            "value": 10910,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 mainTickMaxNs median",
            "value": 10910,
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
            "value": 10910,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 resultAsyncNs median",
            "value": 2861640,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 500 executionNs median",
            "value": 1405479,
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
            "value": 200030456,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 gapNs median",
            "value": 50899587,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainNs median",
            "value": 1130484,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 mainTickMaxNs median",
            "value": 796944,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 prepareMainNs median",
            "value": 1119568,
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
            "value": 10915,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 500 executionNs median",
            "value": 13088549,
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
            "value": 55929973,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 gapNs median",
            "value": 56160821,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainNs median",
            "value": 5912600,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 mainTickMaxNs median",
            "value": 5911177,
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
            "value": 5912600,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 resultAsyncNs median",
            "value": 2961,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 500 executionNs median",
            "value": 1404202,
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
            "value": 49998012,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 gapNs median",
            "value": 50290396,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainNs median",
            "value": 12768,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 mainTickMaxNs median",
            "value": 12768,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 prepareAsyncNs median",
            "value": 1507801,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultMainNs median",
            "value": 12768,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 1000 executionNs median",
            "value": 26785653,
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
            "value": 49981202,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 gapNs median",
            "value": 50222885,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainNs median",
            "value": 13034,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 mainTickMaxNs median",
            "value": 13034,
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
            "value": 13034,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 resultAsyncNs median",
            "value": 4973461,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 1000 executionNs median",
            "value": 2334074,
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
            "value": 399995198,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 gapNs median",
            "value": 50930428,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainNs median",
            "value": 2566554,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 mainTickMaxNs median",
            "value": 862896,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 prepareMainNs median",
            "value": 2554717,
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
            "value": 13239,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 1000 executionNs median",
            "value": 25573748,
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
            "value": 58246837,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 gapNs median",
            "value": 58619812,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainNs median",
            "value": 8256281,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 mainTickMaxNs median",
            "value": 8254558,
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
            "value": 8256281,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 resultAsyncNs median",
            "value": 3075,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 1000 executionNs median",
            "value": 2264980,
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
            "value": 99954379,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 gapNs median",
            "value": 50208588,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainNs median",
            "value": 14677,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 mainTickMaxNs median",
            "value": 14677,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 prepareAsyncNs median",
            "value": 3604700,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultMainNs median",
            "value": 14677,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 2500 executionNs median",
            "value": 65874731,
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
            "value": 49977028,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 gapNs median",
            "value": 50295199,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainNs median",
            "value": 13921,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 mainTickMaxNs median",
            "value": 13921,
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
            "value": 13921,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 resultAsyncNs median",
            "value": 12676996,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 2500 executionNs median",
            "value": 4888786,
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
            "value": 1033291006,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 gapNs median",
            "value": 50998814,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainNs median",
            "value": 6575520,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 mainTickMaxNs median",
            "value": 899581,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 prepareMainNs median",
            "value": 6558628,
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
            "value": 15053,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 2500 executionNs median",
            "value": 63409883,
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
            "value": 59238236,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 gapNs median",
            "value": 59475455,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainNs median",
            "value": 9302218,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 mainTickMaxNs median",
            "value": 9300060,
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
            "value": 9302218,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 resultAsyncNs median",
            "value": 8168770,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 2500 executionNs median",
            "value": 5176532,
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
            "value": 174867527,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 gapNs median",
            "value": 50284371,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainNs median",
            "value": 16561,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 mainTickMaxNs median",
            "value": 16561,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 prepareAsyncNs median",
            "value": 7976905,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultMainNs median",
            "value": 16561,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local false 5000 executionNs median",
            "value": 132398947,
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
            "value": 49976494,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 gapNs median",
            "value": 50241265,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainNs median",
            "value": 16015,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 mainTickMaxNs median",
            "value": 16015,
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
            "value": 16015,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 resultAsyncNs median",
            "value": 27025427,
            "unit": "ns"
          },
          {
            "name": "read primitive local local false 5000 executionNs median",
            "value": 9839206,
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
            "value": 1903987984,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 gapNs median",
            "value": 50983252,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainNs median",
            "value": 13177381,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 mainTickMaxNs median",
            "value": 904621,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 prepareMainNs median",
            "value": 13157143,
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
            "value": 16010,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global false 5000 executionNs median",
            "value": 124913613,
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
            "value": 63577788,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 gapNs median",
            "value": 63818540,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainNs median",
            "value": 13598531,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 mainTickMaxNs median",
            "value": 13596682,
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
            "value": 13598531,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 resultAsyncNs median",
            "value": 17657706,
            "unit": "ns"
          },
          {
            "name": "read primitive global global false 5000 executionNs median",
            "value": 9270746,
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
            "value": 176399327,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 gapNs median",
            "value": 50167574,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainNs median",
            "value": 12438,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 mainTickMaxNs median",
            "value": 12438,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 prepareAsyncNs median",
            "value": 6299498,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultMainNs median",
            "value": 12438,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local local true 5000 executionNs median",
            "value": 130815969,
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
            "value": 50012152,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 gapNs median",
            "value": 50238283,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainNs median",
            "value": 5470,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 mainTickMaxNs median",
            "value": 5470,
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
            "value": 5470,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 resultAsyncNs median",
            "value": 27815737,
            "unit": "ns"
          },
          {
            "name": "read primitive local local true 5000 executionNs median",
            "value": 8946206,
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
            "value": 1903530078,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 gapNs median",
            "value": 50954834,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainNs median",
            "value": 12600364,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 mainTickMaxNs median",
            "value": 883501,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 prepareMainNs median",
            "value": 12581504,
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
            "value": 13560,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global global true 5000 executionNs median",
            "value": 124581890,
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
            "value": 76478488,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 gapNs median",
            "value": 71919546,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainNs median",
            "value": 27393300,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 mainTickMaxNs median",
            "value": 27392198,
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
            "value": 27393300,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 resultAsyncNs median",
            "value": 17804501,
            "unit": "ns"
          },
          {
            "name": "read primitive global global true 5000 executionNs median",
            "value": 8874492,
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
            "value": 174084727,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 gapNs median",
            "value": 50172276,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainNs median",
            "value": 12683,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 mainTickMaxNs median",
            "value": 12683,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 prepareAsyncNs median",
            "value": 7477869,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 conversionMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultMainNs median",
            "value": 12683,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive local global false 5000 executionNs median",
            "value": 132127690,
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
            "value": 62111786,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 gapNs median",
            "value": 62354706,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainNs median",
            "value": 12153434,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 mainTickMaxNs median",
            "value": 12150423,
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
            "value": 12153434,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 resultAsyncNs median",
            "value": 17648721,
            "unit": "ns"
          },
          {
            "name": "read primitive local global false 5000 executionNs median",
            "value": 9081594,
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
            "value": 1905793482,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 gapNs median",
            "value": 50927017,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainNs median",
            "value": 12566850,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 mainTickMaxNs median",
            "value": 872765,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 prepareMainNs median",
            "value": 12533338,
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
            "value": 14296,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write primitive global local false 5000 executionNs median",
            "value": 125074214,
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
            "value": 49984444,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 gapNs median",
            "value": 50158998,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainNs median",
            "value": 5580,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 mainTickMaxNs median",
            "value": 5580,
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
            "value": 5580,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 resultAsyncNs median",
            "value": 26246392,
            "unit": "ns"
          },
          {
            "name": "read primitive global local false 5000 executionNs median",
            "value": 9338996,
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
            "value": 450005711,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 gapNs median",
            "value": 51270129,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainNs median",
            "value": 13171223,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 mainTickMaxNs median",
            "value": 1928213,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 prepareAsyncNs median",
            "value": 2005709,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 conversionMainNs median",
            "value": 13155920,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultMainNs median",
            "value": 14221,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 executionNs median",
            "value": 427890688,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 queueWaitNs median",
            "value": 384280115,
            "unit": "ns"
          },
          {
            "name": "write mixed local local false 1000 largestConversionNs median",
            "value": 66074,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 wallNs median",
            "value": 699887257,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 gapNs median",
            "value": 51983534,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainNs median",
            "value": 23654487,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 mainTickMaxNs median",
            "value": 1962059,
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
            "value": 23649162,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultMainNs median",
            "value": 4964,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 resultAsyncNs median",
            "value": 3105936,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 executionNs median",
            "value": 5507417,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 queueWaitNs median",
            "value": 613844819,
            "unit": "ns"
          },
          {
            "name": "read mixed local local false 1000 largestConversionNs median",
            "value": 105928,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 wallNs median",
            "value": 669809955,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 gapNs median",
            "value": 51678945,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainNs median",
            "value": 15662561,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 mainTickMaxNs median",
            "value": 1921165,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareMainNs median",
            "value": 2602427,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 prepareAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 conversionMainNs median",
            "value": 13053988,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultMainNs median",
            "value": 17256,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 executionNs median",
            "value": 378345998,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 queueWaitNs median",
            "value": 336761892,
            "unit": "ns"
          },
          {
            "name": "write mixed global global false 1000 largestConversionNs median",
            "value": 70382,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 wallNs median",
            "value": 724053551,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 gapNs median",
            "value": 171016305,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainNs median",
            "value": 142447000,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 mainTickMaxNs median",
            "value": 122365752,
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
            "value": 20819334,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultMainNs median",
            "value": 122366604,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 resultAsyncNs median",
            "value": 6522,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 executionNs median",
            "value": 5882961,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 queueWaitNs median",
            "value": 523326296,
            "unit": "ns"
          },
          {
            "name": "read mixed global global false 1000 largestConversionNs median",
            "value": 95173,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 wallNs median",
            "value": 637868932,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 gapNs median",
            "value": 51222520,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainNs median",
            "value": 20358252,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 mainTickMaxNs median",
            "value": 1956752,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareMainNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 prepareAsyncNs median",
            "value": 1774951,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 conversionMainNs median",
            "value": 20340158,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultMainNs median",
            "value": 14772,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 resultAsyncNs median",
            "value": 0,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 executionNs median",
            "value": 614962980,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 queueWaitNs median",
            "value": 564965772,
            "unit": "ns"
          },
          {
            "name": "write objects local local true 1000 largestConversionNs median",
            "value": 161548,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 wallNs median",
            "value": 899981569,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 gapNs median",
            "value": 52038192,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainNs median",
            "value": 32698681,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 mainTickMaxNs median",
            "value": 1973381,
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
            "value": 32693741,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultMainNs median",
            "value": 5695,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 resultAsyncNs median",
            "value": 3958809,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 executionNs median",
            "value": 5648812,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 queueWaitNs median",
            "value": 808617387,
            "unit": "ns"
          },
          {
            "name": "read objects local local true 1000 largestConversionNs median",
            "value": 171817,
            "unit": "ns"
          }
        ]
      }
    ]
  }
}