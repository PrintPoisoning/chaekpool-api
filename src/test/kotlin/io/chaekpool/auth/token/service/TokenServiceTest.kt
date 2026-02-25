package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.repository.RefreshTokenRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.UUID

class TokenServiceTest : BehaviorSpec({

    lateinit var blacklistManager: BlacklistManager
    lateinit var tokenManager: TokenManager
    lateinit var jwtProvider: JwtProvider
    lateinit var refreshTokenRepository: RefreshTokenRepository
    lateinit var tokenService: TokenService

    beforeTest {
        blacklistManager = mockk()
        tokenManager = mockk()
        jwtProvider = mockk()
        refreshTokenRepository = mockk()
        tokenService = TokenService(blacklistManager, tokenManager, jwtProvider, refreshTokenRepository)
    }

    Given("accessToken과 refreshToken이 모두 주어졌을 때") {
        When("deactivate를 호출하면") {
            Then("두 토큰 모두 블랙리스트에 추가되고 refresh 토큰이 삭제된다") {
                val userId = UUID.randomUUID()
                val accessToken = "access-token"
                val refreshToken = "refresh-token"
                val jti = "test-jti"

                every { blacklistManager.blacklistToken(userId, accessToken) } just runs
                every { blacklistManager.blacklistToken(userId, refreshToken) } just runs
                every { jwtProvider.getJti(refreshToken) } returns jti
                every { tokenManager.deleteByJti(jti) } just runs

                tokenService.deactivate(userId, accessToken, refreshToken)

                verify(exactly = 1) { blacklistManager.blacklistToken(userId, accessToken) }
                verify(exactly = 1) { blacklistManager.blacklistToken(userId, refreshToken) }
                verify(exactly = 1) { tokenManager.deleteByJti(jti) }
            }
        }
    }

    Given("accessToken이 null일 때") {
        When("deactivate를 호출하면") {
            Then("refreshToken만 블랙리스트에 추가되고 삭제된다") {
                val userId = UUID.randomUUID()
                val refreshToken = "refresh-token"
                val jti = "test-jti-2"

                every { blacklistManager.blacklistToken(userId, refreshToken) } just runs
                every { jwtProvider.getJti(refreshToken) } returns jti
                every { tokenManager.deleteByJti(jti) } just runs

                tokenService.deactivate(userId, null, refreshToken)

                verify(exactly = 1) { blacklistManager.blacklistToken(userId, refreshToken) }
                verify(exactly = 1) { tokenManager.deleteByJti(jti) }
            }
        }
    }
})
