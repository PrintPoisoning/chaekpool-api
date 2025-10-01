package io.chaekpool.auth.token.service

import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.common.util.isTrueOrForbidden
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val blacklistManager: BlacklistManager,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider
) {

    fun validateToken(userId: Long, token: String): Boolean {
        return jwtProvider.validateToken(token) && !blacklistManager.isBlacklisted(userId, token)
    }

    fun refresh(userId: Long, accessToken: String, refreshToken: String): TokenResponse {
        validateToken(userId, refreshToken).isTrueOrForbidden("Invalid refresh token")

        deactivate(userId, accessToken, refreshToken)

        val newAccessToken = jwtProvider.createAccessToken(userId)
        val newRefreshToken = jwtProvider.createRefreshToken(userId)

        tokenManager.issueRefreshToken(userId, newRefreshToken)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }

    fun deactivate(userId: Long, accessToken: String, refreshToken: String) {
        blacklistManager.blacklistToken(userId, accessToken)
        blacklistManager.blacklistToken(userId, refreshToken)

        // device id 추가 예정
//        tokenManager.deleteAllRefreshTokens(userId)
    }
}
