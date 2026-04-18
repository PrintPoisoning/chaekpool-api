package io.chaekpool.auth.oauth2.dto

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import tools.jackson.databind.json.JsonMapper

class KakaoCallbackResponseTest : BehaviorSpec({

    val jsonMapper = JsonMapper.builder().build()

    Given("authenticated 팩토리 메서드") {
        When("호출하면") {
            Then("type=authenticated + access_token 포함 DTO를 반환한다") {
                val response = KakaoCallbackResponse.authenticated("eyJ.test.token")

                response.type shouldBe "authenticated"
                response.accessToken shouldBe "eyJ.test.token"
                response.rejoinTicket shouldBe null
            }

            Then("JSON 직렬화 시 rejoin_ticket은 제외된다") {
                val response = KakaoCallbackResponse.authenticated("eyJ.test.token")
                val json = jsonMapper.writeValueAsString(response)

                json shouldContain "\"type\":\"authenticated\""
                json shouldContain "\"access_token\":\"eyJ.test.token\""
                json shouldNotContain "rejoin_ticket"
            }
        }
    }

    Given("rejoinRequired 팩토리 메서드") {
        When("호출하면") {
            Then("type=rejoin_required + rejoin_ticket 포함 DTO를 반환한다") {
                val response = KakaoCallbackResponse.rejoinRequired("ticket-uuid-1234")

                response.type shouldBe "rejoin_required"
                response.accessToken shouldBe null
                response.rejoinTicket shouldBe "ticket-uuid-1234"
            }

            Then("JSON 직렬화 시 access_token은 제외된다") {
                val response = KakaoCallbackResponse.rejoinRequired("ticket-uuid-1234")
                val json = jsonMapper.writeValueAsString(response)

                json shouldContain "\"type\":\"rejoin_required\""
                json shouldContain "\"rejoin_ticket\":\"ticket-uuid-1234\""
                json shouldNotContain "access_token"
            }
        }
    }

    Given("type 상수") {
        When("참조하면") {
            Then("TYPE_AUTHENTICATED, TYPE_REJOIN_REQUIRED가 올바른 값을 가진다") {
                KakaoCallbackResponse.TYPE_AUTHENTICATED shouldBe "authenticated"
                KakaoCallbackResponse.TYPE_REJOIN_REQUIRED shouldBe "rejoin_required"
            }
        }
    }
})
