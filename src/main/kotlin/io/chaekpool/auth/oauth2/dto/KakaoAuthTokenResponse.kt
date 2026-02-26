package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.chaekpool.common.serializer.EncryptedStringDeserializer
import io.chaekpool.common.serializer.EncryptedStringSerializer
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.EXPIRES_IN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.TOKEN_TYPE
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize

data class KakaoAuthTokenResponse(
    @param:JsonProperty(TOKEN_TYPE)
    val tokenType: String,

    @param:JsonProperty(ACCESS_TOKEN)
    @param:JsonDeserialize(using = EncryptedStringDeserializer::class)
    @get:JsonSerialize(using = EncryptedStringSerializer::class)
    val accessToken: String,

    @param:JsonProperty(ID_TOKEN)
    val idToken: String?,

    @param:JsonProperty(EXPIRES_IN)
    val expiresIn: Long,

    @param:JsonProperty(REFRESH_TOKEN)
    @param:JsonDeserialize(using = EncryptedStringDeserializer::class)
    @get:JsonSerialize(using = EncryptedStringSerializer::class)
    val refreshToken: String,

    @param:JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Long,

    @param:JsonProperty(SCOPE)
    val scope: String?,
)
