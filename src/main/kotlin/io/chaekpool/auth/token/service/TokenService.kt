package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.repository.RefreshTokenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenService(
    private val blacklistManager: BlacklistManager,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    private val log = KotlinLogging.logger {}

    fun refresh(userId: UUID, accessToken: String?, refreshToken: String): TokenPair {
        tokenManager.assertRefreshToken(userId, refreshToken)
        deactivate(userId, accessToken, refreshToken)

        val tokenPair = tokenManager.createTokenPair(userId)

        tokenManager.saveRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }

    // 문제 ㅈㄴ 많음 - OAuth 제공자에서 토큰 갱신 실패 시 일반 토큰 갱신으로 fallback
    fun refreshWithOAuth(
        userId: UUID,
        accessToken: String?,
        refreshToken: String,
        oauthRefreshFn: (UUID) -> TokenPair
    ): TokenPair {
        return try {
            log.info { "Attempting OAuth token refresh for userId=$userId" }
            val tokenPair = oauthRefreshFn(userId)
            deactivate(userId, accessToken, refreshToken)
            tokenPair
        } catch (e: Exception) {
            log.warn(e) { "OAuth token refresh failed for userId=$userId, falling back to normal refresh" }
            refresh(userId, accessToken, refreshToken)
        }
    }

    fun deactivate(userId: UUID, accessToken: String?, refreshToken: String) {
        accessToken?.let { blacklistManager.blacklistToken(userId, accessToken) }
        blacklistManager.blacklistToken(userId, refreshToken)

        val jti = jwtProvider.getJti(refreshToken)
        tokenManager.deleteByJti(jti)
    }
}
