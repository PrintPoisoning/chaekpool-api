package io.chaekpool.common.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ErrorResponse(
    @param:JsonProperty("status")
    val status: Int,

    @param:JsonProperty("error")
    val error: String,

    @param:JsonProperty("message")
    val message: String?,

    @param:JsonProperty("error_code")
    val errorCode: String,

    @param:JsonProperty("path")
    val path: String,

    @param:JsonProperty("timestamp")
    val timestamp: String = LocalDateTime.now().toString()
)
