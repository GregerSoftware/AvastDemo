package cz.gregetom.avastdemo.filter.logging

import cz.gregetom.avastdemo.api.CustomHttpHeaders
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

/**
 * Enable logging with context.
 * Determine mandatory context properties from request headers or generate new ones.
 */
@Component
@Order(1)
class LoggingContextWebFilter : WebFilter {

    override fun filter(serverWebExchange: ServerWebExchange, webFilterChain: WebFilterChain): Mono<Void> {
        val loggingContext = createLoggingContext(serverWebExchange)

        serverWebExchange.attributes[LoggingContext.LOGGING_CONTEXT_ATTRIBUTE] = loggingContext

        return webFilterChain.filter(serverWebExchange)
                .subscriberContext { it.put(LoggingContext.LOGGING_CONTEXT_ATTRIBUTE, loggingContext) }
    }

    private fun createLoggingContext(serverWebExchange: ServerWebExchange): LoggingContext {
        val userIdentity = serverWebExchange.request.headers[CustomHttpHeaders.USER_IDENTITY_HEADER]?.first()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "${CustomHttpHeaders.USER_IDENTITY_HEADER} header most not be null")

        val tracingId = serverWebExchange.request.headers[CustomHttpHeaders.TRACING_ID_HEADER]?.first()
                ?: UUID.randomUUID().toString()

        return LoggingContext(
                userIdentity = userIdentity,
                tracingId = tracingId
        )
    }
}
