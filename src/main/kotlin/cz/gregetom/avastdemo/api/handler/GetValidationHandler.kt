package cz.gregetom.avastdemo.api.handler

import cz.gregetom.avastdemo.api.handler.parent.RequestHandlerParent
import cz.gregetom.avastdemo.api.to.UrlValidationResponseTO
import cz.gregetom.avastdemo.service.ValidationRequestService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.accepted
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

/**
 * Handler for validation GET method
 */
@Component
class GetValidationHandler(
        private val validationRequestService: ValidationRequestService
) : RequestHandlerParent {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        return validationRequestService.getResult(request.pathVariable("requestId"))
                .flatMap { ok().body(Mono.just(it), UrlValidationResponseTO::class.java) }
                .switchIfEmpty(accepted().build())
    }
}
