package io.chaekpool.auth.token.exception

import io.chaekpool.common.exception.internal.UnauthorizedException

open class InvalidTokenException(
    message: String = "Invalid JWT token",
    errorCode: String = "INVALID_TOKEN",
) : UnauthorizedException(message, errorCode)
