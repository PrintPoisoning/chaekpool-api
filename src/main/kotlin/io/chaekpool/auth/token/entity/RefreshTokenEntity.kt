package io.chaekpool.auth.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.util.UUID

@RedisHash(value = "auth:token:refresh", timeToLive = 604800)
data class RefreshTokenEntity(
    @Id
    val jti: String,

    @Indexed
    val userId: UUID,

    val ip: String?,
    val userAgent: String?,
    val device: String?,
    val platformType: String?
)
