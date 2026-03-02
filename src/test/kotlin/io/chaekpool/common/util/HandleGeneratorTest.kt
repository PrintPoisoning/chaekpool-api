package io.chaekpool.common.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldStartWith

class HandleGeneratorTest : BehaviorSpec({

    Given("HandleGenerator") {
        When("generate를 호출하면") {
            Then("user_ 접두사 + 8자리 소문자 영숫자 형식의 handle을 반환한다") {
                val handle = HandleGenerator.generate()

                handle shouldStartWith "user_"
                handle.length shouldBe 13
                handle shouldMatch Regex("^user_[a-z0-9]{8}$")
            }
        }

        When("generate를 여러 번 호출하면") {
            Then("매번 다른 값을 생성한다") {
                val handles = (1..10).map { HandleGenerator.generate() }.toSet()

                handles.size shouldNotBe 1
            }
        }
    }

    Given("generateUnique") {
        When("중복이 없으면") {
            Then("첫 번째 시도에서 handle을 반환한다") {
                val handle = HandleGenerator.generateUnique { false }

                handle shouldMatch Regex("^user_[a-z0-9]{8}$")
            }
        }

        When("매번 중복이 발생하면") {
            Then("maxRetry 초과 시 IllegalStateException이 발생한다") {
                val exception = shouldThrow<IllegalStateException> {
                    HandleGenerator.generateUnique { true }
                }
                exception.message shouldBe "고유 handle 생성 실패: 3회 재시도 초과"
            }
        }

        When("maxRetry를 지정하면") {
            Then("지정한 횟수만큼 재시도 후 실패한다") {
                var callCount = 0
                val exception = shouldThrow<IllegalStateException> {
                    HandleGenerator.generateUnique(maxRetry = 5) {
                        callCount++
                        true
                    }
                }
                callCount shouldBe 5
                exception.message shouldBe "고유 handle 생성 실패: 5회 재시도 초과"
            }
        }
    }
})
