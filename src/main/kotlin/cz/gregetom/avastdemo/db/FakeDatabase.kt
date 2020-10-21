package cz.gregetom.avastdemo.db

import cz.gregetom.avastdemo.extension.PublisherExtensions.logOnNext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Naive implementation of "data store"
 */
@Component
class FakeDatabase {

    private val db = ConcurrentHashMap<String, UrlValidationRequest>()

    fun save(request: UrlValidationRequest): Mono<UrlValidationRequest> {
        db[request.id] = request
        return get(request.id)
                .logOnNext { LOG.info("Request with id: ${request.id} saved, $request") }
    }

    fun get(id: String): Mono<UrlValidationRequest> {
        return Mono.just(
                db[id]
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UrlValidationRequest with id $id not found")
        )
    }

    fun updateEntry(requestId: String, entry: UrlValidationRequest.UrlEntry): Mono<UrlValidationRequest> {
        return get(requestId)
                .flatMap { request ->
                    val dbEntry = request.urlEntries.first { it.id == entry.id }
                    // properties should be immutable and use data class copy method
                    dbEntry.result = entry.result
                    dbEntry.validatedOn = entry.validatedOn
                    dbEntry.threadId = entry.threadId
                    Mono.just(request)
                }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FakeDatabase::class.java)
    }
}
