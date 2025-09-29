package io.chaekpool.auth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class KakaoUserResponse(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("connected_at")
    val connectedAt: Date,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
)
