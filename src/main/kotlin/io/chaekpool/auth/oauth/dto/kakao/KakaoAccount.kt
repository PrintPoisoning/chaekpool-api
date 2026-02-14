package io.chaekpool.auth.oauth.dto.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoAccount(
    @param:JsonProperty("email")
    val email: String?,

    @param:JsonProperty("has_email")
    val hasEmail: Boolean?,

    @param:JsonProperty("email_needs_agreement")
    val emailNeedsAgreement: Boolean?,

    @param:JsonProperty("is_email_valid")
    val isEmailValid: Boolean?,

    @param:JsonProperty("is_email_verified")
    val isEmailVerified: Boolean?,

    @param:JsonProperty("profile")
    val profile: KakaoProfile?,

    @param:JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean?,

    @param:JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean?,
)
