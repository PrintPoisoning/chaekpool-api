package io.chaekpool.auth.token.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.cookie")
class CookieProperties(
    val httpOnly: Boolean = false,
    val secure: Boolean = false,
    val sameSite: String = "Lax",
    val path: String = "/",
    val maxAge: Long = 3600L
)
