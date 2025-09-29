package io.chaekpool.token.service

import io.chaekpool.token.entity.AccessTokenEntity
import io.chaekpool.token.entity.RefreshTokenEntity
import io.chaekpool.token.repository.AccessTokenRepository
import io.chaekpool.token.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenManager(
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    fun saveAccessToken(userId: String, token: String): AccessTokenEntity {
        val key = "$userId:${UUID.randomUUID()}"
        val entity = AccessTokenEntity(key, userId, token)

        return accessTokenRepository.save(entity)
    }

    fun getAccessTokens(userId: String): List<AccessTokenEntity> =
        accessTokenRepository.findByUserId(userId)

    fun deleteAccessToken(key: String) =
        accessTokenRepository.deleteById(key)

    fun saveRefreshToken(
        userId: String,
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

    fun getRefreshTokens(userId: String): List<RefreshTokenEntity> =
        refreshTokenRepository.findByUserId(userId)

    fun deleteRefreshToken(key: String) =
        refreshTokenRepository.deleteById(key)

    fun deleteAllRefreshTokens(userId: String) =
        refreshTokenRepository.findByUserId(userId).forEach { refreshTokenRepository.deleteById(it.key) }

    fun validateRefreshToken(userId: String, token: String): Boolean {
        return refreshTokenRepository.findByUserId(userId).any { it.token == token }
    }
}
