package io.chaekpool.common.util

import io.chaekpool.common.util.MaskingUtil.maskIpLastOctets
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MaskingUtilTest : BehaviorSpec({

    Given("IPv4 주소가 주어졌을 때") {
        val ip = "192.168.1.1"

        When("maskIpLastOctets(2)를 호출하면") {
            Then("마지막 2개 옥텟이 마스킹된다") {
                ip.maskIpLastOctets(2) shouldBe "192.168.xxx.xxx"
            }
        }

        When("maskIpLastOctets(1)을 호출하면") {
            Then("마지막 1개 옥텟만 마스킹된다") {
                ip.maskIpLastOctets(1) shouldBe "192.168.1.xxx"
            }
        }

        When("maskIpLastOctets(3)을 호출하면") {
            Then("마지막 3개 옥텟이 마스킹된다") {
                ip.maskIpLastOctets(3) shouldBe "192.xxx.xxx.xxx"
            }
        }
    }

    Given("IPv4가 아닌 문자열이 주어졌을 때") {
        When("maskIpLastOctets를 호출하면") {
            Then("원본을 그대로 반환한다") {
                "not-an-ip".maskIpLastOctets(2) shouldBe "not-an-ip"
                "256.1.1.1".maskIpLastOctets(2) shouldBe "256.1.1.1"
                "192.168.1".maskIpLastOctets(2) shouldBe "192.168.1"
            }
        }
    }

    Given("잘못된 octetsToMask 값이 주어졌을 때") {
        val ip = "192.168.1.1"

        When("0을 전달하면") {
            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    ip.maskIpLastOctets(0)
                }
            }
        }

        When("4를 전달하면") {
            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    ip.maskIpLastOctets(4)
                }
            }
        }
    }
})
