package io.chaekpool.auth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProperties(
    @JsonProperty("nickname")
    val nickname: String,

    @JsonProperty("profile_image")
    val profileImage: String,

    @JsonProperty("thumbnail_image")
    val thumbnailImage: String,
)
