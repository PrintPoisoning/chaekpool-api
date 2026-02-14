package io.chaekpool.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.chaekpool.auth.token.dto.TokenPair

data class TokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String
) {
    constructor(tokenPair: TokenPair) : this(tokenPair.accessToken)
}
