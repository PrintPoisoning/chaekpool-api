package io.chaekpool.common.exception.external

import io.chaekpool.common.exception.ExternalServiceException
import org.springframework.http.HttpStatus

class ExternalBadRequestException(
    externalSystem: ExternalSystem? = ExternalSystem.UNKNOWN_API,
    message: String = "외부 API 요청이 잘못되었습니다.",
    errorCode: String = "EXTERNAL_BAD_REQUEST"
) : ExternalServiceException(errorCode, HttpStatus.BAD_REQUEST, message, externalSystem)
