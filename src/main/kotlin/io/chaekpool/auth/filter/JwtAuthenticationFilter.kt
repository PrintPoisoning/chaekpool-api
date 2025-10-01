package io.chaekpool.auth.filter

import io.chaekpool.auth.token.service.BlacklistManager
import io.chaekpool.auth.token.service.JwtProvider
import io.chaekpool.common.logger.LoggerDelegate
import io.chaekpool.common.util.isTrueOrForbidden
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
    private val blacklistManager: BlacklistManager,
    private val jwtProvider: JwtProvider
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
        if (!jwtProvider.validateToken(token)) {
            return
        }

        val userId = jwtProvider.getUserId(token)
        val isNotBlacklisted = !blacklistManager.isBlacklisted(userId, token)

        isNotBlacklisted.isTrueOrForbidden("Access token is blacklisted")

        val authentication = UsernamePasswordAuthenticationToken(
            userId,
            null,
            emptyList() // 권한 정보가 필요하다면 UserDetailsService 등과 연동
        )

        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

        SecurityContextHolder.getContext().authentication = authentication
    }
}
