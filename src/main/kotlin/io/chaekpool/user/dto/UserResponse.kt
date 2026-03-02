package io.chaekpool.user.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserResponse(
    @param:JsonProperty("email")
    val email: String?,

    @param:JsonProperty("nickname")
    val nickname: String?,

    @param:JsonProperty("handle")
    val handle: String,

    @param:JsonProperty("profile_image_url")
    val profileImageUrl: String?,

    @param:JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?,

    @param:JsonProperty("visibility")
    val visibility: String,

    @param:JsonProperty("status")
    val status: String
)
