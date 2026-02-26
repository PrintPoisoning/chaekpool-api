package io.chaekpool.auth.oauth2.controller

import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.oauth2.service.KakaoService
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.CookieProvider
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth/oauth2/kakao")
@Observed(name = "auth.oauth2.kakao")
class KakaoController(
    private val kakaoService: KakaoService,
    private val cookieProvider: CookieProvider
) {

    @GetMapping("/callback")
    fun kakaoLogin(
        @RequestParam code: String,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        val tokenPair: TokenPair = kakaoService.authenticate(code)
        val cookie = cookieProvider.refreshTokenCookie(tokenPair.refreshToken)

        response.addHeader(SET_COOKIE, cookie.toString())

        return ResponseEntity.ok(TokenResponse(tokenPair))
    }

    @PostMapping("/refresh")
    fun refreshOAuthTokens(
        @AccessUserId userId: UUID
    ): ResponseEntity<Unit> {
        kakaoService.refreshOAuthTokens(userId)
        return ResponseEntity.noContent().build()
    }
}
