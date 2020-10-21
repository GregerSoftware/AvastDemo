package cz.gregetom.avastdemo.api.handler

import cz.gregetom.avastdemo.api.handler.parent.RequestHandlerWithBodyParent
import cz.gregetom.avastdemo.api.to.UrlValidationRequestListTO
import cz.gregetom.avastdemo.service.ValidationRequestService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

/**
 * Handler for validation POST method
 */
@Component
class PostValidationHandler(
        private val validationRequestService: ValidationRequestService,
        // hateoas?
        @Value("\${avast-demo.uri-template.validation.get}") private val validationGetUriTemplate: String
) : RequestHandlerWithBodyParent<UrlValidationRequestListTO>(UrlValidationRequestListTO::class) {

    override fun handle(request: ServerRequest, requestBody: Mono<UrlValidationRequestListTO>): Mono<ServerResponse> {
        return requestBody
                .flatMap { validationRequestService.saveValidationRequest(it) }
                .flatMap { requestId -> created(buildGetValidationUri(requestId)).build() }
    }

    private fun buildGetValidationUri(requestId: String): URI {
        return UriComponentsBuilder.fromUriString(validationGetUriTemplate).buildAndExpand(requestId).toUri()
    }
}
