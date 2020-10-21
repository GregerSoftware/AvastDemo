package cz.gregetom.avastdemo.api.mapper

import cz.gregetom.avastdemo.api.to.UrlValidationRequestListTO
import cz.gregetom.avastdemo.api.to.UrlValidationResponseTO
import cz.gregetom.avastdemo.db.UrlValidationRequest
import java.util.*

/**
 * Mapper between database and transfer objects.
 */
object ValidationMapper {

    fun mapToUrlValidationResponseTO(request: UrlValidationRequest): UrlValidationResponseTO {
        return UrlValidationResponseTO(
                result = request.result ?: false,
                urlEntries = request.urlEntries.map { entry ->
                    UrlValidationResponseTO.UrlEntryTO(
                            url = entry.url,
                            validatedOn = entry.validatedOn,
                            result = entry.result ?: false,
                            threadId = entry.threadId
                    )
                }
        )
    }

    fun mapToUrlValidationRequest(request: UrlValidationRequestListTO): UrlValidationRequest {
        return UrlValidationRequest(
                id = UUID.randomUUID().toString(),
                urlEntries = request.urlValidationRequestList.map {
                    UrlValidationRequest.UrlEntry(
                            id = UUID.randomUUID().toString(),
                            url = it.url
                    )
                }.toSet()
        )
    }
}
