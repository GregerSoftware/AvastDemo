package cz.gregetom.avastdemo.db

import java.net.URL
import java.time.OffsetDateTime
import javax.validation.constraints.NotNull

data class UrlValidationRequest(

        @field:NotNull
        val id: String,
        val urlEntries: Set<UrlEntry>,
        @field:NotNull
        val done: Boolean = false,
        val result: Boolean? = null,
        val validatedOn: OffsetDateTime? = null
) {

    data class UrlEntry(
            @field:NotNull
            val id: String,
            @field:NotNull
            val url: URL,
            var validatedOn: OffsetDateTime? = null,
            var result: Boolean? = null,
            var threadId: String? = null
    )
}