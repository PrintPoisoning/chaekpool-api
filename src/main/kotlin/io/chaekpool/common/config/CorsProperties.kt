package io.chaekpool.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "common.cors")
data class CorsProperties(
    var allowedOrigins: List<String> = emptyList()
)
