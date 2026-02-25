package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoApiProfile(
    @param:JsonProperty("nickname")
    val nickname: String?,

    @param:JsonProperty("is_default_nickname")
    val isDefaultNickname: Boolean?,

    @param:JsonProperty("profile_image_url")
    val profileImageUrl: String?,

    @param:JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?,

    @param:JsonProperty("is_default_image")
    val isDefaultImage: Boolean?,
)
