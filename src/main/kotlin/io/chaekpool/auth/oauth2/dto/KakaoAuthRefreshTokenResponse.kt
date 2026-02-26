package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.EXPIRES_IN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.TOKEN_TYPE

data class KakaoAuthRefreshTokenResponse(
    @param:JsonProperty(TOKEN_TYPE)
    val tokenType: String,

    @param:JsonProperty(ACCESS_TOKEN)
    val accessToken: String,

    @param:JsonProperty(EXPIRES_IN)
    val expiresIn: Long,

    @param:JsonProperty(REFRESH_TOKEN)
    val refreshToken: String?,

    @param:JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Long?
)
