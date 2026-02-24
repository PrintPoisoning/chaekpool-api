package io.chaekpool.common.util

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import java.util.Locale

class UserMetadataExtractorTest : BehaviorSpec({
    val extractor = UserMetadataExtractor()

    Given("HTTP 요청이 주어졌을 때") {
        When("정상적인 User-Agent('Mozilla/5.0')와 IP('192.168.1.1')가 있으면") {
            Then("해당 정보를 포함한 UserMetadata를 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("User-Agent") } returns "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.getHeader("Time-Zone") } returns null
                every { request.remoteAddr } returns "192.168.1.1"
                every { request.locale } returns Locale.KOREA

                val metadata = extractor.extract(request)

                metadata.ip shouldBe "192.168.1.1"
                metadata.userAgent shouldBe "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
            }
        }

        When("User-Agent 헤더가 null이면") {
            Then("userAgent를 'UNKNOWN'으로 설정한 UserMetadata를 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("User-Agent") } returns null
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.getHeader("Time-Zone") } returns null
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA

                val metadata = extractor.extract(request)

                metadata.userAgent shouldBe "UNKNOWN"
            }
        }

        When("Mobile User-Agent('iPhone')가 주어지면") {
            Then("platformType을 'MOBILE'로 파싱한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("User-Agent") } returns "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) Mobile"
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.getHeader("Time-Zone") } returns null
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA

                val metadata = extractor.extract(request)
                metadata.platformType shouldBe "MOBILE"
            }
        }

        When("Tablet User-Agent('iPad')가 주어지면") {
            Then("platformType을 'TABLET'로 파싱한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("User-Agent") } returns "Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X) Tablet"
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.getHeader("Time-Zone") } returns null
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA

                val metadata = extractor.extract(request)
                metadata.platformType shouldBe "TABLET"
            }
        }

        When("Bot User-Agent('Googlebot')가 주어지면") {
            Then("platformType을 'BOT'로 파싱한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("User-Agent") } returns "Googlebot/2.1 (+http://www.google.com/bot.html)"
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.getHeader("Time-Zone") } returns null
                every { request.remoteAddr } returns "127.0.0.1"
                every { request.locale } returns Locale.KOREA

                val metadata = extractor.extract(request)
                metadata.platformType shouldBe "BOT"
            }
        }
    }

    Given("HTTP 요청에서 IP를 추출할 때") {
        When("X-Forwarded-For 헤더('203.0.113.1')가 있으면") {
            Then("해당 IP를 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("X-Forwarded-For") } returns "203.0.113.1"
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.remoteAddr } returns "192.168.1.1"

                val ip = extractor.resolveClientIp(request)
                ip shouldBe "203.0.113.1"
            }
        }

        When("X-Forwarded-For에 쉼표로 구분된 IP 목록이 있으면") {
            Then("첫 번째 IP('203.0.113.1')를 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("X-Forwarded-For") } returns "203.0.113.1, 192.168.1.1, 10.0.0.1"
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.remoteAddr } returns "127.0.0.1"

                val ip = extractor.resolveClientIp(request)
                ip shouldBe "203.0.113.1"
            }
        }

        When("IPv6 루프백 주소('::1')가 주어지면") {
            Then("'127.0.0.1'로 정규화하여 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.remoteAddr } returns "::1"

                val ip = extractor.resolveClientIp(request)
                ip shouldBe "127.0.0.1"
            }
        }

        When("IPv6 루프백 주소('0:0:0:0:0:0:0:1')가 주어지면") {
            Then("'127.0.0.1'로 정규화하여 반환한다") {
                val request = mockk<HttpServletRequest>()
                every { request.getHeader("X-Forwarded-For") } returns null
                every { request.getHeader("X-Real-IP") } returns null
                every { request.getHeader("Proxy-Client-IP") } returns null
                every { request.getHeader("WL-Proxy-Client-IP") } returns null
                every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
                every { request.getHeader("HTTP_CLIENT_IP") } returns null
                every { request.remoteAddr } returns "0:0:0:0:0:0:0:1"

                val ip = extractor.resolveClientIp(request)
                ip shouldBe "127.0.0.1"
            }
        }
    }
})
