package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.entity.RefreshTokenEntity
import io.chaekpool.auth.token.exception.TokenNotFoundException
import io.chaekpool.auth.token.provider.JwtProvider
import io.chaekpool.auth.token.repository.RefreshTokenRepository
import io.chaekpool.common.exception.internal.ForbiddenException
import io.chaekpool.common.filter.UserMetadataContext
import io.chaekpool.common.util.MaskingUtil.maskIpLastOctets
import io.chaekpool.common.util.isTrueOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenManager(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userMetadataContext: UserMetadataContext,
    private val jwtProvider: JwtProvider
) {

    private val log = KotlinLogging.logger {}

    fun createAccessToken(userId: UUID): String = jwtProvider.createAccessToken(userId)

    fun createTokenPair(userId: UUID, provider: String): TokenPair {
        return TokenPair(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId, provider)
        )
    }

    fun saveRefreshToken(userId: UUID, token: String): RefreshTokenEntity {
        val jti = jwtProvider.getJti(token)
        val metadata by userMetadataContext

        val entity = RefreshTokenEntity(
            jti = jti,
            userId = userId,
            ip = metadata?.ip?.maskIpLastOctets(2),
            userAgent = metadata?.userAgent,
            device = metadata?.device,
            platformType = metadata?.platformType
        )

        return refreshTokenRepository.save(entity)
    }

    fun assertRefreshToken(userId: UUID, refreshToken: String) {
        jwtProvider.assertToken(refreshToken)
        val jti = jwtProvider.getJti(refreshToken)

        val entity = refreshTokenRepository.findById(jti)
            .orElseThrow { TokenNotFoundException() }

        (entity.userId == userId).isTrueOrThrow {
            ForbiddenException("Token userId mismatch")
        }
    }

    fun deleteByJti(jti: String) {
        refreshTokenRepository.deleteById(jti)
    }
}
