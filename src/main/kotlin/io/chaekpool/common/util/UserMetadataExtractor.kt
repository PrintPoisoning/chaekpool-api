package io.chaekpool.common.util

import io.chaekpool.common.dto.UserMetadata
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import ua_parser.Parser

@Component
class UserMetadataExtractor {

    private val parser = Parser()

    fun extract(request: HttpServletRequest): UserMetadata {
        val ip = resolveClientIp(request)
        val uaString = request.getHeader("User-Agent") ?: "UNKNOWN"

        val client = parser.parse(uaString)

        val os = client.os.family?.uppercase()
        val browser = client.userAgent.family?.uppercase()
        val device = client.device.family?.uppercase()

        val platformType = when {
            uaString.contains("Mobi", true) -> "MOBILE"
            uaString.contains("Tablet", true) -> "TABLET"
            uaString.contains("Bot", true) -> "BOT"
            else -> "DESKTOP"
        }

        return UserMetadata(
            ip = ip,
            userAgent = uaString,
            device = device,
            os = os,
            browser = browser,
            platformType = platformType,
            locale = request.locale?.toString(),
            timezone = request.getHeader("Time-Zone")?.uppercase()
        )
    }

    fun resolveClientIp(request: HttpServletRequest): String {
        for (header in IP_HEADERS) {
            val value = request.getHeader(header) ?: continue
            val ip = value.split(",")
                .firstNotNullOfOrNull { it.trim().takeIfValidIp() }
            if (ip != null) return normalizeLoopback(ip)
        }
        return normalizeLoopback(request.remoteAddr)
    }

    companion object {
        private val IP_HEADERS = listOf(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP",
        )

        private const val IPV6_LOOPBACK_FULL = "0:0:0:0:0:0:0:1"
        private const val IPV6_LOOPBACK_SHORT = "::1"
        private const val IPV4_LOOPBACK = "127.0.0.1"

        private fun String.takeIfValidIp(): String? =
            takeIf { it.isNotBlank() && it.uppercase() != "UNKNOWN" }

        private fun normalizeLoopback(ip: String): String =
            if (ip == IPV6_LOOPBACK_FULL || ip == IPV6_LOOPBACK_SHORT) IPV4_LOOPBACK else ip
    }
}
