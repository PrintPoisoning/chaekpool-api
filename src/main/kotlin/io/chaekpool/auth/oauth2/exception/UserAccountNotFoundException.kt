package io.chaekpool.auth.oauth2.exception

import io.chaekpool.common.exception.internal.NotFoundException

class UserAccountNotFoundException(providerName: String) : NotFoundException(
    message = "사용자의 OAuth 계정을 찾을 수 없습니다: $providerName",
    errorCode = "USER_ACCOUNT_NOT_FOUND"
)
