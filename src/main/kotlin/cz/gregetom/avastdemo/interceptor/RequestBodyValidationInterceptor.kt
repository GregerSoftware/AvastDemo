package cz.gregetom.avastdemo.interceptor

import cz.gregetom.avastdemo.filter.logging.LoggingContext
import cz.gregetom.avastdemo.util.LoggingUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import javax.validation.ConstraintViolation
import javax.validation.Validator

/**
 * Interceptor for request bodies validation.
 */
@Component
class RequestBodyValidationInterceptor(private val validator: Validator) : RequestBodyInterceptor {

    override fun intercept(requestBody: Any, context: LoggingContext) {
        LoggingUtil.logWithContext({ LOG.info("Perform request body validation") }, context)

        val validationResult = validator.validate(requestBody)
        if (validationResult.isNotEmpty()) {
            onValidationErrors(validationResult, context)
        }
    }

    private fun onValidationErrors(errors: Set<ConstraintViolation<*>>, context: LoggingContext): Mono<ServerResponse> {
        val errorMessage = errors.joinToString(", ") { it.message }
        LoggingUtil.logWithContext({ LOG.error("Request body validation failed: $errorMessage") }, context)
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RequestBodyValidationInterceptor::class.java)
    }
}