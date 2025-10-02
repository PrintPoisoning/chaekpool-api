package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

open class UnauthorizedException(
    message: String = "인증이 필요합니다.",
    errorCode: String = "UNAUTHORIZED"
) : ServiceException(errorCode, HttpStatus.UNAUTHORIZED, message)
