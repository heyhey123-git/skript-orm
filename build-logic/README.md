# Build logic

The root build applies five convention plugins. Module builds apply `skript-orm.module` for
the shared repositories, coordinates, JVM toolchain, formatting and test settings.

| Plugin | Responsibility |
| --- | --- |
| `skript-orm.packaging` | Select implementation modules and build the relocated plugin jar. |
| `skript-orm.server-tests` | Prepare and run the Paper self-tests, then check the log. |
| `skript-orm.server-benchmarks` | Run the Skript benchmarks and write the existing JSON reports. |
| `skript-orm.documentation` | Run SkriptHubDocsTool and validate the generated syntax documentation. |
| `skript-orm.release-notes` | Extract the current version's CHANGELOG section. |

`skript-orm.server-runtime` supplies the Paper coordinates and Skript dependency shared by the
three server workflows. `buildlogic.tasks` contains their task implementations;
`ServerTestExpectations` contains the existing self-test verdicts.

Maven dependency and Gradle plugin versions come from the root `gradle/libs.versions.toml`. The included
build imports that catalog rather than keeping a second copy. `BundleConfiguration` supplies
the same module selection to packaging, server tests and `core`'s `plugin.yml` expansion.

Task names, command-line properties and output paths match the previous root script. Server
runs deliberately execute every time; splitting the build does not cache database tests or
benchmark measurements. Configuration-cache support for the server preparation tasks is a
separate concern.

The application modules use JDK 25. Build plugins target Java 17 so they can also be loaded when a
developer starts Gradle on JDK 17 or 21; Gradle still selects JDK 25 for compiling and testing the app.

The shaded jar includes implementation modules through ordinary project dependencies. It does not
copy their source-set outputs again or force early project evaluation. Driver jars are still loaded
by Paper from `plugin.yml`, while Kotlin, coroutines and HikariCP remain bundled and relocated.

Run `./gradlew ktlintCheck :build-logic:check` to check formatting in both builds and validate the
build plugins. CI runs the same checks.
