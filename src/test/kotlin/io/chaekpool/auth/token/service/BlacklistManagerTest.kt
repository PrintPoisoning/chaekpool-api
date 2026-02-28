package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.entity.BlacklistEntity
import io.chaekpool.auth.token.exception.TokenBlacklistedException
import io.chaekpool.auth.token.repository.BlacklistRepository
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify

class BlacklistManagerTest : BehaviorSpec({

    lateinit var blacklistRepository: BlacklistRepository
    lateinit var jwtProvider: JwtProvider
    lateinit var blacklistManager: BlacklistManager

    beforeTest {
        blacklistRepository = mockk()
        jwtProvider = mockk()
        blacklistManager = BlacklistManager(blacklistRepository, jwtProvider)
    }

    Given("만료 시간이 3600초인 유효한 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("JTI와 만료 시간이 저장된다") {
                val token = "valid-access-token"
                val jti = "test-jti-uuid"
                val expiresIn = 3600L
                val slot = slot<BlacklistEntity>()

                every { jwtProvider.getJti(token) } returns jti
                every { jwtProvider.getExpiresIn(token) } returns expiresIn
                every { blacklistRepository.save(capture(slot)) } answers { firstArg() }

                blacklistManager.blacklistToken(token)

                verify(exactly = 1) { blacklistRepository.save(slot.captured) }
                slot.captured.jti shouldBe jti
                slot.captured.expiresIn shouldBe expiresIn
            }
        }
    }

    Given("만료 시간이 0초인 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("저장이 수행되지 않는다") {
                val token = "expired-token"

                every { jwtProvider.getJti(token) } returns "expired-jti"
                every { jwtProvider.getExpiresIn(token) } returns 0L

                blacklistManager.blacklistToken(token)

                verify(exactly = 0) { blacklistRepository.save(any()) }
            }
        }
    }

    Given("만료 시간이 음수인 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("저장이 수행되지 않는다") {
                val token = "negative-ttl-token"

                every { jwtProvider.getJti(token) } returns "negative-jti"
                every { jwtProvider.getExpiresIn(token) } returns -100L

                blacklistManager.blacklistToken(token)

                verify(exactly = 0) { blacklistRepository.save(any()) }
            }
        }
    }

    Given("만료 시간이 1초인 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("토큰이 저장된다") {
                val token = "about-to-expire-token"
                val jti = "test-jti-uuid-2"
                val expiresIn = 1L
                val slot = slot<BlacklistEntity>()

                every { jwtProvider.getJti(token) } returns jti
                every { jwtProvider.getExpiresIn(token) } returns expiresIn
                every { blacklistRepository.save(capture(slot)) } answers { firstArg() }

                blacklistManager.blacklistToken(token)

                verify(exactly = 1) { blacklistRepository.save(slot.captured) }
                slot.captured.expiresIn shouldBe expiresIn
            }
        }
    }

    Given("블랙리스트에 이미 등록된 토큰이 주어졌을 때") {
        When("해당 토큰을 검증하면") {
            Then("TokenBlacklistedException이 발생한다") {
                val token = "blacklisted-token"
                val jti = "blacklisted-jti"

                every { jwtProvider.assertToken(token) } just runs
                every { jwtProvider.getJti(token) } returns jti
                every { blacklistRepository.existsById(jti) } returns true

                val exception = shouldThrow<TokenBlacklistedException> {
                    blacklistManager.assertToken(token)
                }
                exception.errorCode shouldBe "TOKEN_BLACKLISTED"
                exception.message shouldBe "JWT token blacklisted"
            }
        }
    }

    Given("블랙리스트에 등록되지 않은 토큰이 주어졌을 때") {
        When("해당 토큰을 검증하면") {
            Then("예외가 발생하지 않는다") {
                val token = "clean-token"
                val jti = "clean-jti"

                every { jwtProvider.assertToken(token) } just runs
                every { jwtProvider.getJti(token) } returns jti
                every { blacklistRepository.existsById(jti) } returns false

                shouldNotThrow<TokenBlacklistedException> {
                    blacklistManager.assertToken(token)
                }
            }
        }
    }

    Given("동일한 사용자의 서로 다른 두 개의 토큰이 주어졌을 때") {
        When("두 토큰을 순차적으로 블랙리스트에 추가하면") {
            Then("두 번의 저장이 수행된다") {
                val token1 = "first-token"
                val token2 = "second-token"

                every { jwtProvider.getJti(token1) } returns "jti-1"
                every { jwtProvider.getJti(token2) } returns "jti-2"
                every { jwtProvider.getExpiresIn(token1) } returns 3600L
                every { jwtProvider.getExpiresIn(token2) } returns 3600L
                every { blacklistRepository.save(any()) } answers { firstArg() }

                blacklistManager.blacklistToken(token1)
                blacklistManager.blacklistToken(token2)

                verify(exactly = 2) { blacklistRepository.save(any()) }
            }
        }
    }

    Given("서로 다른 사용자의 동일한 토큰이 주어졌을 때") {
        When("각 사용자별로 토큰을 블랙리스트에 추가하면") {
            Then("두 번의 저장이 수행된다") {
                val token = "same-token"
                val jti = "same-jti"

                every { jwtProvider.getJti(token) } returns jti
                every { jwtProvider.getExpiresIn(token) } returns 3600L
                every { blacklistRepository.save(any()) } answers { firstArg() }

                blacklistManager.blacklistToken(token)
                blacklistManager.blacklistToken(token)

                verify(exactly = 2) { blacklistRepository.save(any()) }
            }
        }
    }
})
