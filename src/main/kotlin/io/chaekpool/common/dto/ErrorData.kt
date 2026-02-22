package io.chaekpool.common.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorData(
    @param:JsonProperty("code") val code: String,
    @param:JsonProperty("message") val message: String?
)
