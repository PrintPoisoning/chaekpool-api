package io.chaekpool.auth.service

import io.chaekpool.auth.client.kakao.KakaoAuthClient
import io.chaekpool.auth.client.kakao.KakaoUserClient
import io.chaekpool.auth.config.OAuthKakaoProperties
import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.dto.kakao.KakaoTokenResponse
import io.chaekpool.auth.dto.kakao.KakaoUserResponse
import io.chaekpool.token.service.JwtProvider
import io.chaekpool.token.service.TokenManager
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoUserClient: KakaoUserClient,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider,
    private val props: OAuthKakaoProperties
) {

    fun authenticateWithKakao(code: String): TokenResponse {
        val kakaoToken: KakaoTokenResponse = kakaoAuthClient.getAccessToken(
            clientId = props.clientId,
            clientSecret = props.clientSecret,
            redirectUri = props.redirectUri,
            code = code,
            grantType = "authorization_code"
        )

        val kakaoUser: KakaoUserResponse = kakaoUserClient.getUserInfo("Bearer ${kakaoToken.accessToken}")
        val userId = "kakao:${kakaoUser.id}"

        val accessToken = jwtProvider.createAccessToken(userId)
        val refreshToken = jwtProvider.createRefreshToken(userId)

        tokenManager.saveAccessToken(userId, accessToken)
        tokenManager.saveRefreshToken(userId, refreshToken)

        return TokenResponse(accessToken, refreshToken)
    }
}
