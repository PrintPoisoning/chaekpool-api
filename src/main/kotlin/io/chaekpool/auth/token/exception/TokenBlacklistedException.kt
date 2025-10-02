package io.chaekpool.auth.token.exception

import io.chaekpool.common.exception.internal.ForbiddenException

class TokenBlacklistedException(
    message: String = "JWT token blacklisted",
    errorCode: String = "TOKEN_BLACKLISTED",
) : ForbiddenException(message, errorCode)
