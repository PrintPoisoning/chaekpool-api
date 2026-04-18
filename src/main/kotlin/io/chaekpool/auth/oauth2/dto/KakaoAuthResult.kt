package io.chaekpool.auth.oauth2.dto

import io.chaekpool.auth.token.dto.TokenPair

sealed interface KakaoAuthResult {
    data class Authenticated(val tokenPair: TokenPair) : KakaoAuthResult
    data class RejoinRequired(val ticketId: String) : KakaoAuthResult
}
