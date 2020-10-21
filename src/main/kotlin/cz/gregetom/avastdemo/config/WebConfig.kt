package cz.gregetom.avastdemo.config

import cz.gregetom.avastdemo.api.handler.GetValidationHandler
import cz.gregetom.avastdemo.api.handler.PostValidationHandler
import cz.gregetom.avastdemo.exception.CustomClientException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

@Configuration
class WebConfig {

    @Bean
    fun functionalRoute(
            handlerGet: GetValidationHandler,
            handlerPost: PostValidationHandler
    ): RouterFunction<ServerResponse> {
        return router {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/validation/{requestId}", handlerGet::handle)
                POST("/validation", handlerPost::handle)
            }
        }
    }

    /**
     * Configure [WebClient] - follow redirects and return error, when response has status 4xx or 5xx
     */
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
                .clientConnector(
                        ReactorClientHttpConnector(
                                HttpClient.create().followRedirect(true)
                        )
                )
                .filter(ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
                    if (clientResponse.statusCode().isError) {
                        clientResponse.bodyToMono(ClientResponse::class.java)
                                .flatMap { Mono.error<ClientResponse> { CustomClientException(it.statusCode()) } }
                    } else {
                        Mono.just(clientResponse)
                    }
                })
                .build()
    }
}
