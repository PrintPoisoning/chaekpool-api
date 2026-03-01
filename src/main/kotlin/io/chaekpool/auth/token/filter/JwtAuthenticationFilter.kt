package io.chaekpool.auth.token.filter

import io.chaekpool.auth.constant.Auth.BEARER_PREFIX
import io.chaekpool.auth.token.provider.JwtProvider
import io.chaekpool.auth.token.service.BlacklistManager
import io.chaekpool.common.exception.ErrorCodeAccessDeniedException
import io.chaekpool.common.exception.ErrorCodeBadCredentialsException
import io.chaekpool.common.exception.internal.ForbiddenException
import io.chaekpool.common.exception.internal.UnauthorizedException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val blacklistManager: BlacklistManager,
    private val jwtProvider: JwtProvider,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
    private val accessDeniedHandler: AccessDeniedHandler
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null) {
            try {
                setAuthentication(request, token)
            } catch (e: UnauthorizedException) {
                authenticationEntryPoint.commence(request, response, ErrorCodeBadCredentialsException(e, request))
                return
            } catch (e: ForbiddenException) {
                accessDeniedHandler.handle(request, response, ErrorCodeAccessDeniedException(e, request))
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader(AUTHORIZATION)

        return if (!bearer.isNullOrBlank() && bearer.startsWith(BEARER_PREFIX)) {
            bearer.removePrefix(BEARER_PREFIX)
        } else {
            null
        }
    }

    private fun setAuthentication(request: HttpServletRequest, token: String) {
        blacklistManager.assertToken(token)

        val userId = jwtProvider.getUserId(token)
        val authentication = UsernamePasswordAuthenticationToken(
            userId,
            null,
            emptyList()
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }
}
