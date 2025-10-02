package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

open class NotFoundException(
    message: String = "리소스를 찾을 수 없습니다.",
    errorCode: String = "NOT_FOUND"
) : ServiceException(errorCode, HttpStatus.NOT_FOUND, message)
