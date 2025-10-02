package io.chaekpool.auth.token.exception

import io.chaekpool.common.exception.internal.UnauthorizedException

class TokenBlacklistedException(
    message: String = "비활성화된 토큰입니다.",
    errorCode: String = "TOKEN_BLACKLISTED",
) : UnauthorizedException(message, errorCode)
