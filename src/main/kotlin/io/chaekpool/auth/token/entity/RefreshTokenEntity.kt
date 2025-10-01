package io.chaekpool.auth.token.entity

import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@RedisHash(value = "token:refresh", timeToLive = 604800) // 7일 = 604800초
data class RefreshTokenEntity(
    @Id
    val key: String,

    @Indexed
    val userId: Long,

    val token: String,

    val issuedAt: Long = Instant.now().epochSecond,
)
