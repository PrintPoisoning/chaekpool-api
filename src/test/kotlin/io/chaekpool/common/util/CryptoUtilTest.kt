package io.chaekpool.common.util

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith

class CryptoUtilTest : BehaviorSpec({

    beforeSpec {
        CryptoUtil.init("test-secret-key-minimum-32-chars")
    }

    Given("평문 문자열이 주어졌을 때") {
        When("encrypt를 호출하면") {
            Then("ENC() 형식으로 암호화된다") {
                val plainText = "kakao-access-token-12345"
                val encrypted = CryptoUtil.encrypt(plainText)

                encrypted shouldStartWith "ENC("
                encrypted shouldEndWith ")"
                encrypted shouldNotBe plainText
            }
        }
    }

    Given("암호화된 문자열이 주어졌을 때") {
        When("decrypt를 호출하면") {
            Then("원본 평문이 복원된다") {
                val plainText = "kakao-refresh-token-67890"
                val encrypted = CryptoUtil.encrypt(plainText)
                val decrypted = CryptoUtil.decrypt(encrypted)

                decrypted shouldBe plainText
            }
        }
    }

    Given("평문이 주어졌을 때") {
        When("decrypt를 호출하면") {
            Then("평문 그대로 반환된다") {
                val plainText = "not-encrypted-text"
                val result = CryptoUtil.decrypt(plainText)

                result shouldBe plainText
            }
        }
    }

    Given("같은 평문을 여러 번 암호화할 때") {
        When("encrypt를 호출하면") {
            Then("매번 다른 암호문이 생성된다") {
                val plainText = "same-plain-text"
                val encrypted1 = CryptoUtil.encrypt(plainText)
                val encrypted2 = CryptoUtil.encrypt(plainText)

                encrypted1 shouldNotBe encrypted2

                val decrypted1 = CryptoUtil.decrypt(encrypted1)
                val decrypted2 = CryptoUtil.decrypt(encrypted2)

                decrypted1 shouldBe plainText
                decrypted2 shouldBe plainText
            }
        }
    }
})
