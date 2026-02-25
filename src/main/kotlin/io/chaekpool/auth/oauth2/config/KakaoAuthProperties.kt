package io.chaekpool.auth.oauth2.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.oauth2.kakao")
data class KakaoAuthProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String
)
