package cz.gregetom.avastdemo.api.handler.parent

import cz.gregetom.avastdemo.filter.logging.LoggingContext
import cz.gregetom.avastdemo.interceptor.RequestBodyInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

/**
 * Parent for request handlers with request body
 */
abstract class RequestHandlerWithBodyParent<T : Any>(private val requestBodyClass: KClass<T>) : RequestHandlerParent {

    @Autowired
    private lateinit var requestBodyInterceptors: List<RequestBodyInterceptor>

    /**
     * Handle [request] with [requestBody]
     * @param request
     * @param requestBody from [request]
     * @return publisher with response
     */
    protected abstract fun handle(request: ServerRequest, requestBody: Mono<T>): Mono<ServerResponse>

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val requestBody = request
                .bodyToMono(requestBodyClass.java)
                .doOnNext { requestBody -> performRequestBodyInterceptors(request, requestBody) }

        return handle(request, requestBody)
    }

    /**
     * Perform actions with [requestBody], i.e. validation, logging, etc.
     */
    private fun performRequestBodyInterceptors(request: ServerRequest, requestBody: T) {
        requestBodyInterceptors.forEach {
            it.intercept(requestBody, LoggingContext.from(request.exchange()))
        }
    }
}