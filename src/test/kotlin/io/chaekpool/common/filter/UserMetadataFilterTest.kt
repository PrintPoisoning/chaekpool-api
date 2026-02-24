package io.chaekpool.common.filter

import io.chaekpool.common.util.UserMetadataExtractor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.Locale

class UserMetadataFilterTest : BehaviorSpec({
    val extractor = UserMetadataExtractor()
    val context = UserMetadataContext()
    val filter = UserMetadataFilter(extractor, context)

    afterEach {
        context.clear()
    }

    Given("HTTP 요청이 FilterChain을 통해 처리될 준비가 되어 있을 때") {
        When("User-Agent('Mozilla/5.0'), IP('127.0.0.1')을 가진 요청이 들어오면") {
            Then("필터 체인이 정확히 1회 실행된다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = mockk<HttpServletResponse>(relaxed = true)
                val filterChain = mockk<FilterChain>(relaxed = true)

                every { request.getHeader("User-Agent") } returns "Mozilla/5.0"
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA

                filter.doFilter(request, response, filterChain)

                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }

            Then("필터 체인 실행 후 UserMetadataContext가 정리된다") {
                val retrieved by context
                retrieved shouldBe null
            }
        }
    }

    Given("필터 체인에서 예외가 발생할 때") {
        When("doFilter를 호출하면") {
            Then("예외가 발생하지만 finally 블록에서 UserMetadataContext를 정리한다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = mockk<HttpServletResponse>(relaxed = true)
                val filterChain = mockk<FilterChain>(relaxed = true)

                every { request.getHeader("User-Agent") } returns "Mozilla/5.0"
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA
                every { filterChain.doFilter(any(), any()) } throws RuntimeException("테스트 예외")

                shouldThrow<RuntimeException> {
                    filter.doFilter(request, response, filterChain)
                }

                val retrieved by context
                retrieved shouldBe null
            }
        }
    }
})
