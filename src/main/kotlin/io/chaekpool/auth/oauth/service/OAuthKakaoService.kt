package io.chaekpool.auth.oauth.service

import io.chaekpool.auth.oauth.client.kakao.KakaoAuthClient
import io.chaekpool.auth.oauth.client.kakao.KakaoUserClient
import io.chaekpool.auth.oauth.config.OAuthKakaoProperties
import io.chaekpool.auth.oauth.dto.kakao.KakaoTokenResponse
import io.chaekpool.auth.oauth.dto.kakao.KakaoUserResponse
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class KakaoOAuthService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoUserClient: KakaoUserClient,
    private val tokenManager: TokenManager,
    private val userService: UserService,
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

        val tokenExpiry: LocalDateTime = LocalDateTime.now().plusSeconds(kakaoToken.expiresIn)

        val userId: UUID = userService.save(
            providerName = "KAKAO",
            providerUserId = kakaoUser.id.toString(),
            email = kakaoUser.kakaoAccount?.email,
            profileImageUrl = kakaoUser.kakaoAccount?.profile?.profileImageUrl,
            kakaoAccessToken = kakaoToken.accessToken,
            kakaoRefreshToken = kakaoToken.refreshToken,
            tokenExpiry = tokenExpiry
        )

        val tokenPair = tokenManager.createTokenPair(userId)

        tokenManager.issueRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }
}
