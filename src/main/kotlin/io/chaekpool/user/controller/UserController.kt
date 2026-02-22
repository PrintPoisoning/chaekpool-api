package io.chaekpool.user.controller

import io.chaekpool.auth.annotation.AccessUserId
import io.chaekpool.user.dto.UserResponse
import io.chaekpool.user.service.UserService
import io.micrometer.observation.annotation.Observed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Observed(name = "user")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun me(@AccessUserId userId: UUID): ResponseEntity<UserResponse> {
        val user = userService.getUser(userId)
        return ResponseEntity.ok(user)
    }
}
