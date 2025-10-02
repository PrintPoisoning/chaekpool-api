package io.chaekpool.auth.token.exception

class TokenExpiredException(
    message: String = "JWT token expired",
    errorCode: String = "TOKEN_EXPIRED",
) : InvalidTokenException(message, errorCode)
