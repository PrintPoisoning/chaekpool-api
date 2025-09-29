package io.chaekpool.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

@RedisHash(value = "token:refresh", timeToLive = 604800) // 7일 = 604800초
data class RefreshTokenEntity(
    @Id
    val key: String,
    val userId: String,
    val token: String,
    val issuedAt: Long = Instant.now().epochSecond,

    val device: String? = null,
    val ip: String? = null,
    val userAgent: String? = null
)
