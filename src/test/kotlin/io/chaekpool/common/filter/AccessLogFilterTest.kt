package io.chaekpool.common.filter

import io.chaekpool.common.util.UserMetadataExtractor
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.Locale

class AccessLogFilterTest : BehaviorSpec({
    val extractor = UserMetadataExtractor()
    val filter = AccessLogFilter(extractor)

    Given("AccessLogFilter가 요청을 처리할 준비가 되어 있을 때") {
        When("POST /api/v1/auth/login 요청이 들어오고 응답 상태가 200이면") {
            Then("다운스트림 FilterChain.doFilter가 정확히 1회 호출된다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = mockk<HttpServletResponse>(relaxed = true)
                val filterChain = mockk<FilterChain>(relaxed = true)

                every { request.requestURI } returns "/api/v1/auth/login"
                every { request.queryString } returns null
                every { request.method } returns "POST"
                every { request.getHeader("User-Agent") } returns "Mozilla/5.0"
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA
                every { response.status } returns 200

                filter.doFilter(request, response, filterChain)

                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        When("GET /api/v1/users?page=1&size=10 요청이 들어오면") {
            Then("쿼리 문자열을 포함하여 로그에 기록하고 필터 체인이 실행된다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = mockk<HttpServletResponse>(relaxed = true)
                val filterChain = mockk<FilterChain>(relaxed = true)

                every { request.requestURI } returns "/api/v1/users"
                every { request.queryString } returns "page=1&size=10"
                every { request.method } returns "GET"
                every { request.getHeader("User-Agent") } returns "Mozilla/5.0"
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA
                every { response.status } returns 200

                filter.doFilter(request, response, filterChain)

                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        When("/actuator/health 경로 요청이 들어오면") {
            Then("필터가 스킵되고 FilterChain.doFilter가 즉시 호출된다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = mockk<HttpServletResponse>(relaxed = true)
                val filterChain = mockk<FilterChain>(relaxed = true)

                every { request.requestURI } returns "/actuator/health"

                filter.doFilter(request, response, filterChain)

                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        When("/static/file.css 정적 파일 요청이 들어오면") {
            Then("필터가 스킵되고 FilterChain.doFilter가 즉시 호출된다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = mockk<HttpServletResponse>(relaxed = true)
                val filterChain = mockk<FilterChain>(relaxed = true)

                every { request.requestURI } returns "/static/file.css"

                filter.doFilter(request, response, filterChain)

                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }
    }
})
