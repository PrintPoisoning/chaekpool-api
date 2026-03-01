package io.chaekpool.common.logger

import feign.Logger
import feign.Request
import feign.Response
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.charset.StandardCharsets

class SingleLineFeignLogger : Logger() {

    private val log = KotlinLogging.logger {}

    override fun log(configKey: String?, format: String?, vararg args: Any?) {
        log.debug { String.format(format ?: "", *args) }
    }

    override fun logRequest(configKey: String, logLevel: Level, request: Request) {
        log.debug { formatRequest(logLevel, request) }
    }

    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        val body = if (includeBody(logLevel)) readBody(response) else null

        log.debug { formatResponse(logLevel, response, elapsedTime, body) }

        return if (body != null) rebuffer(response, body) else response
    }

    private fun formatRequest(logLevel: Level, request: Request): String = buildString {
        append("[HTTP_EXT_REQ] method=${request.httpMethod()} url=${request.url()}")
        if (includeHeaders(logLevel)) append(" headers=[${formatHeaders(request.headers())}]")
        if (includeBody(logLevel)) append(" body=${request.body()?.toString(Charsets.UTF_8)}")
    }

    private fun includeHeaders(logLevel: Level): Boolean =
        logLevel.ordinal >= Level.HEADERS.ordinal

    private fun includeBody(logLevel: Level): Boolean =
        logLevel.ordinal >= Level.FULL.ordinal

    private fun readBody(response: Response): String? =
        response.body()?.asInputStream()?.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }

    private fun formatResponse(
        logLevel: Level,
        response: Response,
        elapsedTime: Long,
        body: String?
    ): String = buildString {
        append("[HTTP_EXT_RES] status=${response.status()} elapsed=${elapsedTime}ms")
        if (includeHeaders(logLevel)) append(" headers=[${formatHeaders(response.headers())}]")
        if (body != null) append(" body=$body")
    }

    private fun rebuffer(response: Response, body: String): Response =
        response.toBuilder().body(body, StandardCharsets.UTF_8).build()

    private fun formatHeaders(headers: Map<String, Collection<String>>): String =
        headers.entries.joinToString("; ") { "${it.key}=${it.value}" }
}
