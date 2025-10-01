package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class KakaoUserResponse(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("connected_at")
    val connectedAt: Date?,

    @JsonProperty("properties")
    val properties: KakaoProperties?,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
)
