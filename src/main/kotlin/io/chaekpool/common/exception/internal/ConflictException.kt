package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

open class ConflictException(
    message: String = "이미 존재하는 리소스입니다",
    errorCode: String = "CONFLICT"
) : ServiceException(errorCode, HttpStatus.CONFLICT, message)
