package io.chaekpool.common.exception

import io.chaekpool.common.exception.external.ExternalSystem
import org.springframework.http.HttpStatus

open class ExternalServiceException(
    errorCode: String,
    httpStatus: HttpStatus,
    message: String,
    val externalSystem: ExternalSystem? = ExternalSystem.UNKNOWN_SERVICE
) : ServiceException(errorCode, httpStatus, message)
