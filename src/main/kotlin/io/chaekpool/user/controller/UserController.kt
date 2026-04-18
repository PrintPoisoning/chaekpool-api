package io.chaekpool.user.controller

import io.chaekpool.auth.annotation.AccessToken
import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.auth.annotation.RefreshToken
import io.chaekpool.auth.token.provider.CookieProvider
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.service.UserService
import io.micrometer.observation.annotation.Observed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@RestController
@RequestMapping("/api/v1/users")
@Observed(name = "user")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userService: UserService,
    private val cookieProvider: CookieProvider
) {

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "JWT access token으로 인증된 사용자의 정보를 반환합니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @SwaggerApiResponse(responseCode = "200", description = "사용자 정보 반환")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    @GetMapping("/me")
    fun me(@AccessUserId userId: UUID): ResponseEntity<UserResponse> {
        val user = userService.getUser(userId)
        return ResponseEntity.ok(user)
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "현재 사용자를 soft delete 처리하고 모든 세션 토큰을 폐기합니다. provider_accounts는 복원 가능성을 위해 유지됩니다",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @SwaggerApiResponse(responseCode = "204", description = "탈퇴 성공")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    @SwaggerApiResponse(responseCode = "410", description = "이미 탈퇴한 사용자")
    @DeleteMapping("/me")
    fun leave(
        @AccessUserId userId: UUID,
        @AccessToken accessToken: String,
        @RefreshToken refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        userService.leave(userId, accessToken, refreshToken)

        val expiredCookie = cookieProvider.expiredRefreshTokenCookie()
        response.addHeader(SET_COOKIE, expiredCookie.toString())

        return ResponseEntity.noContent().build()
    }
}
