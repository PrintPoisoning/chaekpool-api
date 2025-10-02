package io.chaekpool.auth.token.dto

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
