package io.chaekpool.common.filter

import io.chaekpool.common.util.UserMetadataExtractor
import io.chaekpool.common.util.truncate
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import kotlin.time.measureTime

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class AccessLogFilter(
    private val userMetadataExtractor: UserMetadataExtractor,
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    private val staticExtensions = setOf(".ico", ".css", ".js", ".png", ".jpg", ".svg", ".woff", ".woff2")

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/actuator") || staticExtensions.any { uri.endsWith(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.queryString?.let { "${request.requestURI}?$it" } ?: request.requestURI
        val metadata = userMetadataExtractor.extract(request)

        val cachedRequest = getCachedRequest(request)
        val cachedResponse = getCachedRequest(response)

        log.info { "[HTTP_IN] method=${request.method} uri=$uri ip=${metadata.ip} platform=${metadata.platformType}" }

        val elapsed = measureTime { filterChain.doFilter(cachedRequest, cachedResponse) }

        if (log.isDebugEnabled()) {
            logRequestBody(cachedRequest)
            logResponseBody(cachedResponse)
        }

        log.info { "[HTTP_OUT] method=${request.method} uri=$uri status=${response.status} elapsed=${elapsed.inWholeMilliseconds}ms" }
    }

    private fun getCachedRequest(request: HttpServletRequest): HttpServletRequest {
        val isNotMultipart = request.contentType?.startsWith("multipart/") == false

        return if (log.isDebugEnabled() && isNotMultipart)
            ContentCachingRequestWrapper(request, 4096)
        else
            request
    }

    private fun getCachedRequest(response: HttpServletResponse): HttpServletResponse =
        if (log.isDebugEnabled())
            ContentCachingResponseWrapper(response)
        else
            response

    private fun logRequestBody(request: HttpServletRequest) {
        if (request !is ContentCachingRequestWrapper) return

        val body = request.contentAsByteArray.toString(Charsets.UTF_8).truncate(1000)
        if (body.isNotBlank()) {
            log.debug { "[HTTP_IN_BODY] body=$body" }
        }
    }

    private fun logResponseBody(response: HttpServletResponse) {
        if (response !is ContentCachingResponseWrapper) return

        val body = response.contentAsByteArray.toString(Charsets.UTF_8).truncate(1000)
        if (body.isNotBlank()) {
            log.debug { "[HTTP_OUT_BODY] body=$body" }
        }
        response.copyBodyToResponse()
    }
}
