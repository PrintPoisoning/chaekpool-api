package io.chaekpool.auth.controller

import io.chaekpool.auth.annotation.AccessToken
import io.chaekpool.auth.annotation.RefreshToken
import io.chaekpool.auth.annotation.UserId
import io.chaekpool.token.service.BlacklistManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val blacklistManager: BlacklistManager
) {

    @DeleteMapping("/tokens")
    fun logout(
        @UserId userId: String,
        @AccessToken accessToken: String,
        @RefreshToken refreshToken: String
    ): ResponseEntity<Unit> {
        blacklistManager.blacklistToken(userId, accessToken)
        blacklistManager.blacklistToken(userId, refreshToken)

        return ResponseEntity.noContent()
            .build()
    }
}
