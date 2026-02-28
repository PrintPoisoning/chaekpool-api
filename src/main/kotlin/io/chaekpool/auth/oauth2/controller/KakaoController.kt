package io.chaekpool.auth.oauth2.controller

import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.oauth2.service.KakaoService
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.provider.CookieProvider
import io.micrometer.observation.annotation.Observed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@RestController
@RequestMapping("/api/v1/auth/oauth2/kakao")
@Observed(name = "auth.oauth2.kakao")
@Tag(name = "OAuth2", description = "OAuth2 인증 API")
class KakaoController(
    private val kakaoService: KakaoService,
    private val cookieProvider: CookieProvider,
    private val kakaoAuthProperties: KakaoAuthProperties
) {

    @Operation(
        summary = "카카오 로그인 페이지 이동",
        description = "카카오 로그인 페이지로 리다이렉트합니다. 브라우저에서 직접 호출하세요",
        security = []
    )
    @SwaggerApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 리다이렉트")
    @GetMapping("/authorize")
    fun authorize(): ResponseEntity<Unit> {
        val authorizeUri = UriComponentsBuilder
            .fromUriString("https://kauth.kakao.com/oauth/authorize")
            .queryParam("client_id", kakaoAuthProperties.clientId)
            .queryParam("redirect_uri", kakaoAuthProperties.redirectUri)
            .queryParam("response_type", "code")
            .build()
            .toUri()

        return ResponseEntity.status(302).location(authorizeUri).build()
    }

    @Operation(
        summary = "카카오 OAuth 로그인",
        description = "카카오 인가 코드로 OAuth 인증을 수행하고 JWT 토큰 쌍을 발급합니다",
        security = []
    )
    @SwaggerApiResponse(responseCode = "200", description = "로그인 성공")
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

    @Operation(
        summary = "카카오 OAuth 토큰 갱신",
        description = "저장된 카카오 refresh token으로 카카오 OAuth 토큰을 갱신합니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @SwaggerApiResponse(responseCode = "204", description = "토큰 갱신 성공")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    @PostMapping("/refresh")
    fun refreshOAuthTokens(
        @AccessUserId userId: UUID
    ): ResponseEntity<Unit> {
        kakaoService.refreshOAuthTokens(userId)
        return ResponseEntity.noContent().build()
    }
}
