package cz.gregetom.avastdemo.util

import cz.gregetom.avastdemo.filter.logging.LoggingContext
import org.slf4j.MDC

object LoggingUtil {

    /**
     * Allow logging with MDC context.
     */
    fun logWithContext(logFunction: () -> Unit, context: LoggingContext) {
        MDC.put(LoggingContext.LOGGING_CONTEXT_ATTRIBUTE, context.toString())
        try {
            logFunction()
        } finally {
            MDC.clear()
        }
    }
}