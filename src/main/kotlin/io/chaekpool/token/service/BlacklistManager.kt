package io.chaekpool.token.service

import io.chaekpool.token.entity.BlacklistEntity
import io.chaekpool.token.repository.BlacklistRepository
import org.springframework.stereotype.Service

@Service
class BlacklistManager(
    private val blacklistRepository: BlacklistRepository,
    private val jwtProvider: JwtProvider,
) {

    fun blacklistToken(userId: String, token: String) {
        val expiresIn = jwtProvider.getExpirationTime(token)

        if (expiresIn > 0) {
            val key = "$userId:$token"
            val entity = BlacklistEntity(key, token, expiresIn)

            blacklistRepository.save(entity)
        }
    }

    fun isBlacklisted(userId: String, token: String): Boolean {
        return blacklistRepository.existsById("$userId:$token")
    }
}
