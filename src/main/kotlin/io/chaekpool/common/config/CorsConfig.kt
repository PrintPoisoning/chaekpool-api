package io.chaekpool.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurationSource(corsProps: CorsProperties): CorsConfigurationSource {
        val (patterns, origins) = corsProps.allowedOrigins.partition { it.contains("*") }

        val configuration = CorsConfiguration().apply {
            allowedOrigins = origins
            allowedOriginPatterns = patterns
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
