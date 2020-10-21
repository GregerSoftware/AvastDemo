package cz.gregetom.avastdemo.config

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {

    /**
     * Include all information about error
     */
    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): Map<String, Any> {
        return super.getErrorAttributes(request, options.including(*ErrorAttributeOptions.Include.values()))
    }
}
