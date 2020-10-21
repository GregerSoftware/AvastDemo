package cz.gregetom.avastdemo.api.handler.parent

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * Parent for request handlers
 */
interface RequestHandlerParent {

    /**
     * Handle [request]
     * @param request
     * @return publisher with response
     */
    fun handle(request: ServerRequest): Mono<ServerResponse>
}