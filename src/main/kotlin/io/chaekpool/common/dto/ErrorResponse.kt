package io.chaekpool.common.dto

import java.time.LocalDateTime
import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    @JsonProperty("status")
    val status: Int,

    @JsonProperty("error")
    val error: String,

    @JsonProperty("message")
    val message: String?,

    @JsonProperty("error_code")
    val errorCode: String,

    @JsonProperty("path")
    val path: String,

    @JsonProperty("timestamp")
    val timestamp: String = LocalDateTime.now().toString()
)
