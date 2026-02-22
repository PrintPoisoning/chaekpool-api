package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenService(
    private val blacklistManager: BlacklistManager,
    private val tokenManager: TokenManager,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    fun refresh(userId: UUID, accessToken: String?, refreshToken: String): TokenPair {
        assertRefreshToken(userId, refreshToken)
        deactivate(userId, accessToken, refreshToken)

        val tokenPair = tokenManager.createTokenPair(userId)

        tokenManager.issueRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }

    fun assertRefreshToken(userId: UUID, refreshToken: String) {
        tokenManager.assertRefreshToken(userId, refreshToken) // device id 추가 논의 필요 (header, metadata etc.)
        blacklistManager.assertToken(userId, refreshToken)
    }


    fun deactivate(userId: UUID, accessToken: String?, refreshToken: String) {
        accessToken?.let { blacklistManager.blacklistToken(userId, accessToken) }
        blacklistManager.blacklistToken(userId, refreshToken)

        tokenManager.deleteByUserIdAndToken(userId, refreshToken) // device id 추가 논의 필요 (header, metadata etc.)
    }
}
