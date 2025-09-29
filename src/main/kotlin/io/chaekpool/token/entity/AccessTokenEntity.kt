package io.chaekpool.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

@RedisHash(value = "token:access", timeToLive = 900) // 900초 = 15분
data class AccessTokenEntity(
    @Id
    val key: String,
    val userId: String,
    val token: String,
    val issuedAt: Long = Instant.now().epochSecond
)
