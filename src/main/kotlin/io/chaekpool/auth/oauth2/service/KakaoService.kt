package io.chaekpool.auth.oauth2.service

import io.chaekpool.auth.constant.AuthProviderConstant
import io.chaekpool.auth.exception.ProviderNotFoundException
import io.chaekpool.auth.oauth2.client.KakaoApiClient
import io.chaekpool.auth.oauth2.client.KakaoAuthClient
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.oauth2.dto.KakaoAuthTokenResponse
import io.chaekpool.auth.repository.AuthProviderRepository
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.user.repository.UserAccountRepository
import io.chaekpool.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

@Service
class KakaoService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoApiClient: KakaoApiClient,
    private val authProviderRepository: AuthProviderRepository,
    private val userRepository: UserRepository,
    private val userAccountRepository: UserAccountRepository,
    private val tokenManager: TokenManager,
    private val jsonMapper: JsonMapper,
    private val props: KakaoAuthProperties
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun authenticate(code: String): TokenPair {
        val kakaoToken = getKakaoTokens(code)
        val kakaoUser = getKakaoUser(kakaoToken.accessToken)
        val kakaoId = kakaoUser.id

        val providerId = getKakaoProviderId()
        val existingUserId = userAccountRepository.findByProviderAndAccountId(providerId, kakaoId.toString())?.userId

        val userId = if (existingUserId != null) {
            handleExistingUser(existingUserId, providerId, kakaoId, kakaoToken)
        } else {
            handleNewUser(providerId, kakaoId, kakaoUser, kakaoToken)
        }

        val tokenPair = tokenManager.createTokenPair(userId)
        tokenManager.saveRefreshToken(userId, tokenPair.refreshToken) // todo: 메서드 명 바꿔서 test 도 바꿔야함

        return tokenPair
    }

    private fun getKakaoTokens(code: String): KakaoAuthTokenResponse {
        return kakaoAuthClient.postOAuthToken(
            grantType = AuthorizationGrantType.AUTHORIZATION_CODE.value,
            clientId = props.clientId,
            clientSecret = props.clientSecret,
            redirectUri = props.redirectUri,
            code = code
        )
    }

    private fun getKakaoUser(accessToken: String) = kakaoApiClient.getUser("Bearer $accessToken")

    private fun handleExistingUser(
        userId: UUID,
        providerId: UUID,
        kakaoId: Long,
        kakaoToken: KakaoAuthTokenResponse
    ): UUID {
        log.info { "Existing user login: userId=$userId, kakaoId=$kakaoId" }
        userRepository.updateLastLoginAt(userId)
        userAccountRepository.updateAuthRegistry(userId, providerId, kakaoToken)
        return userId
    }

    private fun handleNewUser(
        providerId: UUID,
        kakaoId: Long,
        kakaoUser: io.chaekpool.auth.oauth2.dto.KakaoApiUserResponse,
        kakaoToken: KakaoAuthTokenResponse
    ): UUID {
        val email = kakaoUser.kakaoAccount?.email
        val nickname = kakaoUser.kakaoAccount?.profile?.nickname
        val profileImageUrl = kakaoUser.kakaoAccount?.profile?.profileImageUrl

        log.info { "New user sign-up: kakaoId=$kakaoId, email=$email" }

        val newUser = userRepository.save(
            io.chaekpool.generated.jooq.tables.pojos.Users(
                email = email,
                username = nickname,
                profileImageUrl = profileImageUrl
            )
        )

        userAccountRepository.save(
            userId = newUser.id!!,
            providerId = providerId,
            accountId = kakaoId.toString(),
            accountRegistry = kakaoUser,
            authRegistry = kakaoToken
        )

        userRepository.updateLastLoginAt(newUser.id)
        return newUser.id
    }

    private fun getKakaoProviderId(): UUID {
        return authProviderRepository.findByProviderName(AuthProviderConstant.KAKAO)
            ?.id
            .notNullOrThrow { ProviderNotFoundException(AuthProviderConstant.KAKAO) }
    }

    fun getOAuthRefreshToken(userId: UUID): String? {
        val providerId = getKakaoProviderId()
        val userAccount = userAccountRepository.findByUserId(userId, providerId) ?: return null

        val authRegistryJson = userAccount.authRegistry?.data() ?: return null
        val kakaoToken = jsonMapper.readValue(authRegistryJson, KakaoAuthTokenResponse::class.java)
        return kakaoToken.refreshToken
    }

    @Transactional
    fun refreshWithOAuthToken(userId: UUID): TokenPair {
        val providerId = getKakaoProviderId()
        val userAccount = userAccountRepository.findByUserId(userId, providerId)
            .notNullOrThrow { io.chaekpool.common.exception.internal.NotFoundException("사용자 OAuth 계정을 찾을 수 없습니다") }

        val authRegistryJson = userAccount.authRegistry?.data()
            .notNullOrThrow { io.chaekpool.common.exception.internal.NotFoundException("OAuth 토큰을 찾을 수 없습니다") }
        val kakaoToken = jsonMapper.readValue(authRegistryJson, KakaoAuthTokenResponse::class.java)

        val kakaoUser = getKakaoUser(kakaoToken.accessToken)
        log.info { "OAuth token refresh: userId=$userId, kakaoId=${kakaoUser.id}" }

        userRepository.updateLastLoginAt(userId)

        return issueTokenPair(userId)
    }
}
