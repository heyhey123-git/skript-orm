package io.github.heyhey123.skriptorm.benchmarks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.LongAdder;

/**
 * A JDBC driver that wraps another one and counts what the plugin asks of it.
 *
 * <p>It exists because the two numbers that explained the MySQL finding are invisible in wall time:
 * the plugin submitted <em>one</em> batch and asked for <em>five thousand</em> rows, and whether those
 * rows leave the process as one statement or five thousand is a decision the driver makes. A clock
 * cannot tell the two apart; a count can, and a count is deterministic, so it can be a gate rather
 * than a report.
 *
 * <p>Why a {@link Driver} and not a {@code DataSource} proxy: the plugin builds its own pool from a
 * url and a driver class name ({@code JdbcDatabase.doConnect} hands both to Hikari), so nothing can be
 * inserted between the plugin and its pool without editing production code. A driver, by contrast, is
 * selected by the url alone — {@code jdbc:counting:<real url>} — and named through the connection
 * property the plugin already has for it ({@code driver}, {@code JdbcDatabaseFactory.DRIVER_PROPERTY}).
 * That keeps the whole measurement inside {@code benchmarks/}.
 *
 * <p>What it cannot see is worth stating: it sits at the JDBC API boundary, so it counts what the
 * plugin asks the driver for. Whether the driver then sends one statement or five thousand to the
 * server is decided below this line — by Connector/J's {@code rewriteBatchedStatements}, for example —
 * and is not visible here. Reading the counts therefore answers "what did the plugin ask for", and
 * only a server-side or protocol-level count can answer "what did the server execute".
 *
 * <p>Usage: {@code jdbc:counting:jdbc:h2:mem:x;DB_CLOSE_DELAY=-1}, with {@code driver} left as the
 * plugin's own property. A driver that is not discoverable through {@link DriverManager} can be named
 * in a {@code realDriver} property, which is stripped before the real driver sees it.
 */
public final class CountingDriver implements Driver {

    /** Everything after this belongs to the driver being wrapped. */
    public static final String URL_PREFIX = "jdbc:counting:";

    /** Names the class of the driver to delegate to, when {@link DriverManager} cannot find it itself. */
    public static final String REAL_DRIVER_PROPERTY = "realDriver";

    static {
        try {
            DriverManager.registerDriver(new CountingDriver());
        } catch (SQLException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            // The contract: a driver answers null for a url that is not its own, so the next one is asked.
            return null;
        }

        String realUrl = url.substring(URL_PREFIX.length());
        Properties forwarded = new Properties();
        if (info != null) {
            forwarded.putAll(info);
        }
        String named = forwarded.getProperty(REAL_DRIVER_PROPERTY);
        forwarded.remove(REAL_DRIVER_PROPERTY);

        if (named != null && !named.isEmpty()) {
            try {
                Class.forName(named);
            } catch (ClassNotFoundException missing) {
                throw new SQLException("The driver '" + named + "' named by '" + REAL_DRIVER_PROPERTY + "' is not on the classpath.", missing);
            }
        }

        Connection delegate = DriverManager.getConnection(realUrl, forwarded);
        Counters.CONNECTIONS_OPENED.increment();
        return proxy(Connection.class, delegate);
    }

    @Override
    public boolean acceptsURL(String url) {
        return url != null && url.startsWith(URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getLogger(CountingDriver.class.getName());
    }

    /**
     * Wraps what a statement call returned: another statement is wrapped so its calls are counted too,
     * everything else is handed back untouched. A result set is deliberately not wrapped — reading rows
     * is not what this measures, and the extra proxy would land inside the timing.
     */
    private static Object wrapResult(Object result) {
        if (result instanceof PreparedStatement) {
            return proxy(PreparedStatement.class, result);
        }
        if (result instanceof Statement) {
            return proxy(Statement.class, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Object delegate) {
        return (T) Proxy.newProxyInstance(
            CountingDriver.class.getClassLoader(),
            new Class<?>[] {type},
            new CountingHandler(delegate)
        );
    }

    /**
     * Forwards every call and counts the ones that say something about how much work was asked for.
     *
     * <p>{@code addBatch} is a row asked for. Everything named {@code execute} on a statement is a
     * statement handed over: this is where a batch of five thousand rows and a single-row prepared
     * statement would look the same, which is exactly why the batch call count is kept separately.
     */
    private static final class CountingHandler implements InvocationHandler {

        private final Object delegate;

        CountingHandler(Object delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            int arity = args == null ? 0 : args.length;

            boolean batch = "addBatch".equals(name) && (arity == 0 || arity == 1);
            if (batch) {
                Counters.ROWS_ASKED_FOR.increment();
            } else if (isExecution(name)) {
                Counters.STATEMENTS_EXECUTED.increment();
            }

            Object result;
            try {
                result = method.invoke(delegate, args);
            } catch (InvocationTargetException wrapped) {
                throw wrapped.getTargetException();
            }
            return wrapResult(result);
        }

        private static boolean isExecution(String name) {
            switch (name) {
                case "execute":
                case "executeQuery":
                case "executeUpdate":
                case "executeLargeUpdate":
                case "executeBatch":
                case "executeLargeBatch":
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * The counters, read at rest: the benchmark reads them inside a single-threaded call and resets
     * them between iterations, so they are never sampled while another thread is adding to them.
     */
    public static final class Counters {

        /** {@code addBatch} calls: rows the plugin asked to have written. */
        public static final LongAdder ROWS_ASKED_FOR = new LongAdder();

        /** {@code execute*} calls on any statement: statements the plugin asked the driver to run. */
        public static final LongAdder STATEMENTS_EXECUTED = new LongAdder();

        /** Physical connections the driver was asked to open, so pool churn is visible. */
        public static final LongAdder CONNECTIONS_OPENED = new LongAdder();

        private Counters() {
        }

        public static void reset() {
            ROWS_ASKED_FOR.reset();
            STATEMENTS_EXECUTED.reset();
            CONNECTIONS_OPENED.reset();
        }

        public static long rowsAskedFor() {
            return ROWS_ASKED_FOR.sum();
        }

        public static long statementsExecuted() {
            return STATEMENTS_EXECUTED.sum();
        }

        public static long connectionsOpened() {
            return CONNECTIONS_OPENED.sum();
        }
    }
}
