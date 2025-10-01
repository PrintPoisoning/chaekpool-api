package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProfile(
    @JsonProperty("nickname")
    val nickname: String?,

    @JsonProperty("is_default_nickname")
    val isDefaultNickname: Boolean?,

    @JsonProperty("profile_image_url")
    val profileImageUrl: String?,

    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?,

    @JsonProperty("is_default_image")
    val isDefaultImage: Boolean?,
)
