package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN

@JsonInclude(JsonInclude.Include.NON_NULL)
data class KakaoCallbackResponse(
    @param:JsonProperty("type")
    val type: String,

    @param:JsonProperty(ACCESS_TOKEN)
    val accessToken: String? = null,

    @param:JsonProperty("rejoin_ticket")
    val rejoinTicket: String? = null
) {
    companion object {
        const val TYPE_AUTHENTICATED = "authenticated"
        const val TYPE_REJOIN_REQUIRED = "rejoin_required"

        fun authenticated(accessToken: String): KakaoCallbackResponse =
            KakaoCallbackResponse(type = TYPE_AUTHENTICATED, accessToken = accessToken)

        fun rejoinRequired(rejoinTicket: String): KakaoCallbackResponse =
            KakaoCallbackResponse(type = TYPE_REJOIN_REQUIRED, rejoinTicket = rejoinTicket)
    }
}
