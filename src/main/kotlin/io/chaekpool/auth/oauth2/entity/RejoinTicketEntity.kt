package io.chaekpool.auth.oauth2.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.util.UUID

@RedisHash(value = "auth:rejoin:ticket", timeToLive = 300)
data class RejoinTicketEntity(
    @Id
    val ticketId: String,

    val providerId: UUID,
    val accountId: String,
    val leavedUserId: UUID,
    val kakaoAuthTokenResponseJson: String,
    val kakaoAccountResponseJson: String
)
