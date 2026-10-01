package io.github.heyhey123.skriptorm.benchmarks;

import io.github.heyhey123.skriptorm.result.WriteResult;
import io.github.heyhey123.skriptorm.table.Table;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * One `insert many` of five thousand rows through the plugin's own path, against a database that runs
 * inside this process.
 *
 * The size is the one the user's MySQL benchmark used, and the table is six columns wide, so the call
 * carries exactly the 30 000 values the plugin puts in a single statement before it splits. Both
 * numbers that explained the MySQL gap are reported per call, because wall time cannot distinguish
 * one statement carrying five thousand rows from five thousand statements carrying one row: the rows
 * the plugin asked for, and the statements it handed to the driver.
 *
 * The plugin's own answer is reported too — what it counted and whether it claims the count is exact —
 * and the rows are counted again outside the plugin, since a driver's report of what it did is not
 * evidence that the rows exist.
 *
 * The table is cleared before every invocation rather than once per iteration, because the key is
 * supplied by the caller: JMH runs the method many times inside one iteration, and the same five
 * thousand keys cannot be inserted twice. Clearing once per iteration would bound the table's growth
 * but not its identity, and the second call would fail on the primary key. JMH excludes invocation
 * setup from the measured interval, and a delete of five thousand rows costs a few milliseconds against
 * a call that takes tens, so this buys a table that is exactly as empty at the start of every measured
 * call as the user's own benchmark was at the start of its run.
 *
 * H2 runs in memory here rather than through a container, which is the only database this machine can
 * reach: it has no container runtime, and the integration tests need MySQL, MariaDB and PostgreSQL
 * containers.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(1)
@State(Scope.Benchmark)
public class InsertManyBenchmark {

    /** The size of the user's own benchmark, and the plugin's value budget for a six-column row. */
    private static final int ROWS_PER_CALL = 5000;

    private static final String PLAIN_URL = "jdbc:h2:mem:skriptorm_bench;DB_CLOSE_DELAY=-1";

    /** The plugin sees this url and the counting driver; the driver strips the prefix. */
    private static final String COUNTING_URL = CountingDriver.URL_PREFIX + PLAIN_URL;

    private Table table;

    private List<Map<String, Object>> rows;

    /** How the declared shape was refused, or null when a declaration was accepted as written. */
    private String declarationRefusal;

    private long lastRowsAskedFor;

    private long lastStatementsExecuted;

    private long lastReportedCount;

    private boolean lastCountExact;

    /** How many rows the last clear removed: 0 for the first call, 5000 afterwards. */
    private long lastDeleted;

    @Setup(Level.Trial)
    public void connectAndRegister() {
        truncateReport();
        PluginBridge.connect(COUNTING_URL, CountingDriver.class.getName(), PLAIN_URL);

        Table declared = PluginBridge.declaredTable();
        table = declared;
        try {
            PluginBridge.registerTable(declared);
            report("declaration", "auto-increment primary key accepted as written");
        } catch (RuntimeException refused) {
            declarationRefusal = refused.getClass().getName() + ": " + refused.getMessage();
            report("declaration", "auto-increment primary key refused -> " + declarationRefusal);
            table = PluginBridge.suppliedIdTable();
            PluginBridge.registerTable(table);
            report("declaration", "registered instead: " + table.getName() + " with the key supplied");
        }

        rows = PluginBridge.rows(ROWS_PER_CALL);
        report("table", table.getName() + ", " + ROWS_PER_CALL + " rows per call, "
            + table.getColumns().size() + " columns");
    }

    @Setup(Level.Invocation)
    public void clearTableAndCounters() {
        long deleted = PluginBridge.clear(table);
        CountingDriver.Counters.reset();
        // Reported once per iteration below as well; this is the line that proves every measured call
        // started from an empty table rather than from the last one's rows.
        lastDeleted = deleted;
    }

    @Benchmark
    public void insertManyOf5000(Blackhole blackhole) {
        long rowsBefore = CountingDriver.Counters.rowsAskedFor();
        long statementsBefore = CountingDriver.Counters.statementsExecuted();

        WriteResult result = PluginBridge.insertMany(table, rows);

        lastRowsAskedFor = CountingDriver.Counters.rowsAskedFor() - rowsBefore;
        lastStatementsExecuted = CountingDriver.Counters.statementsExecuted() - statementsBefore;
        lastReportedCount = result.getAffectedCount();
        lastCountExact = result.getCountExact();

        // A benchmark method that returns nothing still has to consume its result, or the call is dead
        // code to the JIT. The insert has side effects and would survive anyway; this states it.
        blackhole.consume(result);
    }

    @TearDown(Level.Iteration)
    public void reportIteration() {
        long stored = PluginBridge.count(table);
        report("iteration", "plugin reported " + lastReportedCount + " affected (exact=" + lastCountExact + ")"
            + ", rows asked for " + lastRowsAskedFor
            + ", statements executed " + lastStatementsExecuted
            + ", connections opened " + CountingDriver.Counters.connectionsOpened()
            + ", cleared before the call " + lastDeleted
            + ", rows in table afterwards " + stored);
    }

    @TearDown(Level.Trial)
    public void disconnect() {
        PluginBridge.disconnect();
    }

    /**
     * Writes where the numbers can be read after the run. JMH forks a JVM per run, so counters that
     * live in the forked process would otherwise be invisible from the build that started it; each
     * iteration appends a line, which also shows that clearing between iterations left the counters
     * starting from zero every time.
     */
    private static void report(String label, String message) {
        String line = "[insert-many] " + label + ": " + message;
        System.out.println(line);
        Path target = Paths.get(
            System.getProperty("benchmarks.countersFile", "build/benchmarks/insert-many-counters.txt")
        );
        try {
            Files.createDirectories(target.getParent());
            Files.write(
                target,
                (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException failure) {
            System.out.println("[insert-many] could not write " + target + ": " + failure);
        }
    }

    private static void truncateReport() {
        Path target = Paths.get(
            System.getProperty("benchmarks.countersFile", "build/benchmarks/insert-many-counters.txt")
        );
        try {
            Files.deleteIfExists(target);
        } catch (IOException failure) {
            System.out.println("[insert-many] could not clear " + target + ": " + failure);
        }
    }
}
