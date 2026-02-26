package io.chaekpool.common.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ranges.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant
import java.util.UUID

class UUIDv7Test : BehaviorSpec({

    Given("UUID v7을 생성할 때") {
        When("generate()를 호출하면") {
            Then("버전은 v7 이어야 한다") {
                val uuid = UUIDv7.generate()

                uuid.version() shouldBe 7
                UUIDv7.isUuidV7(uuid) shouldBe true
            }

            Then("시간순으로 정렬된다") {
                val uuid1 = UUIDv7.generate()
                Thread.sleep(10)
                val uuid2 = UUIDv7.generate()

                uuid1.toString() shouldBeLessThan uuid2.toString()
            }
        }
    }

    Given("UUID v7에서 시간을 추출할 때") {
        When("extractTimestamp(UUID)를 호출하면") {
            Then("발급 시간을 반환한다") {
                val beforeGenerate = Instant.now().minusMillis(10)
                val uuid = UUIDv7.generate()
                val afterGenerate = Instant.now().plusMillis(10)

                val extracted = UUIDv7.extractTimestamp(uuid)

                extracted shouldBeIn beforeGenerate..afterGenerate
            }
        }

        When("extractTimestamp(String)을 호출하면") {
            Then("발급 시간을 반환한다") {
                val uuid = UUIDv7.generate()
                val uuidString = uuid.toString()

                val extracted = UUIDv7.extractTimestamp(uuidString)

                extracted shouldNotBe null
            }
        }

        When("UUID v4를 전달하면") {
            Then("IllegalArgumentException이 발생한다") {
                val uuidV4 = UUID.randomUUID()

                shouldThrow<IllegalArgumentException> {
                    UUIDv7.extractTimestamp(uuidV4)
                }
            }
        }

        When("null을 전달하면") {
            Then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    UUIDv7.extractTimestamp(null as UUID?)
                }

                exception.message shouldBe "UUID version 7 required, but got version null"
            }
        }
    }

    Given("UUID 버전 확인 시") {
        When("isUuidV7()을 호출하면") {
            Then("UUID v7이면 true를 반환한다") {
                val uuid = UUIDv7.generate()
                UUIDv7.isUuidV7(uuid) shouldBe true
            }

            Then("UUID v4이면 false를 반환한다") {
                val uuidV4 = UUID.randomUUID()
                UUIDv7.isUuidV7(uuidV4) shouldBe false
            }

            Then("null이면 false를 반환한다") {
                UUIDv7.isUuidV7(null) shouldBe false
            }
        }
    }
})
