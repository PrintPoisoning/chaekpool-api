package io.chaekpool.auth.oauth2.service

import io.chaekpool.auth.constant.Auth
import io.chaekpool.auth.constant.AuthProvider
import io.chaekpool.auth.oauth2.client.KakaoApiClient
import io.chaekpool.auth.oauth2.client.KakaoAuthClient
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.oauth2.dto.KakaoApiAccountResponse
import io.chaekpool.auth.oauth2.dto.KakaoAuthResult
import io.chaekpool.auth.oauth2.dto.KakaoAuthTokenResponse
import io.chaekpool.auth.oauth2.entity.RejoinTicketEntity
import io.chaekpool.auth.oauth2.exception.ProviderNotFoundException
import io.chaekpool.auth.oauth2.exception.RejoinTicketNotFoundException
import io.chaekpool.auth.oauth2.repository.AuthProviderRepository
import io.chaekpool.auth.oauth2.repository.ProviderAccountRepository
import io.chaekpool.auth.oauth2.repository.RejoinTicketRepository
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.HandleGenerator
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.common.util.isTrueOrThrow
import io.chaekpool.common.util.notNullOrThrow
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.user.exception.UserNotFoundException
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
    private val rejoinTicketRepository: RejoinTicketRepository,
    private val tokenManager: TokenManager,
    private val props: KakaoAuthProperties,
    private val jsonMapper: JsonMapper
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun authenticate(code: String): KakaoAuthResult {
        val kakaoToken = getKakaoTokens(code)
        val kakaoAccount = getKakaoAccount(kakaoToken.accessToken)
        val accountId = kakaoAccount.id.toString()

        val providerId = getKakaoProviderId()
        val existingProviderAccount = providerAccountRepository.findByProviderIdAndAccountId(providerId, accountId)

        if (Objects.nonNull(existingProviderAccount)) {
            val existingUserId = existingProviderAccount!!.userId
            val existingUser = userRepository.findById(existingUserId)
                .notNullOrThrow { UserNotFoundException() }

            if (existingUser.status == UserStatusType.LEAVED) {
                val ticketId = issueRejoinTicket(providerId, accountId, existingUserId, kakaoToken, kakaoAccount)
                log.info { "[USER_REJOIN_REQUIRED] leavedUserId=$existingUserId ticketId=$ticketId" }
                return KakaoAuthResult.RejoinRequired(ticketId)
            }

            updateProviderAccount(existingUserId, providerId, kakaoToken)
            return KakaoAuthResult.Authenticated(issueAppTokens(existingUserId))
        }

        val newUserId = createProviderAccount(providerId, accountId, kakaoAccount, kakaoToken)
        return KakaoAuthResult.Authenticated(issueAppTokens(newUserId))
    }

    @Transactional
    fun rejoinWithRestore(rejoinTicket: String): TokenPair {
        val ticket = findRejoinTicket(rejoinTicket)
        val kakaoToken = jsonMapper.readValue(ticket.kakaoAuthTokenResponseJson, KakaoAuthTokenResponse::class.java)
        val kakaoAccount = jsonMapper.readValue(ticket.kakaoAccountResponseJson, KakaoApiAccountResponse::class.java)
        val userId = ticket.leavedUserId

        val restored = userRepository.restoreById(
            userId = userId,
            nickname = kakaoAccount.kakaoAccount?.profile?.nickname,
            profileImageUrl = kakaoAccount.kakaoAccount?.profile?.profileImageUrl,
            thumbnailImageUrl = kakaoAccount.kakaoAccount?.profile?.thumbnailImageUrl
        )
        (restored > 0).isTrueOrThrow { RejoinTicketNotFoundException() }

        providerAccountRepository.updateAuthRegistry(userId, ticket.providerId, kakaoToken)
        providerAccountRepository.updateAccountRegistry(userId, ticket.providerId, kakaoAccount)

        rejoinTicketRepository.deleteById(rejoinTicket)

        val tokenPair = issueAppTokens(userId)
        log.info { "[USER_REJOIN_RESTORE] userId=$userId" }
        return tokenPair
    }

    @Transactional
    fun rejoinWithFresh(rejoinTicket: String): TokenPair {
        val ticket = findRejoinTicket(rejoinTicket)
        val kakaoToken = jsonMapper.readValue(ticket.kakaoAuthTokenResponseJson, KakaoAuthTokenResponse::class.java)
        val kakaoAccount = jsonMapper.readValue(ticket.kakaoAccountResponseJson, KakaoApiAccountResponse::class.java)
        val leavedUserId = ticket.leavedUserId

        userRepository.anonymizeById(leavedUserId)
        providerAccountRepository.deleteByUserId(leavedUserId)

        val newUserId = createProviderAccount(ticket.providerId, ticket.accountId, kakaoAccount, kakaoToken)

        rejoinTicketRepository.deleteById(rejoinTicket)

        val tokenPair = issueAppTokens(newUserId)
        log.info { "[USER_REJOIN_FRESH] oldUserId=$leavedUserId newUserId=$newUserId" }
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
        kakaoId: String,
        kakaoUser: KakaoApiAccountResponse,
        kakaoToken: KakaoAuthTokenResponse
    ): UUID {
        val newUser = userRepository.save(
            io.chaekpool.generated.jooq.tables.pojos.Users(
                email = kakaoUser.kakaoAccount?.email,
                nickname = kakaoUser.kakaoAccount?.profile?.nickname,
                handle = HandleGenerator.generateUnique { userRepository.existsByHandle(it) },
                profileImageUrl = kakaoUser.kakaoAccount?.profile?.profileImageUrl,
                thumbnailImageUrl = kakaoUser.kakaoAccount?.profile?.thumbnailImageUrl
            )
        )

        providerAccountRepository.save(
            userId = newUser.id!!,
            providerId = providerId,
            accountId = kakaoId,
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

    private fun issueAppTokens(userId: UUID): TokenPair {
        userRepository.updateLastLoginAt(userId)
        val tokenPair = tokenManager.createTokenPair(userId, AuthProvider.KAKAO)
        tokenManager.saveRefreshToken(userId, tokenPair.refreshToken)
        return tokenPair
    }

    private fun issueRejoinTicket(
        providerId: UUID,
        accountId: String,
        leavedUserId: UUID,
        kakaoToken: KakaoAuthTokenResponse,
        kakaoAccount: KakaoApiAccountResponse
    ): String {
        val entity = RejoinTicketEntity(
            ticketId = UUIDv7.generate().toString(),
            providerId = providerId,
            accountId = accountId,
            leavedUserId = leavedUserId,
            kakaoAuthTokenResponseJson = jsonMapper.writeValueAsString(kakaoToken),
            kakaoAccountResponseJson = jsonMapper.writeValueAsString(kakaoAccount)
        )
        return rejoinTicketRepository.save(entity).ticketId
    }

    private fun findRejoinTicket(rejoinTicket: String): RejoinTicketEntity {
        return rejoinTicketRepository.findById(rejoinTicket)
            .orElseThrow { RejoinTicketNotFoundException() }
    }
}
