package io.chaekpool.auth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProfile(
    val nickname: String?,

    @JsonProperty("profile_image")
    val profileImage: String?
)
