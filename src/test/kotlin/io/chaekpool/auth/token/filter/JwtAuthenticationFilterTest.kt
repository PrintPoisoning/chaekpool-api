package io.chaekpool.auth.token.filter

import io.chaekpool.auth.token.exception.InvalidTokenException
import io.chaekpool.auth.token.exception.TokenBlacklistedException
import io.chaekpool.auth.token.exception.TokenExpiredException
import io.chaekpool.auth.token.provider.JwtProvider
import io.chaekpool.auth.token.service.BlacklistManager
import io.chaekpool.common.exception.internal.ForbiddenException
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.generated.jooq.enums.UserStatusType
import io.chaekpool.generated.jooq.enums.UserVisibilityType
import io.chaekpool.generated.jooq.tables.pojos.Users
import io.chaekpool.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.servlet.HandlerExceptionResolver

class JwtAuthenticationFilterTest : BehaviorSpec({

    lateinit var blacklistManager: BlacklistManager
    lateinit var jwtProvider: JwtProvider
    lateinit var userRepository: UserRepository
    lateinit var authenticationEntryPoint: AuthenticationEntryPoint
    lateinit var accessDeniedHandler: AccessDeniedHandler
    lateinit var handlerExceptionResolver: HandlerExceptionResolver
    lateinit var filter: JwtAuthenticationFilter
    lateinit var filterChain: FilterChain

    beforeTest {
        blacklistManager = mockk()
        jwtProvider = mockk()
        userRepository = mockk()
        authenticationEntryPoint = mockk()
        accessDeniedHandler = mockk()
        handlerExceptionResolver = mockk()
        filter = JwtAuthenticationFilter(
            blacklistManager,
            jwtProvider,
            userRepository,
            authenticationEntryPoint,
            accessDeniedHandler,
            handlerExceptionResolver
        )
        filterChain = mockk()
        every { filterChain.doFilter(any(), any()) } just runs
        SecurityContextHolder.clearContext()
    }

    afterTest {
        SecurityContextHolder.clearContext()
    }

    Given("Authorization 헤더가 없는 요청이 주어졌을 때") {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authentication이 설정되지 않고 filterChain이 호출된다") {
                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }
    }

    Given("Bearer 접두사가 없는 Authorization 헤더가 주어졌을 때") {
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic some-token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authentication이 설정되지 않는다") {
                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }
    }

    Given("유효한 Bearer 토큰이고 활성 사용자일 때") {
        val token = "valid-jwt-token"
        val userId = UUIDv7.generate()
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authentication의 principal이 userId로 설정된다") {
                val activeUser = Users(
                    id = userId,
                    handle = "user_abc12345",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.ACTIVE
                )
                every { blacklistManager.assertToken(token) } just runs
                every { jwtProvider.getUserId(token) } returns userId
                every { userRepository.findById(userId) } returns activeUser

                filter.doFilter(request, response, filterChain)

                val authentication = SecurityContextHolder.getContext().authentication
                authentication!!.principal shouldBe userId
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }
    }

    Given("유효한 Bearer 토큰이지만 탈퇴한 사용자일 때") {
        val token = "valid-jwt-token-leaved"
        val userId = UUIDv7.generate()
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("handlerExceptionResolver가 UserLeavedException을 처리하고 filterChain은 호출되지 않는다") {
                val leavedUser = Users(
                    id = userId,
                    handle = "user_abc12345",
                    visibility = UserVisibilityType.PUBLIC,
                    status = UserStatusType.LEAVED
                )
                every { blacklistManager.assertToken(token) } just runs
                every { jwtProvider.getUserId(token) } returns userId
                every { userRepository.findById(userId) } returns leavedUser
                every { handlerExceptionResolver.resolveException(request, response, null, any()) } returns null

                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                verify(exactly = 1) { handlerExceptionResolver.resolveException(request, response, null, any()) }
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }
        }
    }

    Given("유효한 Bearer 토큰이지만 DB에 사용자가 없을 때") {
        val token = "valid-jwt-token-missing"
        val userId = UUIDv7.generate()
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("handlerExceptionResolver가 UserNotFoundException을 처리한다") {
                every { blacklistManager.assertToken(token) } just runs
                every { jwtProvider.getUserId(token) } returns userId
                every { userRepository.findById(userId) } returns null
                every { handlerExceptionResolver.resolveException(request, response, null, any()) } returns null

                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                verify(exactly = 1) { handlerExceptionResolver.resolveException(request, response, null, any()) }
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }
        }
    }

    Given("블랙리스트에 등록된 토큰이 주어졌을 때") {
        val token = "blacklisted-token"
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authenticationEntryPoint가 호출되고 filterChain은 호출되지 않는다") {
                every { blacklistManager.assertToken(token) } throws TokenBlacklistedException()
                every { authenticationEntryPoint.commence(request, response, any()) } just runs

                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                request.getAttribute("_errorCode") shouldBe "TOKEN_BLACKLISTED"
                request.getAttribute("_httpStatus") shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 1) { authenticationEntryPoint.commence(request, response, any()) }
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }
        }
    }

    Given("만료된 토큰이 주어졌을 때") {
        val token = "expired-token"
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authenticationEntryPoint가 호출되고 filterChain은 호출되지 않는다") {
                every { blacklistManager.assertToken(token) } throws TokenExpiredException()
                every { authenticationEntryPoint.commence(request, response, any()) } just runs

                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                request.getAttribute("_errorCode") shouldBe "TOKEN_EXPIRED"
                request.getAttribute("_httpStatus") shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 1) { authenticationEntryPoint.commence(request, response, any()) }
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }
        }
    }

    Given("잘못된 서명의 토큰이 주어졌을 때") {
        val token = "invalid-signature-token"
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authenticationEntryPoint가 호출되고 filterChain은 호출되지 않는다") {
                every { blacklistManager.assertToken(token) } throws InvalidTokenException()
                every { authenticationEntryPoint.commence(request, response, any()) } just runs

                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                request.getAttribute("_errorCode") shouldBe "INVALID_TOKEN"
                request.getAttribute("_httpStatus") shouldBe HttpStatus.UNAUTHORIZED
                verify(exactly = 1) { authenticationEntryPoint.commence(request, response, any()) }
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }
        }
    }

    Given("ForbiddenException이 발생하는 토큰이 주어졌을 때") {
        val token = "forbidden-token"
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("accessDeniedHandler가 호출되고 filterChain은 호출되지 않는다") {
                every { blacklistManager.assertToken(token) } just runs
                every { jwtProvider.getUserId(token) } throws ForbiddenException()
                every { accessDeniedHandler.handle(request, response, any()) } just runs

                filter.doFilter(request, response, filterChain)

                SecurityContextHolder.getContext().authentication.shouldBeNull()
                request.getAttribute("_errorCode") shouldBe "FORBIDDEN"
                request.getAttribute("_httpStatus") shouldBe HttpStatus.FORBIDDEN
                verify(exactly = 1) { accessDeniedHandler.handle(request, response, any()) }
                verify(exactly = 0) { filterChain.doFilter(any(), any()) }
            }
        }
    }
})
