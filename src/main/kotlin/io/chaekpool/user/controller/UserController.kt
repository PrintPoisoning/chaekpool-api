package io.chaekpool.user.controller

import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.service.UserService
import io.micrometer.observation.annotation.Observed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@RestController
@RequestMapping("/api/v1/users")
@Observed(name = "user")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(private val userService: UserService) {

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
}
