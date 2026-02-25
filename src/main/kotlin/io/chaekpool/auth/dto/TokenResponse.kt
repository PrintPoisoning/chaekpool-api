package io.chaekpool.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.chaekpool.auth.token.dto.TokenPair
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN

data class TokenResponse(
    @param:JsonProperty(ACCESS_TOKEN)
    val accessToken: String
) {
    constructor(tokenPair: TokenPair) : this(tokenPair.accessToken)
}
