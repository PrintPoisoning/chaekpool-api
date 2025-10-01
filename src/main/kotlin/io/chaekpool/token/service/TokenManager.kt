package io.chaekpool.token.service

import io.chaekpool.token.entity.RefreshTokenEntity
import io.chaekpool.token.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenManager(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun saveRefreshToken(
        userId: Long,
        token: String,
        device: String? = null,
        ip: String? = null,
        userAgent: String? = null
    ): RefreshTokenEntity {
        val key = "$userId:${UUID.randomUUID()}"
        val entity = RefreshTokenEntity(
            key = key,
            userId = userId,
            token = token,
            device = device,
            ip = ip,
            userAgent = userAgent
        )

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
