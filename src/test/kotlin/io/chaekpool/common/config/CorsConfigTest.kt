package io.chaekpool.common.config

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

class CorsConfigTest : BehaviorSpec({

    val corsConfig = CorsConfig()

    Given("exact origin만 설정된 경우") {
        val corsProps = CorsProperties(
            allowedOrigins = listOf("http://localhost:3000", "https://example.com")
        )

        When("corsConfigurationSource를 생성하면") {
            Then("allowedOrigins에만 설정되고 allowedOriginPatterns는 비어있다") {
                val source = corsConfig.corsConfigurationSource(corsProps) as UrlBasedCorsConfigurationSource
                val config = source.getCorsConfiguration(MockHttpServletRequest("GET", "/api/v1/test"))

                config!!.allowedOrigins.shouldContainExactly("http://localhost:3000", "https://example.com")
                config.allowedOriginPatterns!!.shouldBeEmpty()
                config.allowCredentials shouldBe true
            }
        }
    }

    Given("와일드카드 패턴만 설정된 경우") {
        val corsProps = CorsProperties(
            allowedOrigins = listOf("*")
        )

        When("corsConfigurationSource를 생성하면") {
            Then("allowedOriginPatterns에만 설정되고 allowedOrigins는 비어있다") {
                val source = corsConfig.corsConfigurationSource(corsProps) as UrlBasedCorsConfigurationSource
                val config = source.getCorsConfiguration(MockHttpServletRequest("GET", "/api/v1/test"))

                config!!.allowedOrigins!!.shouldBeEmpty()
                config.allowedOriginPatterns.shouldContainExactly("*")
            }
        }
    }

    Given("서브도메인 패턴이 설정된 경우") {
        val corsProps = CorsProperties(
            allowedOrigins = listOf("https://*.example.com")
        )

        When("corsConfigurationSource를 생성하면") {
            Then("allowedOriginPatterns에 설정되고 allowedOrigins는 비어있다") {
                val source = corsConfig.corsConfigurationSource(corsProps) as UrlBasedCorsConfigurationSource
                val config = source.getCorsConfiguration(MockHttpServletRequest("GET", "/api/v1/test"))

                config!!.allowedOrigins!!.shouldBeEmpty()
                config.allowedOriginPatterns.shouldContainExactly("https://*.example.com")
            }
        }
    }

    Given("exact origin과 패턴이 혼합된 경우") {
        val corsProps = CorsProperties(
            allowedOrigins = listOf("http://localhost:3000", "https://*.example.com", "https://api.example.com")
        )

        When("corsConfigurationSource를 생성하면") {
            Then("올바르게 분류된다") {
                val source = corsConfig.corsConfigurationSource(corsProps) as UrlBasedCorsConfigurationSource
                val config = source.getCorsConfiguration(MockHttpServletRequest("GET", "/api/v1/test"))

                config!!.allowedOrigins.shouldContainExactly("http://localhost:3000", "https://api.example.com")
                config.allowedOriginPatterns.shouldContainExactly("https://*.example.com")
            }
        }
    }

    Given("빈 리스트가 설정된 경우") {
        val corsProps = CorsProperties(allowedOrigins = emptyList())

        When("corsConfigurationSource를 생성하면") {
            Then("allowedOrigins와 allowedOriginPatterns 모두 비어있다") {
                val source = corsConfig.corsConfigurationSource(corsProps) as UrlBasedCorsConfigurationSource
                val config = source.getCorsConfiguration(MockHttpServletRequest("GET", "/api/v1/test"))

                config!!.allowedOrigins!!.shouldBeEmpty()
                config.allowedOriginPatterns!!.shouldBeEmpty()
            }
        }
    }
})
