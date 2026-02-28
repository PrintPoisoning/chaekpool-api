package io.chaekpool.auth.token.controller

import io.chaekpool.auth.annotation.AccessToken
import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.auth.annotation.RefreshToken
import io.chaekpool.auth.annotation.RefreshUserId
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.token.provider.CookieProvider
import io.chaekpool.auth.token.service.TokenService
import io.micrometer.observation.annotation.Observed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@RestController
@RequestMapping("/api/v1/auth/token")
@Observed(name = "auth.token")
@Tag(name = "Token", description = "토큰 관리 API")
class TokenController(
    private val tokenService: TokenService,
    private val cookieProvider: CookieProvider
) {

    @Operation(
        summary = "로그아웃",
        description = "access token과 refresh token을 블랙리스트에 등록하고 비활성화합니다"
    )
    @SwaggerApiResponse(responseCode = "204", description = "로그아웃 성공")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    @DeleteMapping
    fun logout(
        @AccessUserId userId: UUID,
        @AccessToken accessToken: String,
        @RefreshToken refreshToken: String
    ): ResponseEntity<Unit> {
        tokenService.deactivate(userId, accessToken, refreshToken)

        return ResponseEntity.noContent()
            .build()
    }

    @Operation(
        summary = "토큰 갱신",
        description = "refresh token 쿠키로 새로운 access token을 발급합니다",
        security = []
    )
    @SwaggerApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @SwaggerApiResponse(responseCode = "401", description = "유효하지 않은 refresh token")
    @PostMapping("/refresh")
    fun refresh(
        @RefreshUserId userId: UUID,
        @AccessToken accessToken: String?,
        @RefreshToken refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        val tokenPair = tokenService.refresh(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
        val cookie = cookieProvider.refreshTokenCookie(tokenPair.refreshToken)

        response.addHeader(SET_COOKIE, cookie.toString())

        return ResponseEntity.ok(TokenResponse(tokenPair))
    }
}
