package io.chaekpool.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash(value = "token:blacklist")
data class BlacklistEntity(
    @Id
    val key: String,
    val token: String,

    @TimeToLive
    val expiration: Long
)
