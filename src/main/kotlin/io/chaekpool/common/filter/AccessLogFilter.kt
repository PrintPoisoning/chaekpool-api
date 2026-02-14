package io.chaekpool.common.filter

import io.chaekpool.common.util.UserMetadataExtractor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.time.measureTime

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class AccessLogFilter(
    private val userMetadataExtractor: UserMetadataExtractor,
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    private val staticExtensions = setOf(".ico", ".css", ".js", ".png", ".jpg", ".svg", ".woff", ".woff2")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val elapsed = measureTime { filterChain.doFilter(request, response) }
        val uri = request.queryString?.let { "${request.requestURI}?$it" } ?: request.requestURI
        val metadata = userMetadataExtractor.extract(request)

        log.info {
            "[ACCESS] ${request.method} $uri ${response.status} ${elapsed.inWholeMilliseconds}ms" +
                    " ${metadata.ip} ${metadata.platformType} ${metadata.userAgent}"
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/actuator") || staticExtensions.any { uri.endsWith(it) }
    }
}
