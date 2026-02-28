package io.chaekpool.auth.token.exception

import io.chaekpool.common.exception.internal.UnauthorizedException

class TokenBlacklistedException(
    message: String = "JWT token blacklisted",
    errorCode: String = "TOKEN_BLACKLISTED",
) : UnauthorizedException(message, errorCode)
