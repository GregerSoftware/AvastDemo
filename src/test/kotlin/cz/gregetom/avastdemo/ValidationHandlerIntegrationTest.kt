package cz.gregetom.avastdemo

import cz.gregetom.avastdemo.api.CustomHttpHeaders
import cz.gregetom.avastdemo.api.to.UrlValidationRequestListTO
import cz.gregetom.avastdemo.api.to.UrlValidationRequestTO
import cz.gregetom.avastdemo.api.to.UrlValidationResponseTO
import cz.gregetom.avastdemo.util.MockServerUtil
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.verify.VerificationTimes
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import java.net.URI
import java.net.URL
import kotlin.reflect.KClass

@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [AvastDemoApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidationHandlerIntegrationTest {

    private lateinit var serverBaseUrl: String
    private lateinit var webTestClient: WebTestClient

    @Value("\${mock-server.port:2001}")
    private var mockServerPort: Int = 0
    private lateinit var mockServerBaseUrl: String
    private lateinit var mockServer: ClientAndServer

    @LocalServerPort
    private val port = 0


    @Before
    fun setUp() {
        mockServerBaseUrl = "http://localhost:$mockServerPort"
        mockServer = ClientAndServer.startClientAndServer(mockServerPort)
        serverBaseUrl = "http://localhost:$port"
        webTestClient = WebTestClient.bindToServer().baseUrl(serverBaseUrl).build()
    }

    @After
    fun after() {
        mockServer.stop()
    }


    @Test
    fun testBadRequest() {
        val invalidRequestBody = UrlValidationRequestListTO(urlValidationRequestList = emptyList())
        invokePostMethod(invalidRequestBody)
                .expectStatus()
                .isBadRequest
    }


    @Test
    fun testRequestNotFound() {
        invokeGetMethod(URI("$serverBaseUrl/test"))
                .expectStatus()
                .isNotFound
    }


    @Test
    fun testRedirect() {
        mockServer
                .`when`(
                        request()
                                .withMethod(HttpMethod.HEAD.name)
                                .withPath("/tmp"),
                        Times.once()
                ).respond(
                        response()
                                .withStatusCode(HttpStatus.PERMANENT_REDIRECT.value())
                                .withHeader(HttpHeaders.LOCATION, "$mockServerBaseUrl/tmp-redirect")
                )
        MockServerUtil.registerMockedPath(mockServer, "/tmp-redirect", HttpStatus.OK)

        val requestBody = UrlValidationRequestListTO(
                urlValidationRequestList = listOf(
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/tmp"))
                )
        )

        val location = invokePostMethodAndReturnLocation(requestBody)
        awaitUntilStatusIsOk { invokeGetMethod(location) }

        val responseBody = invokeGetMethodAndReturnBody(location, UrlValidationResponseTO::class)
        assertTrue(responseBody.result)

        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/tmp"), VerificationTimes.once())
        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/tmp-redirect"), VerificationTimes.once())
    }


    @Test
    fun testRetry() {
        MockServerUtil.registerMockedPath(mockServer, "/tmp", HttpStatus.BAD_REQUEST)
        MockServerUtil.registerMockedPath(mockServer, "/tmp", HttpStatus.OK)

        val requestBody = UrlValidationRequestListTO(
                urlValidationRequestList = listOf(
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/tmp"))
                )
        )

        val location = invokePostMethodAndReturnLocation(requestBody)
        awaitUntilStatusIsOk { invokeGetMethod(location) }

        val responseBody = invokeGetMethodAndReturnBody(location, UrlValidationResponseTO::class)
        assertTrue(responseBody.result)

        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/tmp"), VerificationTimes.exactly(2))
    }


    @Test
    fun testValid() {
        MockServerUtil.registerMockedPath(mockServer, "/tmp", HttpStatus.OK)
        MockServerUtil.registerMockedPath(mockServer, "/my-path", HttpStatus.ACCEPTED)

        val requestBody = UrlValidationRequestListTO(
                urlValidationRequestList = listOf(
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/tmp")),
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/my-path"))
                )
        )

        val location = invokePostMethodAndReturnLocation(requestBody)
        awaitUntilStatusIsOk { invokeGetMethod(location) }

        val responseBody = invokeGetMethodAndReturnBody(location, UrlValidationResponseTO::class)
        assertTrue(responseBody.result)
        assertTrue(responseBody.urlEntries.map { it.result }.reduce { a, b -> a && b })

        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/tmp"), VerificationTimes.once())
        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/my-path"), VerificationTimes.once())
    }


    @Test
    fun testInvalid() {
        MockServerUtil.registerMockedPath(mockServer, "/tmp", HttpStatus.OK)
        MockServerUtil.registerMockedPath(mockServer, "/my-path", HttpStatus.NO_CONTENT)
        MockServerUtil.registerMockedPath(mockServer, "/invalid-path", HttpStatus.NOT_FOUND)

        val requestBody = UrlValidationRequestListTO(
                urlValidationRequestList = listOf(
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/tmp")),
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/my-path")),
                        UrlValidationRequestTO(URL("$mockServerBaseUrl/invalid-path"))
                )
        )

        val location = invokePostMethodAndReturnLocation(requestBody)
        awaitUntilStatusIsOk { invokeGetMethod(location) }

        val responseBody = invokeGetMethodAndReturnBody(location, UrlValidationResponseTO::class)
        assertFalse(responseBody.result)
        assertTrue(responseBody.urlEntries.first { it.url.toString() == "$mockServerBaseUrl/tmp" }.result)
        assertTrue(responseBody.urlEntries.first { it.url.toString() == "$mockServerBaseUrl/my-path" }.result)
        assertFalse(responseBody.urlEntries.first { it.url.toString() == "$mockServerBaseUrl/invalid-path" }.result)

        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/tmp"), VerificationTimes.once())
        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/my-path"), VerificationTimes.once())
        mockServer.verify(request().withMethod(HttpMethod.HEAD.name).withPath("/invalid-path"), VerificationTimes.exactly(4))
    }


    @Test
    fun testParallel() {
        IntRange(1, 10).forEach {
            MockServerUtil.registerMockedPath(mockServer, "/tmp$it", HttpStatus.OK)
        }

        Flux.range(1, 10)
                .parallel(3)
                .runOn(Schedulers.boundedElastic())
                .doOnNext {
                    println("Client $it call on thread ${Thread.currentThread().name}")
                    performClientRequest("/tmp$it")
                }
                .toFlux()
                .blockLast()
    }

    private fun performClientRequest(path: String) {
        val requestBody = UrlValidationRequestListTO(
                urlValidationRequestList = listOf(
                        UrlValidationRequestTO(URL("$mockServerBaseUrl$path"))
                )
        )

        val location = invokePostMethodAndReturnLocation(requestBody)
        awaitUntilStatusIsOk { invokeGetMethod(location) }

        val responseBody = invokeGetMethodAndReturnBody(location, UrlValidationResponseTO::class)
        assertTrue(responseBody.result)
    }

    private fun awaitUntilStatusIsOk(invocation: () -> WebTestClient.ResponseSpec) {
        await()
                .atLeast(Duration.ONE_HUNDRED_MILLISECONDS)
                .atMost(Duration.TEN_SECONDS)
                .with()
                .pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS)
                .until {
                    HttpStatus.OK === invocation().returnResult(ServerRequest::class.java).status
                }
    }

    private fun invokePostMethod(requestBody: UrlValidationRequestListTO): WebTestClient.ResponseSpec {
        return webTestClient
                .post()
                .uri("/validation")
                .header(CustomHttpHeaders.USER_IDENTITY_HEADER, "test-user")
                .body(BodyInserters.fromValue(requestBody))
                .exchange()
    }

    private fun invokePostMethodAndReturnLocation(requestBody: UrlValidationRequestListTO): URI {
        return invokePostMethod(requestBody)
                .expectStatus().isCreated
                .expectHeader().valueMatches(HttpHeaders.LOCATION, "http://localhost:8080/validation/[^/]+")
                .returnResult(ServerRequest::class.java).responseHeaders.location!!
    }

    private fun invokeGetMethod(location: URI): WebTestClient.ResponseSpec {
        return webTestClient
                .get()
                .uri(location.path)
                .header(CustomHttpHeaders.USER_IDENTITY_HEADER, "test-user")
                .exchange()
    }

    private fun <T : Any> invokeGetMethodAndReturnBody(location: URI, requestBodyClass: KClass<T>): T {
        return invokeGetMethod(location)
                .expectStatus()
                .isOk
                .expectBody(requestBodyClass.java)
                .returnResult()
                .responseBody ?: throw IllegalStateException("RequestBody must not be null")
    }
}