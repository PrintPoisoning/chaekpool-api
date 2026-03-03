package io.chaekpool.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "common.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = emptyList()
)
