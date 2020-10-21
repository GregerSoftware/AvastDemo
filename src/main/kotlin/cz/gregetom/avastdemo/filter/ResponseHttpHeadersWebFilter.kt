package cz.gregetom.avastdemo.filter

import cz.gregetom.avastdemo.api.CustomHttpHeaders
import cz.gregetom.avastdemo.filter.logging.LoggingContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Set response http headers, i.e. tracingId, userIdentity,...
 */
@Component
@Order(2)
class ResponseHttpHeadersWebFilter : WebFilter {

    override fun filter(serverWebExchange: ServerWebExchange, webFilterChain: WebFilterChain): Mono<Void> {
        val context = LoggingContext.from(serverWebExchange)

        // add response headers
        serverWebExchange.response.headers.add(CustomHttpHeaders.USER_IDENTITY_HEADER, context.userIdentity)
        serverWebExchange.response.headers.add(CustomHttpHeaders.TRACING_ID_HEADER, context.tracingId)

        return webFilterChain.filter(serverWebExchange)
    }
}
