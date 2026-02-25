package io.chaekpool.auth.oauth2.service

import io.chaekpool.auth.constant.AuthConstant.BEARER_PREFIX
import io.chaekpool.auth.oauth2.client.KakaoApiClient
import io.chaekpool.auth.oauth2.client.KakaoAuthClient
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.UuidV7Util
import io.chaekpool.user.service.UserService
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.stereotype.Service

@Service
class KakaoService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoApiClient: KakaoApiClient,
    private val tokenManager: TokenManager,
    private val userService: UserService,
    private val props: KakaoAuthProperties
) {

    fun authenticate(code: String): TokenPair {
        val kakaoToken = kakaoAuthClient.postOAuthToken(
            clientId = props.clientId,
            clientSecret = props.clientSecret,
            redirectUri = props.redirectUri,
            code = code,
            grantType = AuthorizationGrantType.AUTHORIZATION_CODE.value
        )

        val kakaoUser = kakaoApiClient.getUser("$BEARER_PREFIX${kakaoToken.accessToken}")
        val userId = UuidV7Util.generate() // todo: userService.save()

        val tokenPair = tokenManager.createTokenPair(userId)
        tokenManager.issueRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }
}
