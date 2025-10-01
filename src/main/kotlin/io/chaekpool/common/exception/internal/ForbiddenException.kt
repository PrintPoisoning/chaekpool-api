package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

class ForbiddenException(
    message: String = "권한이 없습니다.",
    errorCode: String = "FORBIDDEN"
) : ServiceException(errorCode, HttpStatus.FORBIDDEN, message)
