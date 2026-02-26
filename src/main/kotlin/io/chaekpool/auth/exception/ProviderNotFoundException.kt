package io.chaekpool.auth.exception

import io.chaekpool.common.exception.internal.NotFoundException

class ProviderNotFoundException(providerName: String) : NotFoundException(
    message = "OAuth 제공자를 찾을 수 없습니다: $providerName",
    errorCode = "PROVIDER_NOT_FOUND"
)
