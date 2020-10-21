package cz.gregetom.avastdemo.api.to

import java.net.URL
import java.time.OffsetDateTime

data class UrlValidationResponseTO(
        val result: Boolean,
        val urlEntries: List<UrlEntryTO>
) {

    data class UrlEntryTO(
            val url: URL,
            val validatedOn: OffsetDateTime?,
            val result: Boolean,
            val threadId: String?
    )
}