package io.chaekpool.auth.filter

import io.chaekpool.auth.exception.ErrorCodeAccessDeniedException
import io.chaekpool.auth.exception.ErrorCodeBadCredentialsException
import io.chaekpool.auth.token.service.BlacklistManager
import io.chaekpool.auth.token.service.JwtProvider
import io.chaekpool.common.exception.internal.ForbiddenException
import io.chaekpool.common.exception.internal.UnauthorizedException
import io.chaekpool.common.logger.LoggerDelegate
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
        try {
            jwtProvider.assertToken(token)

            val userId = jwtProvider.getUserId(token)

            blacklistManager.assertToken(userId, token)

            val authentication = UsernamePasswordAuthenticationToken(
                userId,
                null,
                emptyList()
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: UnauthorizedException) {
            throw ErrorCodeBadCredentialsException(e, request)
        } catch (e: ForbiddenException) {
            throw ErrorCodeAccessDeniedException(e, request)
        }
    }
}
