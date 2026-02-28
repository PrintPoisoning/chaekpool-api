package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.provider.JwtProvider
import io.chaekpool.common.util.UUIDv7
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

class TokenServiceTest : BehaviorSpec({

    lateinit var blacklistManager: BlacklistManager
    lateinit var tokenManager: TokenManager
    lateinit var jwtProvider: JwtProvider
    lateinit var tokenService: TokenService

    beforeTest {
        blacklistManager = mockk()
        tokenManager = mockk()
        jwtProvider = mockk()
        tokenService = TokenService(blacklistManager, tokenManager, jwtProvider)
    }

    Given("accessToken과 refreshToken이 주어졌을 때") {
        When("refresh를 호출하면") {
            Then("access token만 새로 발급하고 refresh token은 유지한다") {
                val userId = UUIDv7.generate()
                val accessToken = "old-access-token"
                val refreshToken = "old-refresh-token"
                val newAccessToken = "new-access-token"

                every { tokenManager.assertRefreshToken(userId, refreshToken) } just runs
                every { blacklistManager.blacklistToken(accessToken) } just runs
                every { tokenManager.createAccessToken(userId) } returns newAccessToken

                val result = tokenService.refresh(userId, accessToken, refreshToken)

                result shouldBe TokenPair(newAccessToken, refreshToken)
                verify(exactly = 1) { tokenManager.createAccessToken(userId) }
            }
        }
    }

    Given("refresh 호출 시 accessToken이 있으면") {
        When("블랙리스트에 추가한다") {
            Then("blacklistToken이 accessToken에 대해 호출된다") {
                val userId = UUIDv7.generate()
                val accessToken = "old-access-token"
                val refreshToken = "old-refresh-token"

                every { tokenManager.assertRefreshToken(userId, refreshToken) } just runs
                every { blacklistManager.blacklistToken(accessToken) } just runs
                every { tokenManager.createAccessToken(userId) } returns "new-access"

                tokenService.refresh(userId, accessToken, refreshToken)

                verify(exactly = 1) { blacklistManager.blacklistToken(accessToken) }
            }
        }
    }

    Given("refresh 호출 시 accessToken이 null이면") {
        When("블랙리스트 추가를 건너뛴다") {
            Then("blacklistToken이 호출되지 않는다") {
                val userId = UUIDv7.generate()
                val refreshToken = "old-refresh-token"

                every { tokenManager.assertRefreshToken(userId, refreshToken) } just runs
                every { tokenManager.createAccessToken(userId) } returns "new-access"

                tokenService.refresh(userId, null, refreshToken)

                verify(exactly = 0) { blacklistManager.blacklistToken(any()) }
            }
        }
    }

    Given("refresh 호출 시") {
        When("refresh token은 블랙리스트에 추가하지 않는다") {
            Then("refreshToken에 대한 blacklistToken 호출이 없다") {
                val userId = UUIDv7.generate()
                val accessToken = "old-access-token"
                val refreshToken = "old-refresh-token"

                every { tokenManager.assertRefreshToken(userId, refreshToken) } just runs
                every { blacklistManager.blacklistToken(accessToken) } just runs
                every { tokenManager.createAccessToken(userId) } returns "new-access"

                tokenService.refresh(userId, accessToken, refreshToken)

                verify(exactly = 0) { blacklistManager.blacklistToken(refreshToken) }
            }
        }
    }

    Given("accessToken과 refreshToken이 모두 주어졌을 때") {
        When("deactivate를 호출하면") {
            Then("두 토큰 모두 블랙리스트에 추가되고 refresh 토큰이 삭제된다") {
                val userId = UUIDv7.generate()
                val accessToken = "access-token"
                val refreshToken = "refresh-token"
                val jti = "test-jti"

                every { blacklistManager.blacklistToken(accessToken) } just runs
                every { blacklistManager.blacklistToken(refreshToken) } just runs
                every { jwtProvider.getJti(refreshToken) } returns jti
                every { tokenManager.deleteByJti(jti) } just runs

                tokenService.deactivate(userId, accessToken, refreshToken)

                verify(exactly = 1) { blacklistManager.blacklistToken(accessToken) }
                verify(exactly = 1) { blacklistManager.blacklistToken(refreshToken) }
                verify(exactly = 1) { tokenManager.deleteByJti(jti) }
            }
        }
    }

    Given("accessToken이 null일 때") {
        When("deactivate를 호출하면") {
            Then("refreshToken만 블랙리스트에 추가되고 삭제된다") {
                val userId = UUIDv7.generate()
                val refreshToken = "refresh-token"
                val jti = "test-jti-2"

                every { blacklistManager.blacklistToken(refreshToken) } just runs
                every { jwtProvider.getJti(refreshToken) } returns jti
                every { tokenManager.deleteByJti(jti) } just runs

                tokenService.deactivate(userId, null, refreshToken)

                verify(exactly = 1) { blacklistManager.blacklistToken(refreshToken) }
                verify(exactly = 1) { tokenManager.deleteByJti(jti) }
            }
        }
    }
})
