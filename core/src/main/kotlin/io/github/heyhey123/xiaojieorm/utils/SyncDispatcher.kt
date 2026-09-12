package io.github.heyhey123.xiaojieorm.utils

import io.github.heyhey123.xiaojieorm.XiaojieOrm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

/**
 * Sync dispatcher that runs tasks on the main server thread.
 *
 * @constructor Create empty Sync dispatcher
 */
object SyncDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Bukkit.getScheduler().runTask(XiaojieOrm.instance, block)
        }
    }
}
