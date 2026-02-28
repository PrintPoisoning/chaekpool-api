package io.chaekpool.auth.oauth2.service

import io.chaekpool.auth.constant.Auth
import io.chaekpool.auth.constant.AuthProvider
import io.chaekpool.auth.oauth2.client.KakaoApiClient
import io.chaekpool.auth.oauth2.client.KakaoAuthClient
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.oauth2.dto.KakaoAuthTokenResponse
import io.chaekpool.auth.oauth2.exception.ProviderNotFoundException
import io.chaekpool.auth.oauth2.repository.AuthProviderRepository
import io.chaekpool.auth.oauth2.repository.ProviderAccountRepository
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import java.util.Objects
import java.util.UUID

@Service
class KakaoService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoApiClient: KakaoApiClient,
    private val authProviderRepository: AuthProviderRepository,
    private val userRepository: UserRepository,
    private val providerAccountRepository: ProviderAccountRepository,
    private val tokenManager: TokenManager,
    private val props: KakaoAuthProperties,
    private val jsonMapper: JsonMapper
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun authenticate(code: String): TokenPair {
        val kakaoToken = getKakaoTokens(code)
        val kakaoAccount = getKakaoAccount(kakaoToken.accessToken)
        val accountId = kakaoAccount.id

        val providerId = getKakaoProviderId()
        val existingUserId =
            providerAccountRepository.findByProviderAndAccountId(providerId, accountId.toString())?.userId

        val userId = if (Objects.isNull(existingUserId)) {
            createProviderAccount(providerId, accountId, kakaoAccount, kakaoToken)
        } else {
            updateProviderAccount(existingUserId!!, providerId, kakaoToken)
        }

        userRepository.updateLastLoginAt(userId)

        val tokenPair = tokenManager.createTokenPair(userId, AuthProvider.KAKAO)
        tokenManager.saveRefreshToken(userId, tokenPair.refreshToken)

        return tokenPair
    }

    @Transactional(readOnly = true)
    fun getKakaoProviderId(): UUID {
        return authProviderRepository.findByProviderName(AuthProvider.KAKAO)
            ?.id
            .notNullOrThrow { ProviderNotFoundException(AuthProvider.KAKAO) }
    }

    @Transactional
    fun refreshOAuthTokens(userId: UUID) {
        val providerId = getKakaoProviderId()

        val providerAccount = providerAccountRepository.findByUserIdAndProviderId(userId, providerId)
            .notNullOrThrow { ProviderNotFoundException(AuthProvider.KAKAO) }

        val currentAuth = jsonMapper.readValue(
            providerAccount.authRegistry?.data(),
            KakaoAuthTokenResponse::class.java
        )

        val refreshedAuth = kakaoAuthClient.postRefreshToken(
            clientId = props.clientId,
            clientSecret = props.clientSecret,
            refreshToken = currentAuth.refreshToken
        )

        val mergedAuth = KakaoAuthTokenResponse(
            tokenType = refreshedAuth.tokenType,
            accessToken = refreshedAuth.accessToken,
            idToken = currentAuth.idToken,
            expiresIn = refreshedAuth.expiresIn,
            refreshToken = refreshedAuth.refreshToken ?: currentAuth.refreshToken,
            refreshTokenExpiresIn = refreshedAuth.refreshTokenExpiresIn ?: currentAuth.refreshTokenExpiresIn,
            scope = currentAuth.scope
        )

        providerAccountRepository.updateAuthRegistry(userId, providerId, mergedAuth)

        val kakaoAccount = getKakaoAccount(refreshedAuth.accessToken)
        providerAccountRepository.updateAccountRegistry(userId, providerId, kakaoAccount)
    }

    fun getKakaoAccount(accessToken: String) = kakaoApiClient.getAccount("${Auth.BEARER_PREFIX}$accessToken")

    private fun getKakaoTokens(code: String): KakaoAuthTokenResponse {
        return kakaoAuthClient.postOAuthToken(
            grantType = AuthorizationGrantType.AUTHORIZATION_CODE.value,
            clientId = props.clientId,
            clientSecret = props.clientSecret,
            redirectUri = props.redirectUri,
            code = code
        )
    }

    @Transactional
    fun createProviderAccount(
        providerId: UUID,
        kakaoId: Long,
        kakaoUser: io.chaekpool.auth.oauth2.dto.KakaoApiAccountResponse,
        kakaoToken: KakaoAuthTokenResponse
    ): UUID {
        val newUser = userRepository.save(
            io.chaekpool.generated.jooq.tables.pojos.Users(
                email = kakaoUser.kakaoAccount?.email,
                username = kakaoUser.kakaoAccount?.profile?.nickname,
                profileImageUrl = kakaoUser.kakaoAccount?.profile?.profileImageUrl
            )
        )

        providerAccountRepository.saveProviderAccount(
            userId = newUser.id!!,
            providerId = providerId,
            accountId = kakaoId.toString(),
            accountRegistry = kakaoUser,
            authRegistry = kakaoToken
        )

        return newUser.id
    }

    @Transactional
    fun updateProviderAccount(
        userId: UUID,
        providerId: UUID,
        kakaoAuthRegistry: KakaoAuthTokenResponse
    ): UUID {
        providerAccountRepository.updateAuthRegistry(userId, providerId, kakaoAuthRegistry)
        return userId
    }
}
