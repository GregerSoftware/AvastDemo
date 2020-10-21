package cz.gregetom.avastdemo.filter.logging

import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Signal

/**
 * Logging context holder.
 */
data class LoggingContext(
        val userIdentity: String,
        val tracingId: String
) {
    companion object {
        fun from(exchange: ServerWebExchange): LoggingContext {
            return exchange.getAttribute<LoggingContext>(LOGGING_CONTEXT_ATTRIBUTE)
                    ?: throw IllegalStateException("Logging context is empty")
        }

        fun from(signal: Signal<*>): LoggingContext {
            return signal.context.getOrEmpty<LoggingContext>(LOGGING_CONTEXT_ATTRIBUTE)
                    .orElseThrow { IllegalStateException("Logging context is empty") }
        }

        const val LOGGING_CONTEXT_ATTRIBUTE = "context"
    }
}