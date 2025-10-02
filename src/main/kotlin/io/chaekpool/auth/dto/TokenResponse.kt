package io.chaekpool.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.chaekpool.auth.token.dto.TokenPair

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String
) {
    constructor(tokenPair: TokenPair) : this(tokenPair.accessToken)
}
