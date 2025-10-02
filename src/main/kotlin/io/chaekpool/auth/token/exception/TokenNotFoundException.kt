package io.chaekpool.auth.token.exception

import io.chaekpool.common.exception.internal.UnauthorizedException

class TokenNotFoundException(
    message: String = "토큰을 찾을 수 없습니다.",
    errorCode: String = "TOKEN_NOT_FOUND",
) : UnauthorizedException(message, errorCode)
