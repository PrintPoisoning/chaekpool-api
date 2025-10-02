package io.chaekpool.auth.oauth.service

import io.chaekpool.auth.oauth.client.kakao.KakaoAuthClient
import io.chaekpool.auth.oauth.client.kakao.KakaoUserClient
import io.chaekpool.auth.oauth.config.OAuthKakaoProperties
import io.chaekpool.auth.oauth.dto.kakao.KakaoTokenResponse
import io.chaekpool.auth.oauth.dto.kakao.KakaoUserResponse
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.IdGenerator
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoUserClient: KakaoUserClient,
    private val tokenManager: TokenManager,
    private val idGenerator: IdGenerator,
    private val props: OAuthKakaoProperties
) {

    fun authenticateWithKakao(code: String): TokenPair {
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

        val tokenPair = tokenManager.createTokenPair(userId)

        tokenManager.issueRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }
}
