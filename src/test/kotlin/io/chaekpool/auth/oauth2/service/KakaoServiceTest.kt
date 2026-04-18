package io.chaekpool.auth.oauth2.service

import io.chaekpool.auth.constant.AuthProvider
import io.chaekpool.auth.oauth2.client.KakaoApiClient
import io.chaekpool.auth.oauth2.client.KakaoAuthClient
import io.chaekpool.auth.oauth2.config.KakaoAuthProperties
import io.chaekpool.auth.oauth2.dto.KakaoApiAccount
import io.chaekpool.auth.oauth2.dto.KakaoApiAccountResponse
import io.chaekpool.auth.oauth2.dto.KakaoApiProfile
import io.chaekpool.auth.oauth2.dto.KakaoAuthRefreshTokenResponse
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
import io.chaekpool.common.exception.external.ExternalBadRequestException
import io.chaekpool.common.exception.internal.NotFoundException
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.generated.jooq.tables.pojos.AuthProviders
import io.chaekpool.generated.jooq.tables.pojos.ProviderAccounts
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.jooq.JSONB
import org.springframework.http.HttpStatus
import tools.jackson.databind.json.JsonMapper
import java.util.Optional

class KakaoServiceTest : BehaviorSpec({

    lateinit var kakaoAuthClient: KakaoAuthClient
    lateinit var kakaoApiClient: KakaoApiClient
    lateinit var authProviderRepository: AuthProviderRepository
    lateinit var userRepository: UserRepository
    lateinit var providerAccountRepository: ProviderAccountRepository
    lateinit var rejoinTicketRepository: RejoinTicketRepository
    lateinit var tokenManager: TokenManager
    lateinit var props: KakaoAuthProperties
    lateinit var jsonMapper: JsonMapper
    lateinit var kakaoService: KakaoService

    beforeTest {
        kakaoAuthClient = mockk()
        kakaoApiClient = mockk()
        authProviderRepository = mockk()
        userRepository = mockk()
        providerAccountRepository = mockk()
        rejoinTicketRepository = mockk()
        tokenManager = mockk()
        props = KakaoAuthProperties(
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
            redirectUri = "http://localhost/callback"
        )
        jsonMapper = mockk()
        kakaoService = KakaoService(
            kakaoAuthClient,
            kakaoApiClient,
            authProviderRepository,
            userRepository,
            providerAccountRepository,
            rejoinTicketRepository,
            tokenManager,
            props,
            jsonMapper
        )
    }

    Given("신규 카카오 사용자가 인증할 때") {
        val code = "auth-code"
        val kakaoId = 12345L

        When("authenticate를 호출하면") {
            Then("새 사용자와 provider account를 생성하고 Authenticated 결과를 반환한다") {
                val providerId = UUIDv7.generate()
                val newUserId = UUIDv7.generate()
                val kakaoToken = KakaoAuthTokenResponse(
                    tokenType = "bearer",
                    accessToken = "kakao-access-token",
                    idToken = null,
                    expiresIn = 3600,
                    refreshToken = "kakao-refresh-token",
                    refreshTokenExpiresIn = 604800,
                    scope = null
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = kakaoId,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                )
                val newUser = Users(id = newUserId, handle = "user_test1234")
                val tokenPair = TokenPair("access-token", "refresh-token")

                every { kakaoAuthClient.postOAuthToken(any(), any(), any(), any(), any()) } returns kakaoToken
                every { kakaoApiClient.getAccount(any()) } returns kakaoAccount
                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns AuthProviders(
                    id = providerId, providerName = AuthProvider.KAKAO, description = "카카오 소셜 로그인"
                )
                every {
                    providerAccountRepository.findByProviderIdAndAccountId(
                        providerId,
                        kakaoId.toString()
                    )
                } returns null
                every { userRepository.existsByHandle(any()) } returns false
                every { userRepository.save(any()) } returns newUser
                every { providerAccountRepository.save(any(), any(), any(), any(), any()) } just runs
                every { userRepository.updateLastLoginAt(newUserId) } returns 1
                every { tokenManager.createTokenPair(newUserId, AuthProvider.KAKAO) } returns tokenPair
                every { tokenManager.saveRefreshToken(newUserId, tokenPair.refreshToken) } returns mockk()

                val result = kakaoService.authenticate(code)

                result shouldBe KakaoAuthResult.Authenticated(tokenPair)
                verify(exactly = 1) { userRepository.save(any()) }
                verify(exactly = 1) {
                    providerAccountRepository.save(
                        newUserId, providerId, kakaoId.toString(), kakaoAccount, kakaoToken
                    )
                }
                verify(exactly = 1) { tokenManager.createTokenPair(newUserId, AuthProvider.KAKAO) }
            }
        }
    }

    Given("기존 활성 카카오 사용자가 인증할 때") {
        val code = "auth-code"
        val kakaoId = 67890L

        When("authenticate를 호출하면") {
            Then("기존 provider account의 auth registry를 갱신하고 Authenticated를 반환한다") {
                val providerId = UUIDv7.generate()
                val existingUserId = UUIDv7.generate()
                val kakaoToken = KakaoAuthTokenResponse(
                    tokenType = "bearer",
                    accessToken = "kakao-access-token",
                    idToken = null,
                    expiresIn = 3600,
                    refreshToken = "kakao-refresh-token",
                    refreshTokenExpiresIn = 604800,
                    scope = null
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = kakaoId,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                )
                val existingProviderAccount = ProviderAccounts(
                    userId = existingUserId,
                    providerId = providerId,
                    accountId = kakaoId.toString()
                )
                val existingUser = Users(
                    id = existingUserId,
                    handle = "user_exist1234",
                    status = UserStatusType.ACTIVE
                )
                val tokenPair = TokenPair("access-token", "refresh-token")

                every { kakaoAuthClient.postOAuthToken(any(), any(), any(), any(), any()) } returns kakaoToken
                every { kakaoApiClient.getAccount(any()) } returns kakaoAccount
                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns AuthProviders(
                    id = providerId, providerName = AuthProvider.KAKAO, description = "카카오 소셜 로그인"
                )
                every {
                    providerAccountRepository.findByProviderIdAndAccountId(providerId, kakaoId.toString())
                } returns existingProviderAccount
                every { userRepository.findById(existingUserId) } returns existingUser
                every { providerAccountRepository.updateAuthRegistry(existingUserId, providerId, kakaoToken) } returns 1
                every { userRepository.updateLastLoginAt(existingUserId) } returns 1
                every { tokenManager.createTokenPair(existingUserId, AuthProvider.KAKAO) } returns tokenPair
                every { tokenManager.saveRefreshToken(existingUserId, tokenPair.refreshToken) } returns mockk()

                val result = kakaoService.authenticate(code)

                result shouldBe KakaoAuthResult.Authenticated(tokenPair)
                verify(exactly = 0) { userRepository.save(any()) }
                verify(exactly = 1) {
                    providerAccountRepository.updateAuthRegistry(existingUserId, providerId, kakaoToken)
                }
                verify(exactly = 1) { tokenManager.createTokenPair(existingUserId, AuthProvider.KAKAO) }
            }
        }
    }

    Given("탈퇴한 카카오 사용자가 다시 인증할 때") {
        val code = "auth-code"
        val kakaoId = 99999L

        When("authenticate를 호출하면") {
            Then("rejoin ticket을 발급하고 RejoinRequired를 반환한다") {
                val providerId = UUIDv7.generate()
                val leavedUserId = UUIDv7.generate()
                val kakaoToken = KakaoAuthTokenResponse(
                    tokenType = "bearer",
                    accessToken = "kakao-access-token",
                    idToken = null,
                    expiresIn = 3600,
                    refreshToken = "kakao-refresh-token",
                    refreshTokenExpiresIn = 604800,
                    scope = null
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = kakaoId,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                )
                val existingProviderAccount = ProviderAccounts(
                    userId = leavedUserId,
                    providerId = providerId,
                    accountId = kakaoId.toString()
                )
                val leavedUser = Users(
                    id = leavedUserId,
                    handle = "user_leaved123",
                    status = UserStatusType.LEAVED
                )

                every { kakaoAuthClient.postOAuthToken(any(), any(), any(), any(), any()) } returns kakaoToken
                every { kakaoApiClient.getAccount(any()) } returns kakaoAccount
                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns AuthProviders(
                    id = providerId, providerName = AuthProvider.KAKAO, description = "카카오 소셜 로그인"
                )
                every {
                    providerAccountRepository.findByProviderIdAndAccountId(providerId, kakaoId.toString())
                } returns existingProviderAccount
                every { userRepository.findById(leavedUserId) } returns leavedUser
                every { jsonMapper.writeValueAsString(kakaoToken) } returns "{\"token\":\"json\"}"
                every { jsonMapper.writeValueAsString(kakaoAccount) } returns "{\"account\":\"json\"}"
                every { rejoinTicketRepository.save(any<RejoinTicketEntity>()) } answers { firstArg() }

                val result = kakaoService.authenticate(code)

                result.shouldBeInstanceOf<KakaoAuthResult.RejoinRequired>()
                verify(exactly = 1) { rejoinTicketRepository.save(any<RejoinTicketEntity>()) }
                verify(exactly = 0) { tokenManager.createTokenPair(any(), any()) }
            }
        }
    }

    Given("유효한 rejoin ticket으로 복원 요청 시") {
        val ticketId = UUIDv7.generate().toString()

        When("rejoinWithRestore를 호출하면") {
            Then("users를 ACTIVE로 복원하고 카카오 최신값으로 프로필을 갱신한 뒤 TokenPair를 반환한다") {
                val providerId = UUIDv7.generate()
                val leavedUserId = UUIDv7.generate()
                val kakaoToken = KakaoAuthTokenResponse(
                    tokenType = "bearer",
                    accessToken = "kakao-access-token",
                    idToken = null,
                    expiresIn = 3600,
                    refreshToken = "kakao-refresh-token",
                    refreshTokenExpiresIn = 604800,
                    scope = null
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = 99999L,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = KakaoApiAccount(
                        email = "new@example.com",
                        hasEmail = true,
                        emailNeedsAgreement = false,
                        isEmailValid = true,
                        isEmailVerified = true,
                        profile = KakaoApiProfile(
                            nickname = "최신닉네임",
                            isDefaultNickname = false,
                            profileImageUrl = "https://example.com/new-profile.jpg",
                            thumbnailImageUrl = "https://example.com/new-thumb.jpg",
                            isDefaultImage = false
                        ),
                        profileNicknameNeedsAgreement = false,
                        profileImageNeedsAgreement = false
                    )
                )
                val ticket = RejoinTicketEntity(
                    ticketId = ticketId,
                    providerId = providerId,
                    accountId = "99999",
                    leavedUserId = leavedUserId,
                    kakaoAuthTokenResponseJson = "{\"token\":\"json\"}",
                    kakaoAccountResponseJson = "{\"account\":\"json\"}"
                )
                val tokenPair = TokenPair("new-access", "new-refresh")

                every { rejoinTicketRepository.findById(ticketId) } returns Optional.of(ticket)
                every {
                    jsonMapper.readValue(ticket.kakaoAuthTokenResponseJson, KakaoAuthTokenResponse::class.java)
                } returns kakaoToken
                every {
                    jsonMapper.readValue(ticket.kakaoAccountResponseJson, KakaoApiAccountResponse::class.java)
                } returns kakaoAccount
                every { userRepository.restoreById(leavedUserId, "최신닉네임", any(), any()) } returns 1
                every { providerAccountRepository.updateAuthRegistry(leavedUserId, providerId, kakaoToken) } returns 1
                every {
                    providerAccountRepository.updateAccountRegistry(
                        leavedUserId,
                        providerId,
                        kakaoAccount
                    )
                } returns 1
                every { rejoinTicketRepository.deleteById(ticketId) } just runs
                every { userRepository.updateLastLoginAt(leavedUserId) } returns 1
                every { tokenManager.createTokenPair(leavedUserId, AuthProvider.KAKAO) } returns tokenPair
                every { tokenManager.saveRefreshToken(leavedUserId, tokenPair.refreshToken) } returns mockk()

                val result = kakaoService.rejoinWithRestore(ticketId)

                result shouldBe tokenPair
                verify(exactly = 1) { userRepository.restoreById(leavedUserId, "최신닉네임", any(), any()) }
                verify(exactly = 1) {
                    providerAccountRepository.updateAuthRegistry(
                        leavedUserId,
                        providerId,
                        kakaoToken
                    )
                }
                verify(exactly = 1) {
                    providerAccountRepository.updateAccountRegistry(
                        leavedUserId,
                        providerId,
                        kakaoAccount
                    )
                }
                verify(exactly = 1) { rejoinTicketRepository.deleteById(ticketId) }
            }
        }
    }

    Given("유효한 rejoin ticket으로 새로 시작 요청 시") {
        val ticketId = UUIDv7.generate().toString()

        When("rejoinWithFresh를 호출하면") {
            Then("기존 row는 익명화되고 provider_accounts 삭제 후 새 row가 생성된다") {
                val providerId = UUIDv7.generate()
                val leavedUserId = UUIDv7.generate()
                val newUserId = UUIDv7.generate()
                val kakaoToken = KakaoAuthTokenResponse(
                    tokenType = "bearer",
                    accessToken = "kakao-access-token",
                    idToken = null,
                    expiresIn = 3600,
                    refreshToken = "kakao-refresh-token",
                    refreshTokenExpiresIn = 604800,
                    scope = null
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = 99999L,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                )
                val ticket = RejoinTicketEntity(
                    ticketId = ticketId,
                    providerId = providerId,
                    accountId = "99999",
                    leavedUserId = leavedUserId,
                    kakaoAuthTokenResponseJson = "{\"token\":\"json\"}",
                    kakaoAccountResponseJson = "{\"account\":\"json\"}"
                )
                val newUser = Users(id = newUserId, handle = "user_new123456")
                val tokenPair = TokenPair("fresh-access", "fresh-refresh")

                every { rejoinTicketRepository.findById(ticketId) } returns Optional.of(ticket)
                every {
                    jsonMapper.readValue(ticket.kakaoAuthTokenResponseJson, KakaoAuthTokenResponse::class.java)
                } returns kakaoToken
                every {
                    jsonMapper.readValue(ticket.kakaoAccountResponseJson, KakaoApiAccountResponse::class.java)
                } returns kakaoAccount
                every { userRepository.anonymizeById(leavedUserId) } returns 1
                every { providerAccountRepository.deleteByUserId(leavedUserId) } returns 1
                every { userRepository.existsByHandle(any()) } returns false
                every { userRepository.save(any()) } returns newUser
                every { providerAccountRepository.save(any(), any(), any(), any(), any()) } just runs
                every { rejoinTicketRepository.deleteById(ticketId) } just runs
                every { userRepository.updateLastLoginAt(newUserId) } returns 1
                every { tokenManager.createTokenPair(newUserId, AuthProvider.KAKAO) } returns tokenPair
                every { tokenManager.saveRefreshToken(newUserId, tokenPair.refreshToken) } returns mockk()

                val result = kakaoService.rejoinWithFresh(ticketId)

                result shouldBe tokenPair
                verify(exactly = 1) { userRepository.anonymizeById(leavedUserId) }
                verify(exactly = 1) { providerAccountRepository.deleteByUserId(leavedUserId) }
                verify(exactly = 1) { userRepository.save(any()) }
                verify(exactly = 1) { rejoinTicketRepository.deleteById(ticketId) }
            }
        }
    }

    Given("만료되었거나 존재하지 않는 rejoin ticket이 주어졌을 때") {
        val ticketId = UUIDv7.generate().toString()

        When("rejoinWithRestore를 호출하면") {
            Then("RejoinTicketNotFoundException이 발생한다") {
                every { rejoinTicketRepository.findById(ticketId) } returns Optional.empty()

                val exception = shouldThrow<RejoinTicketNotFoundException> {
                    kakaoService.rejoinWithRestore(ticketId)
                }
                exception.shouldBeInstanceOf<NotFoundException>()
                exception.httpStatus shouldBe HttpStatus.NOT_FOUND
                exception.errorCode shouldBe "REJOIN_TICKET_NOT_FOUND"
                exception.message shouldBe "재가입 티켓을 찾을 수 없습니다"
            }
        }

        When("rejoinWithFresh를 호출하면") {
            Then("RejoinTicketNotFoundException이 발생한다") {
                every { rejoinTicketRepository.findById(ticketId) } returns Optional.empty()

                val exception = shouldThrow<RejoinTicketNotFoundException> {
                    kakaoService.rejoinWithFresh(ticketId)
                }
                exception.httpStatus shouldBe HttpStatus.NOT_FOUND
                exception.errorCode shouldBe "REJOIN_TICKET_NOT_FOUND"
            }
        }
    }

    Given("카카오 provider가 DB에 없을 때") {
        When("getKakaoProviderId를 호출하면") {
            Then("ProviderNotFoundException이 발생한다") {
                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns null

                val exception = shouldThrow<ProviderNotFoundException> {
                    kakaoService.getKakaoProviderId()
                }
                exception.shouldBeInstanceOf<NotFoundException>()
                exception.httpStatus shouldBe HttpStatus.NOT_FOUND
                exception.errorCode shouldBe "PROVIDER_NOT_FOUND"
                exception.message shouldBe "OAuth 제공자를 찾을 수 없습니다: KAKAO"
            }
        }
    }

    Given("카카오 OAuth 토큰 갱신 시") {
        val userId = UUIDv7.generate()
        val providerId = UUIDv7.generate()
        val currentAuth = KakaoAuthTokenResponse(
            tokenType = "bearer",
            accessToken = "old-kakao-access",
            idToken = "old-id-token",
            expiresIn = 3600,
            refreshToken = "old-kakao-refresh",
            refreshTokenExpiresIn = 604800,
            scope = "profile"
        )
        val providerAccount = ProviderAccounts(
            userId = userId,
            providerId = providerId,
            accountId = "12345",
            authRegistry = JSONB.jsonb("{}")
        )

        When("갱신 응답에 refresh_token이 있으면") {
            Then("auth_registry와 account_registry를 모두 갱신한다") {
                val refreshedAuth = KakaoAuthRefreshTokenResponse(
                    tokenType = "bearer",
                    accessToken = "new-kakao-access",
                    expiresIn = 3600,
                    refreshToken = "new-kakao-refresh",
                    refreshTokenExpiresIn = 5184000
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = 12345L,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                )

                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns AuthProviders(
                    id = providerId, providerName = AuthProvider.KAKAO, description = "카카오 소셜 로그인"
                )
                every {
                    providerAccountRepository.findByUserIdAndProviderId(userId, providerId)
                } returns providerAccount
                every { jsonMapper.readValue("{}", KakaoAuthTokenResponse::class.java) } returns currentAuth
                every { kakaoAuthClient.postRefreshToken(any(), any(), any(), any()) } returns refreshedAuth
                every { providerAccountRepository.updateAuthRegistry(userId, providerId, any()) } returns 1
                every { kakaoApiClient.getAccount(any()) } returns kakaoAccount
                every { providerAccountRepository.updateAccountRegistry(userId, providerId, kakaoAccount) } returns 1

                kakaoService.refreshOAuthTokens(userId)

                verify(exactly = 1) {
                    providerAccountRepository.updateAuthRegistry(userId, providerId, match<KakaoAuthTokenResponse> {
                        it.accessToken == "new-kakao-access" && it.refreshToken == "new-kakao-refresh"
                    })
                }
                verify(exactly = 1) {
                    providerAccountRepository.updateAccountRegistry(userId, providerId, kakaoAccount)
                }
            }
        }

        When("갱신 응답에 refresh_token이 null이면") {
            Then("기존 refresh_token을 유지한다") {
                val refreshedAuth = KakaoAuthRefreshTokenResponse(
                    tokenType = "bearer",
                    accessToken = "new-kakao-access",
                    expiresIn = 3600,
                    refreshToken = null,
                    refreshTokenExpiresIn = null
                )
                val kakaoAccount = KakaoApiAccountResponse(
                    id = 12345L,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                )

                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns AuthProviders(
                    id = providerId, providerName = AuthProvider.KAKAO, description = "카카오 소셜 로그인"
                )
                every {
                    providerAccountRepository.findByUserIdAndProviderId(userId, providerId)
                } returns providerAccount
                every { jsonMapper.readValue("{}", KakaoAuthTokenResponse::class.java) } returns currentAuth
                every { kakaoAuthClient.postRefreshToken(any(), any(), any(), any()) } returns refreshedAuth
                every { providerAccountRepository.updateAuthRegistry(userId, providerId, any()) } returns 1
                every { kakaoApiClient.getAccount(any()) } returns kakaoAccount
                every { providerAccountRepository.updateAccountRegistry(userId, providerId, kakaoAccount) } returns 1

                kakaoService.refreshOAuthTokens(userId)

                verify(exactly = 1) {
                    providerAccountRepository.updateAuthRegistry(userId, providerId, match<KakaoAuthTokenResponse> {
                        it.accessToken == "new-kakao-access" && it.refreshToken == "old-kakao-refresh"
                                && it.refreshTokenExpiresIn == 604800L
                    })
                }
            }
        }

        When("provider account가 없으면") {
            Then("ProviderNotFoundException이 발생한다") {
                every { authProviderRepository.findByProviderName(AuthProvider.KAKAO) } returns AuthProviders(
                    id = providerId, providerName = AuthProvider.KAKAO, description = "카카오 소셜 로그인"
                )
                every { providerAccountRepository.findByUserIdAndProviderId(userId, providerId) } returns null

                val exception = shouldThrow<ProviderNotFoundException> {
                    kakaoService.refreshOAuthTokens(userId)
                }
                exception.shouldBeInstanceOf<NotFoundException>()
                exception.httpStatus shouldBe HttpStatus.NOT_FOUND
                exception.errorCode shouldBe "PROVIDER_NOT_FOUND"
                exception.message shouldBe "OAuth 제공자를 찾을 수 없습니다: KAKAO"
            }
        }
    }

    Given("카카오 토큰 교환 API 호출이 실패할 때") {
        val code = "invalid-code"

        When("authenticate를 호출하면") {
            Then("ExternalBadRequestException이 발생한다") {
                every {
                    kakaoAuthClient.postOAuthToken(any(), any(), any(), any(), any())
                } throws ExternalBadRequestException()

                val exception = shouldThrow<ExternalBadRequestException> {
                    kakaoService.authenticate(code)
                }
                exception.httpStatus shouldBe HttpStatus.BAD_REQUEST
                exception.errorCode shouldBe "EXTERNAL_BAD_REQUEST"
                exception.message shouldBe "외부 API 요청이 잘못되었습니다"
            }
        }
    }

    Given("카카오 사용자 정보 조회 API 호출이 실패할 때") {
        val code = "auth-code"
        val kakaoToken = KakaoAuthTokenResponse(
            tokenType = "bearer",
            accessToken = "kakao-access-token",
            idToken = null,
            expiresIn = 3600,
            refreshToken = "kakao-refresh-token",
            refreshTokenExpiresIn = 604800,
            scope = null
        )

        When("authenticate를 호출하면") {
            Then("ExternalBadRequestException이 발생한다") {
                every {
                    kakaoAuthClient.postOAuthToken(any(), any(), any(), any(), any())
                } returns kakaoToken
                every { kakaoApiClient.getAccount(any()) } throws ExternalBadRequestException()

                val exception = shouldThrow<ExternalBadRequestException> {
                    kakaoService.authenticate(code)
                }
                exception.httpStatus shouldBe HttpStatus.BAD_REQUEST
                exception.errorCode shouldBe "EXTERNAL_BAD_REQUEST"
                exception.message shouldBe "외부 API 요청이 잘못되었습니다"
            }
        }
    }
})
