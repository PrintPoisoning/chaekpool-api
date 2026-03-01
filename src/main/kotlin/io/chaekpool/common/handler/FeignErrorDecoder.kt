package io.chaekpool.common.handler

import feign.Response
import feign.codec.ErrorDecoder
import io.chaekpool.common.exception.ExternalServiceException
import io.chaekpool.common.exception.external.ExternalBadRequestException
import io.chaekpool.common.exception.external.ExternalForbiddenException
import io.chaekpool.common.exception.external.ExternalServerErrorException
import io.chaekpool.common.exception.external.ExternalSystem
import io.chaekpool.common.exception.external.ExternalUnauthorizedException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import java.nio.charset.StandardCharsets

class FeignErrorDecoder : ErrorDecoder {

    private val log = KotlinLogging.logger {}

    override fun decode(methodKey: String?, response: Response): RuntimeException {
        val body = response.body()
            ?.asInputStream()
            ?.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }
            ?: "No response body"

        log.error { "[HTTP_EXT_ERR] methodKey=$methodKey status=${response.status()} body=$body" }

        return when (response.status()) {
            400 -> ExternalBadRequestException(ExternalSystem.UNKNOWN_API)
            401 -> ExternalUnauthorizedException(ExternalSystem.UNKNOWN_API)
            403 -> ExternalForbiddenException(ExternalSystem.UNKNOWN_API)
            502 -> ExternalServerErrorException(ExternalSystem.UNKNOWN_API)
            else -> {
                val httpStatus = HttpStatus.resolve(response.status()) ?: HttpStatus.INTERNAL_SERVER_ERROR

                ExternalServiceException(
                    errorCode = httpStatus.name,
                    httpStatus = httpStatus,
                    message = response.reason() ?: httpStatus.reasonPhrase,
                    externalSystem = ExternalSystem.UNKNOWN_API,
                )
            }
        }
    }
}
