package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.provider.JwtProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenService(
    private val blacklistManager: BlacklistManager,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider
) {

    private val log = KotlinLogging.logger {}

    fun refresh(userId: UUID, accessToken: String?, refreshToken: String): TokenPair {
        tokenManager.assertRefreshToken(userId, refreshToken)
        accessToken?.let { blacklistManager.blacklistToken(it) }

        val newAccessToken = tokenManager.createAccessToken(userId)
        return TokenPair(newAccessToken, refreshToken)
    }

    fun deactivate(userId: UUID, accessToken: String?, refreshToken: String) {
        accessToken?.let { blacklistManager.blacklistToken(it) }
        blacklistManager.blacklistToken(refreshToken)

        val jti = jwtProvider.getJti(refreshToken)
        tokenManager.deleteByJti(jti)
    }
}
