package io.chaekpool.auth.dev.controller

import io.chaekpool.auth.constant.AuthProvider
import io.chaekpool.auth.token.dto.TokenPair
import io.chaekpool.auth.token.entity.RefreshTokenEntity
import io.chaekpool.auth.token.provider.CookieProvider
import io.chaekpool.auth.token.service.TokenManager
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.mock.web.MockHttpServletResponse

class DevAuthControllerTest : BehaviorSpec({

    lateinit var userRepository: UserRepository
    lateinit var tokenManager: TokenManager
    lateinit var cookieProvider: CookieProvider
    lateinit var devAuthController: DevAuthController

    beforeTest {
        userRepository = mockk()
        tokenManager = mockk()
        cookieProvider = mockk()
        devAuthController = DevAuthController(userRepository, tokenManager, cookieProvider)
    }

    Given("username 파라미터가 주어졌을 때") {
        val username = "test-swagger"
        val userId = UUIDv7.generate()
        val savedUser = Users(id = userId, username = username)
        val tokenPair = TokenPair(accessToken = "test-access-token", refreshToken = "test-refresh-token")
        val refreshTokenEntity = RefreshTokenEntity(
            jti = "test-jti",
            userId = userId,
            ip = null,
            userAgent = null,
            device = null,
            platformType = null
        )
        val cookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken).build()

        When("devLogin을 호출하면") {
            Then("새 사용자를 생성하고 토큰을 반환한다") {
                every { userRepository.save(any()) } returns savedUser
                every { tokenManager.createTokenPair(userId, AuthProvider.CHAEKPOOL) } returns tokenPair
                every { tokenManager.saveRefreshToken(userId, tokenPair.refreshToken) } returns refreshTokenEntity
                every { userRepository.updateLastLoginAt(userId) } returns 1
                every { cookieProvider.refreshTokenCookie(tokenPair.refreshToken) } returns cookie

                val response = MockHttpServletResponse()
                val result = devAuthController.devLogin(username, response)

                result.statusCode shouldBe HttpStatus.OK
                result.body shouldNotBe null
                result.body!!.accessToken shouldBe tokenPair.accessToken

                response.getHeader(SET_COOKIE) shouldNotBe null

                verify(exactly = 1) { userRepository.save(any()) }
                verify(exactly = 1) { tokenManager.createTokenPair(userId, AuthProvider.CHAEKPOOL) }
                verify(exactly = 1) { tokenManager.saveRefreshToken(userId, tokenPair.refreshToken) }
                verify(exactly = 1) { userRepository.updateLastLoginAt(userId) }
                verify(exactly = 1) { cookieProvider.refreshTokenCookie(tokenPair.refreshToken) }
            }
        }
    }

    Given("username이 기본값(dev-user)일 때") {
        val defaultUsername = "dev-user"
        val userId = UUIDv7.generate()
        val savedUser = Users(id = userId, username = defaultUsername)
        val tokenPair = TokenPair(accessToken = "default-access-token", refreshToken = "default-refresh-token")
        val refreshTokenEntity = RefreshTokenEntity(
            jti = "default-jti",
            userId = userId,
            ip = null,
            userAgent = null,
            device = null,
            platformType = null
        )
        val cookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken).build()

        When("devLogin을 호출하면") {
            Then("기본 username으로 사용자를 생성하고 토큰을 반환한다") {
                every { userRepository.save(any()) } returns savedUser
                every { tokenManager.createTokenPair(userId, AuthProvider.CHAEKPOOL) } returns tokenPair
                every { tokenManager.saveRefreshToken(userId, tokenPair.refreshToken) } returns refreshTokenEntity
                every { userRepository.updateLastLoginAt(userId) } returns 1
                every { cookieProvider.refreshTokenCookie(tokenPair.refreshToken) } returns cookie

                val response = MockHttpServletResponse()
                val result = devAuthController.devLogin(defaultUsername, response)

                result.statusCode shouldBe HttpStatus.OK
                result.body shouldNotBe null
                result.body!!.accessToken shouldBe tokenPair.accessToken

                verify(exactly = 1) { userRepository.save(match { it.username == defaultUsername }) }
            }
        }
    }
})
