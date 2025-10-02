package io.chaekpool.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler

@Configuration
class SecurityExceptionConfig {

    @Bean
    fun exceptionRules(
        customAuthenticationEntryPoint: AuthenticationEntryPoint,
        customAccessDeniedHandler: AccessDeniedHandler
    ): Customizer<ExceptionHandlingConfigurer<HttpSecurity>> {
        return Customizer {
            it.authenticationEntryPoint(customAuthenticationEntryPoint)
            it.accessDeniedHandler(customAccessDeniedHandler)
        }
    }
}
