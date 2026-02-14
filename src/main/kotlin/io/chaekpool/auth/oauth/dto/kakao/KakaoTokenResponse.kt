package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoTokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String,

    @param:JsonProperty("token_type")
    val tokenType: String,

    @param:JsonProperty("refresh_token")
    val refreshToken: String?,

    @param:JsonProperty("expires_in")
    val expiresIn: Long,

    @param:JsonProperty("scope")
    val scope: String?
)
