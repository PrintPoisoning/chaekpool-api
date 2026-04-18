package io.chaekpool.auth.oauth2.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RejoinRequest(
    @param:JsonProperty("rejoin_ticket")
    val rejoinTicket: String
)
