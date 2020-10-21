package cz.gregetom.avastdemo.util

import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

object MockServerUtil {

    fun registerMockedPath(mockServer: ClientAndServer, path: String, httpStatus: HttpStatus) {
        mockServer
                .`when`(
                        HttpRequest.request()
                                .withMethod(HttpMethod.HEAD.name)
                                .withPath(path),
                        Times.once()
                ).respond(
                        HttpResponse.response().withStatusCode(httpStatus.value())
                )
    }
}