package io.chaekpool.auth.token.controller

import io.chaekpool.auth.annotation.AccessToken
import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.auth.annotation.RefreshToken
import io.chaekpool.auth.annotation.RefreshUserId
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.oauth2.service.KakaoService
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.CookieProvider
import io.chaekpool.auth.token.service.TokenService
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth/token")
@Observed(name = "auth.token")
class TokenController(
    private val tokenService: TokenService,
    private val cookieProvider: CookieProvider,
    private val kakaoService: KakaoService
) {

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

    @PostMapping("/refresh")
    fun refresh(
        @RefreshUserId userId: UUID,
        @AccessToken accessToken: String?,
        @RefreshToken refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        // todo: 문제 ㅈㄴ 많음, 컨트롤러에서 이따구로 하는게 아니라 서비스에서 그렇게 해야지 db 조회해서 provider 에 맞게 딱딱 해야지
        // todo: provider 확정되면 그때 provider 별로 token 유효성 검증하고 아니면 401 or refresh 를 해야지
        val tokenPair: TokenPair = tokenService.refreshWithOAuth(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            oauthRefreshFn = { kakaoService.refreshWithOAuthToken(it) }
        )
        val cookie = cookieProvider.refreshTokenCookie(tokenPair.refreshToken)

        response.addHeader(SET_COOKIE, cookie.toString())

        return ResponseEntity.ok(TokenResponse(tokenPair))
    }
}
