package io.chaekpool.auth.oauth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.kakao")
data class OAuthKakaoProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String
)
