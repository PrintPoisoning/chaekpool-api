package io.chaekpool.common.exception.internal

import io.chaekpool.common.exception.ServiceException
import org.springframework.http.HttpStatus

open class InternalServerErrorException(
    message: String = "서버 내부 오류가 발생했습니다",
    errorCode: String = "INTERNAL_SERVER_ERROR"
) : ServiceException(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, message)
