package io.chaekpool.user.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class UserResponse(
    @param:JsonProperty("id")
    val id: UUID,

    @param:JsonProperty("email")
    val email: String?,

    @param:JsonProperty("username")
    val username: String?,

    @param:JsonProperty("profile_image_url")
    val profileImageUrl: String?,

    @param:JsonProperty("visibility")
    val visibility: String,

    @param:JsonProperty("status")
    val status: String
)
