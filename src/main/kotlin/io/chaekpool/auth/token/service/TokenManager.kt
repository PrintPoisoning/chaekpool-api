package io.chaekpool.auth.token.service

import java.util.UUID
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.entity.RefreshTokenEntity
import io.chaekpool.auth.token.exception.InvalidTokenException
import io.chaekpool.auth.token.exception.TokenNotFoundException
import io.chaekpool.auth.token.repository.RefreshTokenRepository
import io.chaekpool.common.filter.UserMetadataContext
import io.chaekpool.common.logger.LoggerDelegate
import io.chaekpool.common.util.isTrueOrThrow
import org.springframework.stereotype.Service

@Service
class TokenManager(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userMetadataContext: UserMetadataContext,
    private val jwtProvider: JwtProvider
) {

    private val log by LoggerDelegate()

    fun createTokenPair(userId: Long): TokenPair {
        return TokenPair(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId)
        )
    }

    fun issueRefreshToken(userId: Long, token: String): RefreshTokenEntity {
        val key = "$userId:${UUID.randomUUID()}"
        val entity = RefreshTokenEntity(
            key = key,
            userId = userId,
            token = token,
        )

        val metadata by userMetadataContext

        // TODO: metadata db 추가 예정
        log.debug("[TokenManager] metadata={}", metadata)

        return refreshTokenRepository.save(entity)
    }

    fun assertRefreshToken(userId: Long, refreshToken: String) {
        val isValid = jwtProvider.validateToken(refreshToken)
        val isExist = existsByUserIdAndToken(userId, refreshToken)

        isValid.isTrueOrThrow { InvalidTokenException() }
        isExist.isTrueOrThrow { TokenNotFoundException() }
    }

    fun findByUserIdAndToken(userId: Long, token: String): RefreshTokenEntity? {
        return refreshTokenRepository.findByUserId(userId)
            .firstOrNull { it.token == token }
    }

    fun existsByUserIdAndToken(userId: Long, token: String): Boolean {
        return findByUserIdAndToken(userId, token) != null
    }

    fun deleteByUserIdAndToken(userId: Long, token: String) {
        findByUserIdAndToken(userId, token)?.let { refreshTokenRepository.delete(it) }
    }
}
