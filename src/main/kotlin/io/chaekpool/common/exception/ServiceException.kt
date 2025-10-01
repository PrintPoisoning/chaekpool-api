package io.chaekpool.common.exception

import org.springframework.http.HttpStatus

open class ServiceException(
    val errorCode: String,
    val httpStatus: HttpStatus,
    override val message: String
) : RuntimeException(message)
