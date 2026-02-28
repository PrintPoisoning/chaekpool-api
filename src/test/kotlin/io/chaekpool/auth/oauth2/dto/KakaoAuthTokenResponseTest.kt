package io.chaekpool.auth.oauth2.dto

import io.chaekpool.auth.oauth2.repository.ProviderAccountRepository
import io.chaekpool.common.util.UUIDv7
import io.chaekpool.support.TestcontainersConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jooq.DSLContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfig::class)
class KakaoAuthTokenResponseTest(
    private val jsonMapper: JsonMapper,
    private val dsl: DSLContext,
    private val providerAccountRepository: ProviderAccountRepository
) : BehaviorSpec({

    Given("KakaoAuthTokenResponse JSONB 직렬화 roundtrip") {
        val original = KakaoAuthTokenResponse(
            tokenType = "bearer",
            accessToken = "test-access-token-value",
            idToken = "test-id-token",
            expiresIn = 3600,
            refreshToken = "test-refresh-token-value",
            refreshTokenExpiresIn = 604800,
            scope = "profile"
        )

        When("JsonMapper로 직렬화하면") {
            val json = jsonMapper.writeValueAsString(original)

            Then("accessToken과 refreshToken 필드가 암호화된 형식으로 저장된다") {
                json shouldNotContain "test-access-token-value"
                json shouldNotContain "test-refresh-token-value"
                json shouldContain "ENC("
            }
        }

        When("직렬화 후 역직렬화하면") {
            val json = jsonMapper.writeValueAsString(original)
            val deserialized = jsonMapper.readValue(json, KakaoAuthTokenResponse::class.java)

            Then("원본 값이 보존된다") {
                deserialized.tokenType shouldBe original.tokenType
                deserialized.accessToken shouldBe original.accessToken
                deserialized.idToken shouldBe original.idToken
                deserialized.expiresIn shouldBe original.expiresIn
                deserialized.refreshToken shouldBe original.refreshToken
                deserialized.refreshTokenExpiresIn shouldBe original.refreshTokenExpiresIn
                deserialized.scope shouldBe original.scope
            }
        }
    }

    Given("평문 JSON이 주어졌을 때") {
        val plainJson = """
            {
                "token_type": "bearer",
                "access_token": "plain-access-token",
                "id_token": null,
                "expires_in": 3600,
                "refresh_token": "plain-refresh-token",
                "refresh_token_expires_in": 604800,
                "scope": null
            }
        """.trimIndent()

        When("역직렬화하면") {
            val deserialized = jsonMapper.readValue(plainJson, KakaoAuthTokenResponse::class.java)

            Then("CryptoProvider passthrough로 평문 값이 그대로 반환된다") {
                deserialized.accessToken shouldBe "plain-access-token"
                deserialized.refreshToken shouldBe "plain-refresh-token"
            }
        }
    }

    Given("DB JSONB 저장/조회 roundtrip") {
        val original = KakaoAuthTokenResponse(
            tokenType = "bearer",
            accessToken = "db-roundtrip-access-token",
            idToken = "db-roundtrip-id-token",
            expiresIn = 3600,
            refreshToken = "db-roundtrip-refresh-token",
            refreshTokenExpiresIn = 604800,
            scope = "profile"
        )

        When("provider_accounts에 JSONB로 저장 후 조회하면") {
            val userId = UUIDv7.generate()
            dsl.execute("INSERT INTO users (id) VALUES (?)", userId)
            val providerId = dsl.fetchOne(
                "SELECT id FROM auth_providers WHERE provider_name = 'KAKAO'"
            )!!.get(0, UUID::class.java)

            providerAccountRepository.saveProviderAccount(
                userId = userId,
                providerId = providerId,
                accountId = "test-account-id-${UUIDv7.generate()}",
                accountRegistry = KakaoApiAccountResponse(
                    id = 12345L,
                    connectedAt = null,
                    properties = null,
                    kakaoAccount = null
                ),
                authRegistry = original
            )

            val saved = providerAccountRepository.findByUserIdAndProviderId(userId, providerId)

            Then("auth_registry에 암호화된 JSON이 저장되고 역직렬화 시 원본 값이 복원된다") {
                saved shouldNotBe null
                val rawJson = saved!!.authRegistry?.data()
                rawJson shouldNotBe null
                rawJson!! shouldNotContain "db-roundtrip-access-token"
                rawJson shouldNotContain "db-roundtrip-refresh-token"
                rawJson shouldContain "ENC("

                val deserialized = jsonMapper.readValue(rawJson, KakaoAuthTokenResponse::class.java)
                deserialized.accessToken shouldBe "db-roundtrip-access-token"
                deserialized.refreshToken shouldBe "db-roundtrip-refresh-token"
                deserialized.tokenType shouldBe "bearer"
            }
        }
    }
})
