package io.chaekpool.common.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("trace_id", "span_id", "status", "data")
data class ApiResponse<T>(
    @param:JsonProperty("trace_id") val traceId: String,
    @param:JsonProperty("span_id") val spanId: String,
    @param:JsonProperty("status") val status: Int,
    @param:JsonProperty("data") val data: T
)
