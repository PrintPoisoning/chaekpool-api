package io.chaekpool.auth.token.exception

class MissingClaimException(
    message: String = "JWT token missing required claim",
    errorCode: String = "MISSING_CLAIM",
) : InvalidTokenException(message, errorCode)
