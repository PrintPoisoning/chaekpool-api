package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.entity.BlacklistEntity
import io.chaekpool.auth.token.exception.TokenBlacklistedException
import io.chaekpool.auth.token.provider.JwtProvider
import io.chaekpool.auth.token.repository.BlacklistRepository
import io.chaekpool.common.util.isTrueOrThrow
import org.springframework.stereotype.Service

@Service
class BlacklistManager(
    private val blacklistRepository: BlacklistRepository,
    private val jwtProvider: JwtProvider,
) {

    fun blacklistToken(token: String) {
        val jti = jwtProvider.getJti(token)
        val expiresIn = jwtProvider.getExpiresIn(token)

        if (expiresIn > 0) {
            val entity = BlacklistEntity(jti, expiresIn)
            blacklistRepository.save(entity)
        }
    }

    fun assertToken(token: String) {
        jwtProvider.assertToken(token)

        val jti = jwtProvider.getJti(token)
        val isNotBlacklisted = !blacklistRepository.existsById(jti)

        isNotBlacklisted.isTrueOrThrow { TokenBlacklistedException() }
    }
}
