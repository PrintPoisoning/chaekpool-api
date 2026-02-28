package io.chaekpool.auth.token.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.chaekpool.auth.token.config.JwtProperties
import io.chaekpool.auth.token.exception.InvalidTokenException
import io.chaekpool.auth.token.exception.MissingClaimException
import io.chaekpool.auth.token.exception.TokenExpiredException
import io.chaekpool.common.util.UUIDv7
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.time.Instant
import java.util.Date
import java.util.UUID

class JwtProviderTest : BehaviorSpec({
    // test/resources/application.yml의 auth.jwt.secret과 동일
    val testJwtProperties = JwtProperties(
        secret = "test-jwt-secret-key-for-integration-tests-only-minimum-32-chars",
        accessTokenValiditySeconds = 900,
        refreshTokenValiditySeconds = 604800
    )
    val jwtProvider = JwtProvider(testJwtProperties)

    Given("createAccessToken을 호출했을 때") {
        val userId = UUIDv7.generate()

        When("유효한 userId를 전달하면") {
            val token = jwtProvider.createAccessToken(userId)

            Then("JWT 토큰 문자열을 반환한다") {
                token shouldNotBe null
                token.split(".").size shouldBe 3
            }

            Then("토큰에서 userId를 추출할 수 있다") {
                val extractedUserId = jwtProvider.getUserId(token)
                extractedUserId shouldBe userId
            }

            Then("토큰이 유효하다") {
                shouldNotThrowAny {
                    jwtProvider.assertToken(token)
                }
            }

            Then("토큰에 jti claim이 포함된다") {
                val jti = jwtProvider.getJti(token)
                jti shouldNotBe null
                UUID.fromString(jti)
            }
        }
    }

    Given("createRefreshToken을 호출했을 때") {
        val userId = UUIDv7.generate()
        val provider = "KAKAO"

        When("유효한 userId와 provider를 전달하면") {
            val token = jwtProvider.createRefreshToken(userId, provider)

            Then("JWT 토큰 문자열을 반환한다") {
                token shouldNotBe null
                token.split(".").size shouldBe 3
            }

            Then("만료 시간이 설정값과 일치한다") {
                val expirationTime = jwtProvider.getExpiresIn(token)
                expirationTime shouldBeGreaterThan 604700
                expirationTime shouldBeLessThan 604900
            }

            Then("토큰에서 provider를 추출할 수 있다") {
                val extractedProvider = jwtProvider.getProvider(token)
                extractedProvider shouldBe provider
            }
        }
    }

    Given("assertToken을 호출했을 때") {
        When("유효한 토큰을 전달하면") {
            Then("예외가 발생하지 않는다") {
                val userId = UUIDv7.generate()
                val token = jwtProvider.createAccessToken(userId)

                jwtProvider.assertToken(token)
            }
        }

        When("잘못된 형식의 토큰을 전달하면") {
            Then("InvalidTokenException이 발생한다") {
                val exception = shouldThrow<InvalidTokenException> {
                    jwtProvider.assertToken("invalid.token.format")
                }
                exception.errorCode shouldBe "INVALID_TOKEN"
                exception.message shouldContain "Malformed JWT"
            }
        }

        When("서명이 변조된 토큰을 전달하면") {
            Then("InvalidTokenException이 발생한다") {
                val userId = UUIDv7.generate()
                val validToken = jwtProvider.createAccessToken(userId)
                val tamperedToken = validToken.substring(0, validToken.length - 5) + "XXXXX"

                val exception = shouldThrow<InvalidTokenException> {
                    jwtProvider.assertToken(tamperedToken)
                }
                exception.errorCode shouldBe "INVALID_TOKEN"
                exception.message shouldContain "signature verification failed"
            }
        }

        When("만료된 토큰을 전달하면") {
            Then("TokenExpiredException이 발생한다") {
                val expiredJwtProperties = JwtProperties(
                    secret = testJwtProperties.secret,
                    accessTokenValiditySeconds = -1,
                    refreshTokenValiditySeconds = testJwtProperties.refreshTokenValiditySeconds
                )
                val expiredJwtProvider = JwtProvider(expiredJwtProperties)
                val userId = UUIDv7.generate()
                val expiredToken = expiredJwtProvider.createAccessToken(userId)

                shouldThrow<TokenExpiredException> {
                    jwtProvider.assertToken(expiredToken)
                }
            }
        }

        When("빈 문자열을 전달하면") {
            Then("InvalidTokenException이 발생한다") {
                shouldThrow<InvalidTokenException> {
                    jwtProvider.assertToken("")
                }
            }
        }

        When("exp claim이 없는 토큰을 전달하면") {
            Then("MissingClaimException이 발생한다") {
                // exp 없는 JWT 수동 생성
                val claims = JWTClaimsSet.Builder()
                    .subject(UUIDv7.generate().toString())
                    .issueTime(Date.from(Instant.now()))
                    // exp 생략
                    .build()

                val jwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
                jwt.sign(MACSigner(testJwtProperties.secret))
                val token = jwt.serialize()

                val exception = shouldThrow<MissingClaimException> {
                    jwtProvider.assertToken(token)
                }
                exception.errorCode shouldBe "MISSING_CLAIM"
                exception.message shouldContain "JWT has no exp"
            }
        }
    }

    Given("getUserId를 호출했을 때") {
        When("유효한 토큰을 전달하면") {
            Then("올바른 userId를 반환한다") {
                val userId = UUIDv7.generate()
                val token = jwtProvider.createAccessToken(userId)

                val extractedUserId = jwtProvider.getUserId(token)
                extractedUserId shouldBe userId
            }
        }

        When("잘못된 형식의 토큰을 전달하면") {
            Then("ParseException이 발생한다") {
                shouldThrow<Exception> {
                    jwtProvider.getUserId("invalid.token")
                }
            }
        }
    }

    Given("getExpirationTime을 호출했을 때") {
        When("유효한 accessToken을 전달하면") {
            Then("남은 만료 시간(초)을 반환한다") {
                val userId = UUIDv7.generate()
                val token = jwtProvider.createAccessToken(userId)

                val expirationTime = jwtProvider.getExpiresIn(token)
                expirationTime shouldBeGreaterThan 890
                expirationTime shouldBeLessThan 910
            }
        }

        When("만료된 토큰을 전달하면") {
            Then("음수를 반환한다") {
                val expiredJwtProperties = JwtProperties(
                    secret = testJwtProperties.secret,
                    accessTokenValiditySeconds = -10,
                    refreshTokenValiditySeconds = testJwtProperties.refreshTokenValiditySeconds
                )
                val expiredJwtProvider = JwtProvider(expiredJwtProperties)
                val userId = UUIDv7.generate()
                val expiredToken = expiredJwtProvider.createAccessToken(userId)

                val expirationTime = jwtProvider.getExpiresIn(expiredToken)
                expirationTime shouldBeLessThan 0
            }
        }
    }

    Given("getJti를 호출했을 때") {
        When("유효한 토큰을 전달하면") {
            Then("JTI를 반환한다") {
                val userId = UUIDv7.generate()
                val token = jwtProvider.createAccessToken(userId)

                val jti = jwtProvider.getJti(token)
                jti shouldNotBe null
                UUID.fromString(jti)
            }
        }

        When("jti가 없는 토큰을 전달하면") {
            Then("MissingClaimException이 발생한다") {
                val claims = JWTClaimsSet.Builder()
                    .subject(UUIDv7.generate().toString())
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(900)))
                    .build()

                val jwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
                jwt.sign(MACSigner(testJwtProperties.secret))
                val token = jwt.serialize()

                val exception = shouldThrow<MissingClaimException> {
                    jwtProvider.getJti(token)
                }
                exception.errorCode shouldBe "MISSING_CLAIM"
                exception.message shouldContain "JWT has no jti"
            }
        }
    }

    Given("getProvider를 호출했을 때") {
        When("provider claim이 있는 refresh token을 전달하면") {
            Then("provider 값을 반환한다") {
                val userId = UUIDv7.generate()
                val token = jwtProvider.createRefreshToken(userId, "KAKAO")

                val provider = jwtProvider.getProvider(token)
                provider shouldBe "KAKAO"
            }
        }

        When("provider claim이 없는 토큰을 전달하면") {
            Then("null을 반환한다") {
                val userId = UUIDv7.generate()
                val token = jwtProvider.createAccessToken(userId)

                val provider = jwtProvider.getProvider(token)
                provider shouldBe null
            }
        }
    }
})
