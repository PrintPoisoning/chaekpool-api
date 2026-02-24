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
    lateinit var refreshTokenRepository: RefreshTokenRepository
    lateinit var tokenService: TokenService

    beforeTest {
        blacklistManager = mockk()
        tokenManager = mockk()
        refreshTokenRepository = mockk()
        tokenService = TokenService(blacklistManager, tokenManager, refreshTokenRepository)
    }

    Given("accessToken과 refreshToken이 모두 주어졌을 때") {
        When("deactivate를 호출하면") {
            Then("두 토큰 모두 블랙리스트에 추가되고 refresh 토큰이 삭제된다") {
                val userId = UUID.randomUUID()
                val accessToken = "access-token"
                val refreshToken = "refresh-token"

                every { blacklistManager.blacklistToken(userId, accessToken) } just runs
                every { blacklistManager.blacklistToken(userId, refreshToken) } just runs
                every { tokenManager.deleteByUserIdAndToken(userId, refreshToken) } just runs

                tokenService.deactivate(userId, accessToken, refreshToken)

                verify(exactly = 1) { blacklistManager.blacklistToken(userId, accessToken) }
                verify(exactly = 1) { blacklistManager.blacklistToken(userId, refreshToken) }
                verify(exactly = 1) { tokenManager.deleteByUserIdAndToken(userId, refreshToken) }
            }
        }
    }

    Given("accessToken이 null일 때") {
        When("deactivate를 호출하면") {
            Then("refreshToken만 블랙리스트에 추가되고 삭제된다") {
                val userId = UUID.randomUUID()
                val refreshToken = "refresh-token"

                every { blacklistManager.blacklistToken(userId, refreshToken) } just runs
                every { tokenManager.deleteByUserIdAndToken(userId, refreshToken) } just runs

                tokenService.deactivate(userId, null, refreshToken)

                // accessToken이 null이므로 blacklistToken은 refreshToken에 대해 1번만 호출됨
                verify(exactly = 1) { blacklistManager.blacklistToken(userId, refreshToken) }
                verify(exactly = 1) { tokenManager.deleteByUserIdAndToken(userId, refreshToken) }
            }
        }
    }
})
