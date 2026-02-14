package io.chaekpool.common.logger

import feign.Logger
import feign.Request
import feign.Response
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.charset.StandardCharsets

class SingleLineFeignLogger : Logger() {

    private val log = KotlinLogging.logger {}

    override fun log(configKey: String?, format: String?, vararg args: Any?) {
        log.info { String.format(format ?: "", *args) }
    }

    override fun logRequest(configKey: String, logLevel: Level, request: Request) {
        val headers = request.headers().entries.joinToString("; ") { "${it.key}=${it.value}" }
        val body = request.body()?.toString(Charsets.UTF_8)

        log.debug {
            "[FEIGN_REQUEST] method=${request.httpMethod()} url=${request.url()} headers=[$headers] body=$body"
        }
    }

    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        val headers = response.headers().entries.joinToString("; ") { "${it.key}=${it.value}" }
        val body = response.body()?.asInputStream()?.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }

        log.debug {
            "[FEIGN_RESPONSE] status=${response.status()} elapsedTime=${elapsedTime}ms headers=[$headers] body=$body"
        }

        return response.toBuilder()
            .body(body, StandardCharsets.UTF_8)
            .build()
    }
}
