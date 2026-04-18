package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

open class GoneException(
    message: String = "요청한 리소스가 더 이상 유효하지 않습니다",
    errorCode: String = "GONE"
) : ServiceException(errorCode, HttpStatus.GONE, message)
