package cz.gregetom.avastdemo.extension

import cz.gregetom.avastdemo.filter.logging.LoggingContext
import cz.gregetom.avastdemo.util.LoggingUtil
import reactor.core.publisher.Mono

/**
 * Support for logging purposes
 */
object PublisherExtensions {

    fun <T> Mono<T>.logOnNext(log: (T?) -> Unit): Mono<T> {
        return this.doOnEach { signal ->
            if (signal.isOnNext) {
                LoggingUtil.logWithContext({ log(signal.get()) }, LoggingContext.from(signal))
            }
        }
    }

    fun <T> Mono<T>.logOnError(log: (Throwable?) -> Unit): Mono<T> {
        return this.doOnEach { signal ->
            if (signal.isOnError) {
                LoggingUtil.logWithContext({ log(signal.throwable) }, LoggingContext.from(signal))
            }
        }
    }
}
