package io.github.heyhey123.skriptorm.type

/** Schedules conversions that need the server thread. Installed for the plugin's lifetime. */
object ConversionWork {
    interface Executor {
        suspend fun <T> executeBatch(actions: List<() -> T>): List<T>
    }

    @Volatile
    private var executor: Executor? = null

    fun install(executor: Executor?) {
        this.executor = executor
    }

    suspend fun <T> execute(action: () -> T): T = executeBatch(listOf(action)).single()

    /** Standalone query tests run without Bukkit; the enabled plugin always installs an executor. */
    suspend fun <T> executeBatch(actions: List<() -> T>): List<T> =
        executor?.executeBatch(actions) ?: actions.map { it() }
}
