package cz.gregetom.avastdemo.interceptor

import cz.gregetom.avastdemo.filter.logging.LoggingContext

/**
 * Interface for cross-cutting concerns working with request body, i.e. logging, validation,...
 */
interface RequestBodyInterceptor {

    fun intercept(requestBody: Any, context: LoggingContext)
}