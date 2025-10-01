package io.chaekpool.auth.token.controller

import io.chaekpool.auth.annotation.AccessToken
import io.chaekpool.auth.annotation.RefreshToken
import io.chaekpool.auth.annotation.UserId
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.token.service.CookieProvider
import io.chaekpool.auth.token.service.TokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/token")
class TokenController(
    private val tokenService: TokenService,
    private val cookieProvider: CookieProvider
) {

    @DeleteMapping("/")
    fun logout(
        @UserId userId: Long,
        @AccessToken accessToken: String,
        @RefreshToken refreshToken: String
    ): ResponseEntity<Unit> {
        tokenService.deactivate(userId, accessToken, refreshToken)

        return ResponseEntity.noContent()
            .build()
    }

    @PostMapping("/refresh")
    fun refresh(
        @UserId userId: Long,
        @AccessToken accessToken: String,
        @RefreshToken refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        val tokenResponse: TokenResponse = tokenService.refresh(userId, accessToken, refreshToken)
        val cookie = cookieProvider.refreshTokenCookie(tokenResponse.refreshToken)

        response.addHeader(SET_COOKIE, cookie.toString())

        return ResponseEntity.ok(tokenResponse)
    }
}
