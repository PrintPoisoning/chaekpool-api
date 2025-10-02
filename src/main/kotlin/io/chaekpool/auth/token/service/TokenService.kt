package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.repository.RefreshTokenRepository
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val blacklistManager: BlacklistManager,
    private val tokenManager: TokenManager,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    fun refresh(userId: Long, accessToken: String?, refreshToken: String): TokenPair {
        assertRefreshToken(userId, refreshToken)
        deactivateToken(userId, accessToken, refreshToken)

        val tokenPair = tokenManager.createTokenPair(userId)

        tokenManager.issueRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }

    fun assertRefreshToken(userId: Long, refreshToken: String) {
        tokenManager.assertRefreshToken(userId, refreshToken) // device id 추가 논의 필요 (header, metadata etc.)
        blacklistManager.assertToken(userId, refreshToken)
    }


    fun deactivateToken(userId: Long, accessToken: String?, refreshToken: String) {
        accessToken?.let { blacklistManager.blacklistToken(userId, accessToken) }
        blacklistManager.blacklistToken(userId, refreshToken)

        tokenManager.deleteByUserIdAndToken(userId, refreshToken) // device id 추가 논의 필요 (header, metadata etc.)
    }
}
