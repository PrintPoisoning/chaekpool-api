package io.chaekpool.auth.oauth.dto.kakao

import java.util.Date
import com.fasterxml.jackson.annotation.JsonProperty

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
