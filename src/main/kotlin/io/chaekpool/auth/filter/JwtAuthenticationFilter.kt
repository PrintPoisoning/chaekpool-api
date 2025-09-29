package io.chaekpool.auth.filter

import io.chaekpool.common.util.LoggerDelegate
import io.chaekpool.token.service.BlacklistManager
import io.chaekpool.token.service.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val blacklistManager: BlacklistManager
) : OncePerRequestFilter() {

    private val log by LoggerDelegate()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        resolveToken(request)?.let { setAuthentication(request, it) }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")

        return if (!bearer.isNullOrBlank() && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else {
            null
        }
    }

    private fun setAuthentication(request: HttpServletRequest, token: String) {
        log.info("setAuthentication token={}", token)

        if (!jwtProvider.validateToken(token)) {
            return
        }

        val userId = jwtProvider.getUserId(token)

        if (blacklistManager.isBlacklisted(userId, token)) {
            throw RuntimeException("Token is blacklisted")
        }

        val authentication = UsernamePasswordAuthenticationToken(
            userId,
            null,
            emptyList() // 권한 정보가 필요하다면 UserDetailsService 등과 연동
        )

        log.info("Extracted userId={}", userId)

        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

        SecurityContextHolder.getContext().authentication = authentication
    }
}
