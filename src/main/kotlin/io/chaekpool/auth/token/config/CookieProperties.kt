package io.chaekpool.auth.token.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.cookie")
class CookieProperties(
    val httpOnly: Boolean,
    val secure: Boolean,
    val sameSite: String,
    val path: String,
    val maxAge: Long
)
