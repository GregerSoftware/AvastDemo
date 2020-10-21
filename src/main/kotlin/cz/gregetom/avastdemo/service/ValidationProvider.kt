package cz.gregetom.avastdemo.service

import cz.gregetom.avastdemo.db.FakeDatabase
import cz.gregetom.avastdemo.db.UrlValidationRequest
import cz.gregetom.avastdemo.extension.PublisherExtensions.logOnError
import cz.gregetom.avastdemo.extension.PublisherExtensions.logOnNext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.util.retry.Retry
import java.time.Duration
import java.time.OffsetDateTime

/**
 * URLs validation provider.
 */
@Service
class ValidationProvider(
        private val fakeDatabase: FakeDatabase,
        private val webClient: WebClient
) {

    /**
     * Validate urls of request with [requestId].
     * Test [UrlValidationRequest.urlEntries] in parallel.
     */
    fun validate(requestId: String): Flux<UrlValidationRequest> {
        return fakeDatabase.get(requestId)
                .logOnNext { LOG.info("Start request $requestId validation") }
                .flatMapIterable { it.urlEntries }
                .parallel()
                .flatMap { performHttpCall(requestId, it) }
                .toFlux()
                .doOnComplete { computeRequestResult(requestId) }
    }


    /**
     * Perform [urlEntry] test with HTTP HEAD method. Try it 3 times.
     */
    private fun performHttpCall(requestId: String, urlEntry: UrlValidationRequest.UrlEntry): Mono<UrlValidationRequest> {
        return webClient.head()
                .uri(urlEntry.url.toURI())
                .exchange()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .logOnNext { LOG.info("${urlEntry.url} is valid") }
                .flatMap { setResultToValidationRequestEntry(requestId, urlEntry, it.statusCode().is2xxSuccessful) }
                .logOnError { LOG.info("${urlEntry.url} is invalid") }
                .onErrorResume { setResultToValidationRequestEntry(requestId, urlEntry, false) }
    }

    private fun setResultToValidationRequestEntry(requestId: String, originalEntry: UrlValidationRequest.UrlEntry, result: Boolean): Mono<UrlValidationRequest> {
        val updatedEntry = originalEntry.copy(result = result, validatedOn = OffsetDateTime.now(), threadId = Thread.currentThread().name)
        return fakeDatabase.updateEntry(requestId, updatedEntry)
    }

    private fun computeRequestResult(requestId: String): Disposable {
        return fakeDatabase.get(requestId)
                .map { request ->
                    request.copy(
                            done = true,
                            result = request.urlEntries.map { it.result!! }.reduce { a, b -> a && b },
                            validatedOn = OffsetDateTime.now()
                    )
                }
                .flatMap { fakeDatabase.save(it) }
                .subscribe()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ValidationProvider::class.java)
    }
}