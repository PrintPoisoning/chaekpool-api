package io.chaekpool.auth.dev.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.chaekpool.auth.token.dto.TokenPair
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN

data class DevTokenResponse(
    @param:JsonProperty(ACCESS_TOKEN)
    val accessToken: String,

    @param:JsonProperty(REFRESH_TOKEN)
    val refreshToken: String
) {
    constructor(tokenPair: TokenPair) : this(tokenPair.accessToken, tokenPair.refreshToken)
}
