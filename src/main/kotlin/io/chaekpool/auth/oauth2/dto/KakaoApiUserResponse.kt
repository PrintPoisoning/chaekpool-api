package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class KakaoApiUserResponse(
    @param:JsonProperty("id")
    val id: Long,

    @param:JsonProperty("connected_at")
    val connectedAt: Date?,

    @param:JsonProperty("properties")
    val properties: KakaoApiProperties?,

    @param:JsonProperty("kakao_account")
    val kakaoAccount: KakaoApiAccount?
)
