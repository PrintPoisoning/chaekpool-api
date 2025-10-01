package io.chaekpool.auth.oauth.service

import io.chaekpool.auth.dto.TokenResponse
import io.chaekpool.auth.oauth.client.kakao.KakaoAuthClient
import io.chaekpool.auth.oauth.client.kakao.KakaoUserClient
import io.chaekpool.auth.oauth.config.OAuthKakaoProperties
import io.chaekpool.auth.oauth.dto.kakao.KakaoTokenResponse
import io.chaekpool.auth.oauth.dto.kakao.KakaoUserResponse
import io.chaekpool.auth.token.service.JwtProvider
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.IdGenerator
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoUserClient: KakaoUserClient,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider,
    private val idGenerator: IdGenerator,
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
        val userId: Long = idGenerator.nextId()

        // save kakaoUser to db

        val accessToken = jwtProvider.createAccessToken(userId)
        val refreshToken = jwtProvider.createRefreshToken(userId)

        tokenManager.issueRefreshToken(userId, refreshToken)

        return TokenResponse(accessToken, refreshToken)
    }
}
