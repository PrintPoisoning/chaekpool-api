package io.chaekpool.auth.token.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenValiditySeconds: Long,
    val refreshTokenValiditySeconds: Long
)
