package io.chaekpool.common.exception.external

import io.chaekpool.common.exception.ExternalServiceException
import org.springframework.http.HttpStatus

class ExternalUnauthorizedException(
    externalSystem: ExternalSystem? = ExternalSystem.UNKNOWN_API,
    message: String = "외부 API 인증 실패",
    errorCode: String = "EXTERNAL_UNAUTHORIZED",
) : ExternalServiceException(errorCode, HttpStatus.UNAUTHORIZED, message, externalSystem)
