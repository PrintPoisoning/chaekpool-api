package io.chaekpool.auth.controller

import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.service.KakaoOAuthService
import io.chaekpool.token.config.JwtProperties
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/oauth/kakao")
class OAuthKakaoController(
    private val kakaoOAuthService: KakaoOAuthService,
    private val jwtProperties: JwtProperties
) {

    @GetMapping("/callback")
    fun kakaoLogin(
        @RequestParam code: String,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        val tokenResponse: TokenResponse = kakaoOAuthService.authenticateWithKakao(code)
        val cookie = ResponseCookie.from("refresh_token", tokenResponse.refreshToken)
            .httpOnly(true)
//            .secure(true)
            .sameSite("Strict")
            .path("/api/v1")
            .maxAge(jwtProperties.refreshTokenValiditySeconds)
            .build()

        response.addHeader("Set-Cookie", cookie.toString())

        return ResponseEntity.ok(tokenResponse)
    }
}
