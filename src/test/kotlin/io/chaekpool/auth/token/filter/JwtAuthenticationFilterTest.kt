package io.chaekpool.auth.token.filter

import io.chaekpool.auth.token.exception.TokenBlacklistedException
import io.chaekpool.auth.token.provider.JwtProvider
import io.chaekpool.auth.token.service.BlacklistManager
import io.chaekpool.common.exception.ErrorCodeAccessDeniedException
import io.chaekpool.common.exception.ErrorCodeBadCredentialsException
import io.chaekpool.common.exception.internal.ForbiddenException
import io.chaekpool.common.util.UUIDv7
import io.kotest.assertions.throwables.shouldThrow
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

class JwtAuthenticationFilterTest : BehaviorSpec({

    lateinit var blacklistManager: BlacklistManager
    lateinit var jwtProvider: JwtProvider
    lateinit var filter: JwtAuthenticationFilter
    lateinit var filterChain: FilterChain

    beforeTest {
        blacklistManager = mockk()
        jwtProvider = mockk()
        filter = JwtAuthenticationFilter(blacklistManager, jwtProvider)
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

    Given("유효한 Bearer 토큰이 주어졌을 때") {
        val token = "valid-jwt-token"
        val userId = UUIDv7.generate()
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("authentication의 principal이 userId로 설정된다") {
                every { blacklistManager.assertToken(token) } just runs
                every { jwtProvider.getUserId(token) } returns userId

                filter.doFilter(request, response, filterChain)

                val authentication = SecurityContextHolder.getContext().authentication
                authentication!!.principal shouldBe userId
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }
    }

    Given("블랙리스트에 등록된 토큰이 주어졌을 때") {
        val token = "blacklisted-token"
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("ErrorCodeBadCredentialsException이 발생한다") {
                every { blacklistManager.assertToken(token) } throws TokenBlacklistedException()

                val exception = shouldThrow<ErrorCodeBadCredentialsException> {
                    filter.doFilter(request, response, filterChain)
                }
                exception.message shouldBe "JWT token blacklisted"
                request.getAttribute("_errorCode") shouldBe "TOKEN_BLACKLISTED"
                request.getAttribute("_httpStatus") shouldBe HttpStatus.UNAUTHORIZED
            }
        }
    }

    Given("ForbiddenException이 발생하는 토큰이 주어졌을 때") {
        val token = "forbidden-token"
        val request = MockHttpServletRequest()
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        val response = MockHttpServletResponse()

        When("필터를 실행하면") {
            Then("ErrorCodeAccessDeniedException이 발생한다") {
                every { blacklistManager.assertToken(token) } just runs
                every { jwtProvider.getUserId(token) } throws ForbiddenException()

                val exception = shouldThrow<ErrorCodeAccessDeniedException> {
                    filter.doFilter(request, response, filterChain)
                }
                exception.message shouldBe "권한이 없습니다"
                request.getAttribute("_errorCode") shouldBe "FORBIDDEN"
                request.getAttribute("_httpStatus") shouldBe HttpStatus.FORBIDDEN
            }
        }
    }
})
