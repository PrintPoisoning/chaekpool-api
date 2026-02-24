package io.chaekpool.common.util

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class AssertionExtensionTest : BehaviorSpec({
    Given("isTrueOrThrow를 호출했을 때") {
        When("true로 검증하면") {
            Then("예외가 발생하지 않는다") {
                shouldNotThrow<Exception> {
                    true.isTrueOrThrow { IllegalArgumentException("오류") }
                }
            }
        }
        When("false로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    false.isTrueOrThrow { IllegalArgumentException("오류") }
                }
            }
        }
    }

    Given("notNullOrThrow를 호출했을 때") {
        When("null이 아닌 값으로 검증하면") {
            Then("값을 반환한다") {
                val value = "test"
                value.notNullOrThrow { IllegalArgumentException() } shouldBe "test"
            }
        }
        When("null로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    val value: String? = null
                    value.notNullOrThrow { IllegalArgumentException() }
                }
            }
        }
    }

    Given("hasTextOrThrow를 호출했을 때") {
        When("공백이 아닌 문자열로 검증하면") {
            Then("문자열을 반환한다") {
                "hello".hasTextOrThrow { IllegalArgumentException() } shouldBe "hello"
            }
        }
        When("null로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    val value: String? = null
                    value.hasTextOrThrow { IllegalArgumentException() }
                }
            }
        }
        When("빈 문자열로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    "".hasTextOrThrow { IllegalArgumentException() }
                }
            }
        }
        When("공백만 있는 문자열로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    "   ".hasTextOrThrow { IllegalArgumentException() }
                }
            }
        }
    }

    Given("notEmptyOrThrow를 호출했을 때") {
        When("비어있지 않은 컬렉션으로 검증하면") {
            Then("컬렉션을 반환한다") {
                val list = listOf(1, 2, 3)
                list.notEmptyOrThrow { IllegalArgumentException() } shouldBe list
            }
        }
        When("null 컬렉션으로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    val list: List<Int>? = null
                    list.notEmptyOrThrow { IllegalArgumentException() }
                }
            }
        }
        When("빈 컬렉션으로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    emptyList<Int>().notEmptyOrThrow { IllegalArgumentException() }
                }
            }
        }
    }

    Given("requireOrThrow를 호출했을 때") {
        When("조건을 만족하는 값으로 검증하면") {
            Then("값을 반환한다") {
                val number = 10
                number.requireOrThrow({ it > 5 }) { IllegalArgumentException() } shouldBe 10
            }
        }
        When("조건을 만족하지 않는 값으로 검증하면") {
            Then("지정한 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    val number = 3
                    number.requireOrThrow({ it > 5 }) { IllegalArgumentException() }
                }
            }
        }
    }
})
