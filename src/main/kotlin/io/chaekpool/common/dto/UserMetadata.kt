package io.chaekpool.common.dto

data class UserMetadata(
    val ip: String?,
    val userAgent: String?,
    val device: String?,
    val os: String?,
    val browser: String?,
    val platformType: String?,
    val locale: String?,
    val timezone: String?,
    val appVersion: String? = null
)
