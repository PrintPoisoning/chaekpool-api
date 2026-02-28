package io.chaekpool.auth.dev.controller

import io.chaekpool.auth.constant.AuthProvider
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.token.provider.CookieProvider
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Profile("local", "dev")
@RestController
@RequestMapping("/api/v1/auth/dev")
@Tag(name = "Dev", description = "개발용 인증 API (local/dev 프로필 전용)")
class DevAuthController(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val cookieProvider: CookieProvider
) {

    @Operation(
        summary = "개발용 테스트 로그인",
        description = "새 사용자를 생성하고 JWT 토큰을 발급합니다. Swagger UI에서 빠르게 인증 토큰을 발급받아 API 테스트에 활용할 수 있습니다",
        security = []
    )
    @SwaggerApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    @Transactional
    fun devLogin(
        @RequestParam(defaultValue = "dev-user") username: String,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        val user = userRepository.save(Users(username = username))

        val tokenPair = tokenManager.createTokenPair(user.id!!, AuthProvider.CHAEKPOOL)
        tokenManager.saveRefreshToken(user.id, tokenPair.refreshToken)
        userRepository.updateLastLoginAt(user.id)

        val cookie = cookieProvider.refreshTokenCookie(tokenPair.refreshToken)
        response.addHeader(SET_COOKIE, cookie.toString())

        return ResponseEntity.ok(TokenResponse(tokenPair))
    }
}
