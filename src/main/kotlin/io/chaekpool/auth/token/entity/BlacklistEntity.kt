package io.chaekpool.auth.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash(value = "auth:token:blacklist")
data class BlacklistEntity(
    @Id
    val jti: String,

    @TimeToLive
    val expiresIn: Long
)
