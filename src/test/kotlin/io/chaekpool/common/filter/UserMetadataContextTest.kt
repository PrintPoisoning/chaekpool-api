package io.chaekpool.common.filter

import io.chaekpool.common.dto.UserMetadata
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class UserMetadataContextTest : BehaviorSpec({
    val context = UserMetadataContext()

    afterEach {
        context.clear()
    }

    Given("빈 UserMetadataContext가 있을 때") {
        When("메타데이터(IP: 127.0.0.1, userAgent: test-agent)를 set하면") {
            Then("다시 조회했을 때 동일한 메타데이터를 얻을 수 있다") {
                val metadata = UserMetadata(
                    ip = "127.0.0.1",
                    userAgent = "test-agent",
                    device = "test-device",
                    os = "test-os",
                    browser = "test-browser",
                    platformType = "DESKTOP",
                    locale = "ko-KR",
                    timezone = "Asia/Seoul"
                )

                context.set(metadata)

                val retrieved by context
                retrieved shouldBe metadata
            }
        }

        When("set 후 clear를 호출하면") {
            Then("저장된 메타데이터가 제거된다") {
                val metadata = UserMetadata(
                    ip = "127.0.0.1",
                    userAgent = "test-agent",
                    device = "test-device",
                    os = "test-os",
                    browser = "test-browser",
                    platformType = "DESKTOP",
                    locale = "ko-KR",
                    timezone = "Asia/Seoul"
                )

                context.set(metadata)
                context.clear()

                val retrieved by context
                retrieved shouldBe null
            }
        }

        When("여러 번 set을 호출하면") {
            Then("마지막 값이 저장된다") {
                val metadata = UserMetadata(
                    ip = "127.0.0.1",
                    userAgent = "test-agent",
                    device = "test-device",
                    os = "test-os",
                    browser = "test-browser",
                    platformType = "DESKTOP",
                    locale = "ko-KR",
                    timezone = "Asia/Seoul"
                )
                val firstMetadata = metadata.copy(ip = "1.1.1.1")
                val secondMetadata = metadata.copy(ip = "2.2.2.2")

                context.set(firstMetadata)
                context.set(secondMetadata)

                val retrieved by context
                retrieved?.ip shouldBe "2.2.2.2"
            }
        }
    }

    Given("멀티스레드 환경에서") {
        When("서로 다른 스레드에서 set을 호출하면") {
            Then("각 스레드의 값이 격리된다") {
                val metadata1 = UserMetadata(
                    ip = "1.1.1.1", userAgent = "agent1", device = null,
                    os = null, browser = null, platformType = "DESKTOP",
                    locale = "ko-KR", timezone = "Asia/Seoul"
                )
                val metadata2 = UserMetadata(
                    ip = "2.2.2.2", userAgent = "agent2", device = null,
                    os = null, browser = null, platformType = "MOBILE",
                    locale = "en-US", timezone = "America/New_York"
                )

                val thread1Result = CompletableDeferred<String?>()
                val thread2Result = CompletableDeferred<String?>()

                thread {
                    context.set(metadata1)
                    Thread.sleep(50)
                    val retrieved by context
                    thread1Result.complete(retrieved?.ip)
                    context.clear()
                }

                thread {
                    context.set(metadata2)
                    Thread.sleep(50)
                    val retrieved by context
                    thread2Result.complete(retrieved?.ip)
                    context.clear()
                }

                runBlocking {
                    thread1Result.await() shouldBe "1.1.1.1"
                    thread2Result.await() shouldBe "2.2.2.2"
                }
            }
        }
    }
})
