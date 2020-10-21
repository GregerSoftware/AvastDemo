package cz.gregetom.avastdemo.service

import cz.gregetom.avastdemo.api.mapper.ValidationMapper
import cz.gregetom.avastdemo.api.to.UrlValidationRequestListTO
import cz.gregetom.avastdemo.api.to.UrlValidationResponseTO
import cz.gregetom.avastdemo.db.FakeDatabase
import cz.gregetom.avastdemo.filter.logging.LoggingContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ValidationRequestService(
        private val fakeDatabase: FakeDatabase,
        private val validatorProvider: ValidationProvider
) {

    fun getResult(requestId: String): Mono<UrlValidationResponseTO> {
        return fakeDatabase.get(requestId)
                .filter { it.done }
                .map { ValidationMapper.mapToUrlValidationResponseTO(it) }
    }

    fun saveValidationRequest(request: UrlValidationRequestListTO): Mono<String> {
        return fakeDatabase.save(ValidationMapper.mapToUrlValidationRequest(request))
                .map { it.id }
                .doOnEach { signal ->
                    if (signal.isOnNext) {
                        validatorProvider.validate(signal.get()!!)
                                .subscriberContext { it.put(LoggingContext.LOGGING_CONTEXT_ATTRIBUTE, LoggingContext.from(signal)) }
                                .subscribe()
                    }
                }
    }
}