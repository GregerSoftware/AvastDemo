package cz.gregetom.avastdemo.api.to

import javax.validation.constraints.Size

data class UrlValidationRequestListTO(

        @field:Size(min = 1, max = 20, message = "UrlValidationRequest list size must be between <1,20>")
        val urlValidationRequestList: List<UrlValidationRequestTO>
)