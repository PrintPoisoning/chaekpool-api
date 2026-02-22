package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class KakaoUserResponse(
    @param:JsonProperty("id")
    val id: Long,

    @param:JsonProperty("connected_at")
    val connectedAt: Date?,

    @param:JsonProperty("properties")
    val properties: KakaoProperties?,

    @param:JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
)
