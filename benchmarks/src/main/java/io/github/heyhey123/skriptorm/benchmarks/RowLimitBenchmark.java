package io.github.heyhey123.skriptorm.benchmarks;

import io.github.heyhey123.skriptorm.skript.utils.RowLimit;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * The suite's smoke case, and deliberately the least interesting one: it measures the arithmetic that
 * decides how many rows one write statement may carry, which runs once per write and is invisible next
 * to the statement it precedes.
 *
 * <p>Its job is to fail loudly if the harness is broken — the module compiling, the annotation
 * processor generating the benchmark list, JMH forking a JVM, the JSON report landing on disk. Every
 * case that replaces it in the verdict has to clear that bar first, so the harness is proven before
 * anything is measured with it.
 *
 * <p>{@code RowLimit} is {@code internal} in Kotlin and public in the class file, which is why this
 * case can call it: {@code internal} is enforced by the Kotlin compiler, not by the JVM. The widths
 * are the ones the plugin's own tables use, from a single identifier up to a wide row.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(1)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class RowLimitBenchmark {

    private int[] widths;

    @Setup
    public void setUp() {
        widths = new int[] {1, 2, 3, 4, 6, 8, 12, 32};
    }

    @Benchmark
    public void rowsPerStatement(Blackhole hole) {
        int total = 0;
        for (int width : widths) {
            total += RowLimit.INSTANCE.rowsPerStatement(width);
        }
        hole.consume(total);
    }
}
