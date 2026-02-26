package io.chaekpool.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.crypto")
data class CryptoProperties(
    val secretKey: String
)
