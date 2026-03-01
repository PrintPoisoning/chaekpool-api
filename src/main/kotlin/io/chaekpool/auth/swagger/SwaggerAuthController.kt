package io.chaekpool.auth.swagger

import io.chaekpool.auth.constant.Auth
import io.chaekpool.auth.constant.AuthProvider
import io.chaekpool.auth.oauth2.client.KakaoApiClient
import io.chaekpool.auth.oauth2.client.KakaoAuthClient
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.oauth2.exception.ProviderNotFoundException
import io.chaekpool.auth.oauth2.repository.AuthProviderRepository
import io.chaekpool.auth.oauth2.repository.ProviderAccountRepository
import io.chaekpool.auth.token.provider.CookieProvider
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.Objects

@Hidden
@Profile("local", "dev")
@RestController
@RequestMapping("/api/v1/auth/swagger")
class SwaggerAuthController(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoApiClient: KakaoApiClient,
    private val kakaoAuthProperties: KakaoAuthProperties,
    private val authProviderRepository: AuthProviderRepository,
    private val providerAccountRepository: ProviderAccountRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val cookieProvider: CookieProvider
) {

    @GetMapping("/oauth2/kakao")
    fun authorize(request: HttpServletRequest): ResponseEntity<Unit> {
        val callbackUri = buildCallbackUri(request)

        val authorizeUri = UriComponentsBuilder
            .fromUriString("https://kauth.kakao.com/oauth/authorize")
            .queryParam("client_id", kakaoAuthProperties.clientId)
            .queryParam("redirect_uri", callbackUri)
            .queryParam("response_type", "code")
            .build()
            .toUri()

        return ResponseEntity.status(302).location(authorizeUri).build()
    }

    @GetMapping("/oauth2/kakao/callback", produces = [MediaType.TEXT_HTML_VALUE])
    @Transactional
    fun callback(
        @RequestParam code: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        val callbackUri = buildCallbackUri(request)

        val kakaoToken = kakaoAuthClient.postOAuthToken(
            grantType = AuthorizationGrantType.AUTHORIZATION_CODE.value,
            clientId = kakaoAuthProperties.clientId,
            clientSecret = kakaoAuthProperties.clientSecret,
            redirectUri = callbackUri,
            code = code
        )

        val kakaoAccount = kakaoApiClient.getAccount("${Auth.BEARER_PREFIX}${kakaoToken.accessToken}")
        val accountId = kakaoAccount.id.toString()

        val providerId = authProviderRepository.findByProviderName(AuthProvider.KAKAO)
            ?.id
            .notNullOrThrow { ProviderNotFoundException(AuthProvider.KAKAO) }

        val existingUserId = providerAccountRepository.findByProviderIdAndAccountId(providerId, accountId)?.userId

        val userId = if (Objects.isNull(existingUserId)) {
            val newUser = userRepository.save(
                Users(
                    email = kakaoAccount.kakaoAccount?.email,
                    username = kakaoAccount.kakaoAccount?.profile?.nickname,
                    profileImageUrl = kakaoAccount.kakaoAccount?.profile?.profileImageUrl
                )
            )
            providerAccountRepository.saveProviderAccount(
                userId = newUser.id!!,
                providerId = providerId,
                accountId = accountId,
                accountRegistry = kakaoAccount,
                authRegistry = kakaoToken
            )
            newUser.id
        } else {
            providerAccountRepository.updateAuthRegistry(existingUserId!!, providerId, kakaoToken)
            existingUserId
        }

        userRepository.updateLastLoginAt(userId)

        val tokenPair = tokenManager.createTokenPair(userId, AuthProvider.KAKAO)
        tokenManager.saveRefreshToken(userId, tokenPair.refreshToken)

        val cookie = cookieProvider.refreshTokenCookie(tokenPair.refreshToken)
        response.addHeader(SET_COOKIE, cookie.toString())

        val accessToken = tokenPair.accessToken

        return """
            <!DOCTYPE html>
            <html>
            <head><title>카카오 로그인 완료</title></head>
            <body>
            <p>로그인 완료. 이 창은 자동으로 닫힙니다.</p>
            <script>
                window.opener.postMessage(
                    { type: 'KAKAO_AUTH_COMPLETE', accessToken: '$accessToken' },
                    window.location.origin
                );
                window.close();
            </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildCallbackUri(request: HttpServletRequest): String {
        return UriComponentsBuilder
            .fromUriString(request.requestURL.toString())
            .replacePath("/api/v1/auth/swagger/oauth2/kakao/callback")
            .replaceQuery(null)
            .build()
            .toUriString()
    }
}
