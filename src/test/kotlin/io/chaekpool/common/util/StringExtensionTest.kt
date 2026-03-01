package io.chaekpool.common.util

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

class StringExtensionTest : BehaviorSpec({
    Given("truncate를 호출했을 때") {
        When("문자열 길이가 maxLength 이하이면") {
            Then("원본 문자열을 그대로 반환한다") {
                val text = "hello"

                text.truncate(10) shouldBe "hello"
            }
        }

        When("문자열 길이가 maxLength와 같으면") {
            Then("원본 문자열을 그대로 반환한다") {
                val text = "12345"

                text.truncate(5) shouldBe "12345"
            }
        }

        When("문자열 길이가 maxLength를 초과하면") {
            Then("maxLength만큼 자르고 ...(truncated)를 붙인다") {
                val text = "abcdefghij"

                val result = text.truncate(5)

                result shouldBe "abcde...(truncated)"
                result shouldEndWith "...(truncated)"
            }
        }

        When("빈 문자열에 대해 호출하면") {
            Then("빈 문자열을 그대로 반환한다") {
                val text = ""

                text.truncate(10) shouldBe ""
            }
        }

        When("한국어 문자열이 maxLength를 초과하면") {
            Then("글자 수 기준으로 자르고 ...(truncated)를 붙인다") {
                val text = "가나다라마바사아자차"

                val result = text.truncate(5)

                result shouldBe "가나다라마...(truncated)"
            }
        }

        When("문자열 길이가 maxLength보다 1만큼 길면") {
            Then("잘림 표시가 붙는다") {
                val text = "123456"

                val result = text.truncate(5)

                result shouldBe "12345...(truncated)"
            }
        }
    }
})
