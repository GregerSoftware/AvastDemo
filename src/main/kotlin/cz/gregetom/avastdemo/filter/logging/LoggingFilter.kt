package cz.gregetom.avastdemo.filter.logging

import cz.gregetom.avastdemo.util.LoggingUtil
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Filter for logging incoming requests and outgoing responses.
 */
@Component
class LoggingFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        logRequest(exchange.request, LoggingContext.from(exchange))

        val filter = chain.filter(exchange)

        exchange.response.beforeCommit {
            logResponse(exchange.response, LoggingContext.from(exchange))
            Mono.empty()
        }

        return filter
    }

    private fun logRequest(request: ServerHttpRequest, context: LoggingContext) {
        val builder = StringBuilder()
                .append("***** HTTP REQUEST *****")
                .append("\n")
                .append("Headers: ${request.headers.map { "${it.key}:${it.value}" }}")
                .append("\n")
                .append("************************")
                .append("\n")

        LoggingUtil.logWithContext({ LOG.debug(builder.toString()) }, context)
    }

    private fun logResponse(response: ServerHttpResponse, context: LoggingContext) {
        val builder = StringBuilder()
                .append("***** HTTP RESPONSE *****")
                .append("\n")
                .append("Status code: ${response.statusCode}")
                .append("\n")
                .append("Headers: ${response.headers.map { "${it.key}:${it.value}" }}")
                .append("\n")
                .append("*************************")
                .append("\n")

        LoggingUtil.logWithContext({ LOG.debug(builder.toString()) }, context)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LoggingFilter::class.java)
    }
}