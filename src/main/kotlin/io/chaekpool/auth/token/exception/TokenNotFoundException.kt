package io.chaekpool.auth.token.exception

class TokenNotFoundException(
    message: String = "토큰을 찾을 수 없습니다",
    errorCode: String = "TOKEN_NOT_FOUND",
) : InvalidTokenException(message, errorCode)
