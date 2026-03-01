package io.chaekpool.common.logger

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import feign.Feign
import feign.Headers
import feign.Logger.Level
import feign.RequestLine
import feign.Response
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level as LogbackLevel
import ch.qos.logback.classic.Logger as LogbackLogger

class SingleLineFeignLoggerTest : BehaviorSpec({
    val wireMock = WireMockServer(wireMockConfig().dynamicPort())
    val logbackLogger = LoggerFactory.getLogger(SingleLineFeignLogger::class.java) as LogbackLogger
    lateinit var appender: ListAppender<ILoggingEvent>

    fun buildClient(logLevel: Level): TestFeignClient =
        Feign.builder()
            .logger(SingleLineFeignLogger())
            .logLevel(logLevel)
            .target(TestFeignClient::class.java, wireMock.baseUrl())

    fun requestLog(): String? =
        appender.list.map { it.formattedMessage }.firstOrNull { it.contains("[HTTP_EXT_REQ]") }

    fun responseLog(): String? =
        appender.list.map { it.formattedMessage }.firstOrNull { it.contains("[HTTP_EXT_RES]") }

    beforeSpec {
        wireMock.start()
    }

    afterSpec {
        wireMock.stop()
    }

    beforeTest {
        wireMock.resetAll()
        appender = ListAppender()
        appender.start()
        logbackLogger.addAppender(appender)
        logbackLogger.level = LogbackLevel.DEBUG
    }

    afterTest {
        logbackLogger.detachAppender(appender)
        appender.stop()
    }

    Given("BASIC 레벨로 GET 요청 시") {
        When("Feign 클라이언트가 호출되면") {
            Then("요청/응답 로그에 기본 정보만 포함되고 헤더와 바디는 제외된다") {
                wireMock.stubFor(
                    get(urlEqualTo("/test"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withBody("""{"message":"hello"}""")
                        )
                )

                buildClient(Level.BASIC).get()

                val reqLog = requestLog()
                reqLog shouldNotBe null
                reqLog shouldContain "[HTTP_EXT_REQ]"
                reqLog shouldContain "method=GET"
                reqLog shouldContain "/test"
                reqLog shouldNotContain "headers="
                reqLog shouldNotContain "body="

                val resLog = responseLog()
                resLog shouldNotBe null
                resLog shouldContain "[HTTP_EXT_RES]"
                resLog shouldContain "status=200"
                resLog shouldContain "elapsed="
                resLog shouldNotContain "headers="
                resLog shouldNotContain "body="
            }
        }
    }

    Given("HEADERS 레벨로 GET 요청 시") {
        When("Feign 클라이언트가 호출되면") {
            Then("요청/응답 로그에 헤더가 포함되고 바디는 제외된다") {
                wireMock.stubFor(
                    get(urlEqualTo("/test"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withHeader("X-Custom", "test-value")
                                .withBody("""{"message":"hello"}""")
                        )
                )

                buildClient(Level.HEADERS).get()

                val reqLog = requestLog()
                reqLog shouldNotBe null
                reqLog shouldContain "headers="
                reqLog shouldNotContain "body="

                val resLog = responseLog()
                resLog shouldNotBe null
                resLog shouldContain "headers="
                resLog shouldNotContain "body="
            }
        }
    }

    Given("FULL 레벨로 POST 요청 시") {
        When("Feign 클라이언트가 호출되면") {
            Then("요청/응답 로그에 헤더와 바디가 모두 포함된다") {
                wireMock.stubFor(
                    post(urlEqualTo("/test"))
                        .willReturn(
                            aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""{"result":"ok"}""")
                        )
                )

                buildClient(Level.FULL).post("""{"input":"data"}""")

                val reqLog = requestLog()
                reqLog shouldNotBe null
                reqLog shouldContain "headers="
                reqLog shouldContain "body="

                val resLog = responseLog()
                resLog shouldNotBe null
                resLog shouldContain "headers="
                resLog shouldContain "body="
                resLog shouldContain """{"result":"ok"}"""
            }
        }
    }

    Given("FULL 레벨로 204 No Content 응답 시") {
        When("Feign 클라이언트가 호출되면") {
            Then("예외 없이 처리되고 응답 로그에 status=204가 포함된다") {
                wireMock.stubFor(
                    delete(urlEqualTo("/test"))
                        .willReturn(aResponse().withStatus(204))
                )

                buildClient(Level.FULL).delete()

                val resLog = responseLog()
                resLog shouldNotBe null
                resLog shouldContain "status=204"
            }
        }
    }
})

private interface TestFeignClient {
    @RequestLine("GET /test")
    fun get(): String

    @RequestLine("POST /test")
    @Headers("Content-Type: application/json")
    fun post(body: String): String

    @RequestLine("DELETE /test")
    fun delete(): Response
}
