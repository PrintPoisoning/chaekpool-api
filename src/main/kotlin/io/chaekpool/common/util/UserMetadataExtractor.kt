package io.chaekpool.common.util

import io.chaekpool.common.dto.UserMetadata
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import ua_parser.Parser

@Component
class UserMetadataExtractor {

    private val parser = Parser()

    fun extract(request: HttpServletRequest): UserMetadata {
        val ip = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
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
}
