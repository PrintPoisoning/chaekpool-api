package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

open class BadRequestException(
    message: String = "잘못된 요청입니다",
    errorCode: String = "BAD_REQUEST"
) : ServiceException(errorCode, HttpStatus.BAD_REQUEST, message)
