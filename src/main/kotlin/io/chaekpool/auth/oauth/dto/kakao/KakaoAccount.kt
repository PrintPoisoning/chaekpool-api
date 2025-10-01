package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoAccount(
    @JsonProperty("email")
    val email: String?,

    @JsonProperty("has_email")
    val hasEmail: Boolean?,

    @JsonProperty("email_needs_agreement")
    val emailNeedsAgreement: Boolean?,

    @JsonProperty("is_email_valid")
    val isEmailValid: Boolean?,

    @JsonProperty("is_email_verified")
    val isEmailVerified: Boolean?,

    @JsonProperty("profile")
    val profile: KakaoProfile?,

    @JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean?,

    @JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean?,
)
