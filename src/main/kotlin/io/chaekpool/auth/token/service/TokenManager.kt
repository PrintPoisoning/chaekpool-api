package io.chaekpool.auth.token.service

import java.util.UUID
import io.chaekpool.auth.token.entity.RefreshTokenEntity
import io.chaekpool.auth.token.repository.RefreshTokenRepository
import io.chaekpool.common.filter.UserMetadataContext
import io.chaekpool.common.logger.LoggerDelegate
import org.springframework.stereotype.Service

@Service
class TokenManager(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userMetadataContext: UserMetadataContext
) {

    private val log by LoggerDelegate()

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

    fun getRefreshTokens(userId: Long): List<RefreshTokenEntity> {
        return refreshTokenRepository.findByUserId(userId)
    }

    fun deleteRefreshToken(key: String) {
        refreshTokenRepository.deleteById(key)
    }

    fun deleteAllRefreshTokens(userId: Long) {
        refreshTokenRepository.findByUserId(userId).forEach { refreshTokenRepository.deleteById(it.key) }
    }

    fun validateRefreshToken(userId: Long, token: String): Boolean {
        return refreshTokenRepository.findByUserId(userId).any { it.token == token }
    }
}
