package io.chaekpool.common.exception.external

import io.chaekpool.common.exception.ExternalServiceException
import org.springframework.http.HttpStatus

class ExternalForbiddenException(
    externalSystem: ExternalSystem? = ExternalSystem.UNKNOWN_API,
    message: String = "외부 API 접근 권한 없음",
    errorCode: String = "EXTERNAL_FORBIDDEN"
) : ExternalServiceException(errorCode, HttpStatus.FORBIDDEN, message, externalSystem)
