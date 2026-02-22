package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.entity.BlacklistEntity
import io.chaekpool.auth.token.exception.TokenBlacklistedException
import io.chaekpool.auth.token.repository.BlacklistRepository
import io.chaekpool.common.util.isTrueOrThrow
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BlacklistManager(
    private val blacklistRepository: BlacklistRepository,
    private val jwtProvider: JwtProvider,
) {

    fun blacklistToken(userId: UUID, token: String) {
        val expiresIn = jwtProvider.getExpirationTime(token)

        if (expiresIn > 0) {
            val key = "$userId:$token"
            val entity = BlacklistEntity(key, token, expiresIn)

            blacklistRepository.save(entity)
        }
    }

    fun assertToken(userId: UUID, token: String) {
        val isNotBlacklisted = !blacklistRepository.existsById("$userId:$token")

        isNotBlacklisted.isTrueOrThrow { TokenBlacklistedException() }
    }
}
