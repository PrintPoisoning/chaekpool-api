package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoApiProperties(
    @param:JsonProperty("nickname")
    val nickname: String,

    @param:JsonProperty("profile_image")
    val profileImage: String,

    @param:JsonProperty("thumbnail_image")
    val thumbnailImage: String,
)
