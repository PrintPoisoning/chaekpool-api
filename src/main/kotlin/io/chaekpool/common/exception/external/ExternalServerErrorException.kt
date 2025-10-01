package io.chaekpool.common.exception.external

import io.chaekpool.common.exception.ExternalServiceException
import org.springframework.http.HttpStatus

class ExternalServerErrorException(
    externalSystem: ExternalSystem,
    message: String = "외부 API 서버 오류",
    errorCode: String = "EXTERNAL_SERVER_ERROR"
) : ExternalServiceException(errorCode, HttpStatus.BAD_GATEWAY, message, externalSystem)
