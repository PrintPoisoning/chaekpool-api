package io.chaekpool.auth.token.service

import io.chaekpool.auth.token.entity.BlacklistEntity
import io.chaekpool.auth.token.exception.TokenBlacklistedException
import io.chaekpool.auth.token.repository.BlacklistRepository
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID

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
            Then("토큰이 만료 시간과 함께 저장된다") {
                val userId = UUID.randomUUID()
                val token = "valid-access-token"
                val expiresIn = 3600L
                val slot = slot<BlacklistEntity>()

                every { jwtProvider.getExpirationTime(token) } returns expiresIn
                every { blacklistRepository.save(capture(slot)) } answers { firstArg() }

                blacklistManager.blacklistToken(userId, token)

                verify(exactly = 1) { blacklistRepository.save(slot.captured) }
                slot.captured.key shouldBe "$userId:$token"
                slot.captured.token shouldBe token
                slot.captured.expiration shouldBe expiresIn
            }
        }
    }

    Given("만료 시간이 0초인 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("저장이 수행되지 않는다") {
                val userId = UUID.randomUUID()
                val token = "expired-token"

                every { jwtProvider.getExpirationTime(token) } returns 0L

                blacklistManager.blacklistToken(userId, token)

                verify(exactly = 0) { blacklistRepository.save(any()) }
            }
        }
    }

    Given("만료 시간이 음수인 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("저장이 수행되지 않는다") {
                val userId = UUID.randomUUID()
                val token = "negative-ttl-token"

                every { jwtProvider.getExpirationTime(token) } returns -100L

                blacklistManager.blacklistToken(userId, token)

                verify(exactly = 0) { blacklistRepository.save(any()) }
            }
        }
    }

    Given("만료 시간이 1초인 토큰이 주어졌을 때") {
        When("해당 토큰을 블랙리스트에 추가하면") {
            Then("토큰이 저장된다") {
                val userId = UUID.randomUUID()
                val token = "about-to-expire-token"
                val expiresIn = 1L
                val slot = slot<BlacklistEntity>()

                every { jwtProvider.getExpirationTime(token) } returns expiresIn
                every { blacklistRepository.save(capture(slot)) } answers { firstArg() }

                blacklistManager.blacklistToken(userId, token)

                verify(exactly = 1) { blacklistRepository.save(slot.captured) }
                slot.captured.expiration shouldBe expiresIn
            }
        }
    }

    Given("블랙리스트에 이미 등록된 토큰이 주어졌을 때") {
        When("해당 토큰을 검증하면") {
            Then("TokenBlacklistedException이 발생한다") {
                val userId = UUID.randomUUID()
                val token = "blacklisted-token"
                val key = "$userId:$token"

                every { blacklistRepository.existsById(key) } returns true

                shouldThrow<TokenBlacklistedException> {
                    blacklistManager.assertToken(userId, token)
                }
            }
        }
    }

    Given("블랙리스트에 등록되지 않은 토큰이 주어졌을 때") {
        When("해당 토큰을 검증하면") {
            Then("예외가 발생하지 않는다") {
                val userId = UUID.randomUUID()
                val token = "clean-token"
                val key = "$userId:$token"

                every { blacklistRepository.existsById(key) } returns false

                shouldNotThrow<TokenBlacklistedException> {
                    blacklistManager.assertToken(userId, token)
                }
            }
        }
    }

    Given("동일한 사용자의 서로 다른 두 개의 토큰이 주어졌을 때") {
        When("두 토큰을 순차적으로 블랙리스트에 추가하면") {
            Then("두 번의 저장이 수행된다") {
                val userId = UUID.randomUUID()
                val token1 = "first-token"
                val token2 = "second-token"

                every { jwtProvider.getExpirationTime(token1) } returns 3600L
                every { jwtProvider.getExpirationTime(token2) } returns 3600L
                every { blacklistRepository.save(any()) } answers { firstArg() }

                blacklistManager.blacklistToken(userId, token1)
                blacklistManager.blacklistToken(userId, token2)

                verify(exactly = 2) { blacklistRepository.save(any()) }
            }
        }
    }

    Given("서로 다른 사용자의 동일한 토큰이 주어졌을 때") {
        When("각 사용자별로 토큰을 블랙리스트에 추가하면") {
            Then("두 번의 저장이 수행된다") {
                val userId1 = UUID.randomUUID()
                val userId2 = UUID.randomUUID()
                val token = "same-token"

                every { jwtProvider.getExpirationTime(token) } returns 3600L
                every { blacklistRepository.save(any()) } answers { firstArg() }

                blacklistManager.blacklistToken(userId1, token)
                blacklistManager.blacklistToken(userId2, token)

                verify(exactly = 2) { blacklistRepository.save(any()) }
            }
        }
    }
})
