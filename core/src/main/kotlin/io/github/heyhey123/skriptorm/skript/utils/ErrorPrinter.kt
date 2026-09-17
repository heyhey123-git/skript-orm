package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Trigger
import io.github.heyhey123.skriptorm.SkriptOrm
import java.util.logging.Level
import java.util.logging.Logger

object ErrorPrinter {

    private val logger: Logger
        get() = SkriptOrm.instance.logger

    /** Logs [error] with the trigger and source location when available. */
    fun printErrorWithDetail(trigger: Trigger, error: Throwable) {
        logger.severe("An error occurred while executing the Skript trigger:")
        logger.severe("In trigger: ${trigger.name}")
        if (trigger.script != null) {
            logger.severe("Line: ${trigger.lineNumber} ")
            logger.severe("In script: ${trigger.script!!.nameAndPath()} ")
        }
        logger.log(Level.SEVERE, "Error details:", error)
    }

    /**
     * Prints an error message with detailed information about the Skript trigger and the error message that occurred.
     *
     * @param trigger
     * @param message
     */
    fun printErrorMessageWithDetail(trigger: Trigger, message: String) {
        logger.severe("An error occurred while executing the Skript trigger:")
        logger.severe("In trigger: ${trigger.name}")
        if (trigger.script != null) {
            logger.severe("Line: ${trigger.lineNumber} ")
            logger.severe("In script: ${trigger.script!!.nameAndPath()} ")
        }
        logger.severe("Error message: $message")
    }
}
