package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProperties(
    @param:JsonProperty("nickname")
    val nickname: String,

    @param:JsonProperty("profile_image")
    val profileImage: String,

    @param:JsonProperty("thumbnail_image")
    val thumbnailImage: String,
)
