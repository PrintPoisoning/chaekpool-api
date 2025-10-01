package io.chaekpool.common.filter

import io.chaekpool.common.util.UserMetadataExtractor
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class UserMetadataFilter(
    private val extractor: UserMetadataExtractor,
    private val context: UserMetadataContext
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            context.set(extractor.extract(request))
            
            filterChain.doFilter(request, response)
        } finally {
            context.clear()
        }
    }
}
