package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.config.CookieProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CookieProviderTest : BehaviorSpec({

    val defaultProperties = CookieProperties(
        httpOnly = false,
        secure = false,
        sameSite = "Lax",
        path = "/",
        maxAge = 3600L
    )

    Given("CookieProperties가 주어졌을 때") {
        val provider = CookieProvider(defaultProperties)

        When("refreshTokenCookie를 호출하면") {
            val refreshToken = "default-token"
            val cookie = provider.refreshTokenCookie(refreshToken)

            Then("기본값으로 쿠키가 생성된다") {
                cookie.name shouldBe "refresh_token"
                cookie.value shouldBe refreshToken
                cookie.isHttpOnly shouldBe false
                cookie.isSecure shouldBe false
                cookie.sameSite shouldBe "Lax"
                cookie.path shouldBe "/"
                cookie.maxAge.seconds shouldBe 3600L
            }
        }
    }

    Given("커스텀 CookieProperties가 주어졌을 때") {
        val properties = CookieProperties(
            httpOnly = true,
            secure = true,
            sameSite = "Strict",
            path = "/api",
            maxAge = 7200L
        )
        val provider = CookieProvider(properties)

        When("refreshTokenCookie를 호출하면") {
            val refreshToken = "test-refresh-token"
            val cookie = provider.refreshTokenCookie(refreshToken)

            Then("쿠키 속성이 올바르게 설정된다") {
                cookie.name shouldBe "refresh_token"
                cookie.value shouldBe refreshToken
                cookie.isHttpOnly shouldBe true
                cookie.isSecure shouldBe true
                cookie.sameSite shouldBe "Strict"
                cookie.path shouldBe "/api"
                cookie.maxAge.seconds shouldBe 7200L
            }
        }
    }

    Given("빈 문자열 토큰이 주어졌을 때") {
        val provider = CookieProvider(defaultProperties)

        When("refreshTokenCookie를 호출하면") {
            val cookie = provider.refreshTokenCookie("")

            Then("빈 값으로 쿠키가 생성된다") {
                cookie.name shouldBe "refresh_token"
                cookie.value shouldBe ""
            }
        }
    }

    Given("특수문자가 포함된 토큰이 주어졌을 때") {
        val provider = CookieProvider(defaultProperties)

        When("refreshTokenCookie를 호출하면") {
            val specialToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.abc123-_"
            val cookie = provider.refreshTokenCookie(specialToken)

            Then("토큰 값이 그대로 저장된다") {
                cookie.name shouldBe "refresh_token"
                cookie.value shouldBe specialToken
            }
        }
    }
})
