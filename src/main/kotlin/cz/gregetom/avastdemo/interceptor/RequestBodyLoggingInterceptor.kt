package cz.gregetom.avastdemo.interceptor

import cz.gregetom.avastdemo.filter.logging.LoggingContext
import cz.gregetom.avastdemo.util.LoggingUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Interceptor for request bodies logging.
 */
@Component
class RequestBodyLoggingInterceptor : RequestBodyInterceptor {

    override fun intercept(requestBody: Any, context: LoggingContext) {
        logRequestBody(requestBody, context)
    }

    private fun logRequestBody(requestBody: Any, context: LoggingContext) {
        val builder = StringBuilder()
                .append("***** HTTP REQUEST BODY *****")
                .append("\n")
                .append(requestBody)
                .append("\n")
                .append("*****************************")
                .append("\n")

        LoggingUtil.logWithContext({ LOG.debug(builder.toString()) }, context)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RequestBodyLoggingInterceptor::class.java)
    }
}