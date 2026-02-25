package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.config.CookieProperties
import org.springframework.http.ResponseCookie
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REFRESH_TOKEN
import org.springframework.stereotype.Component

@Component
class CookieProvider(
    private val cookieProperties: CookieProperties
) {

    fun refreshTokenCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN, refreshToken)
            .httpOnly(cookieProperties.httpOnly)
            .sameSite(cookieProperties.sameSite)
            .secure(cookieProperties.secure)
            .path(cookieProperties.path)
            .maxAge(cookieProperties.maxAge)
            .build()
    }
}
