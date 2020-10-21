package cz.gregetom.avastdemo.exception

import org.springframework.http.HttpStatus

// should contains more information
class CustomClientException(status: HttpStatus) : RuntimeException("Client error $status")
