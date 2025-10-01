package io.chaekpool.common.logger

import feign.Logger
import feign.Request
import feign.Response
import io.chaekpool.common.util.LoggerDelegate
import java.nio.charset.StandardCharsets

class SingleLineFeignLogger : Logger() {

    private val log by LoggerDelegate()

    override fun log(configKey: String?, format: String?, vararg args: Any?) {
        log.info(format ?: "", *args)
    }

    override fun logRequest(configKey: String, logLevel: Level, request: Request) {
        val headers = request.headers().entries.joinToString("; ") { "${it.key}=${it.value}" }
        val body = request.body()?.toString(Charsets.UTF_8)

        log.debug(
            "[FEIGN_REQUEST] method={} url={} headers=[{}] body={}",
            request.httpMethod(), request.url(), headers, body
        )
    }

    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        val headers = response.headers().entries.joinToString("; ") { "${it.key}=${it.value}" }
        val body = response.body()?.asInputStream()?.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }

        log.debug(
            "[FEIGN_RESPONSE] status={} elapsedTime={}ms headers=[{}] body={}",
            response.status(), elapsedTime, headers, body
        )

        return response.toBuilder()
            .body(body, StandardCharsets.UTF_8)
            .build()
    }
}
