package io.chaekpool.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer

typealias AuthRegistry = AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry

@Configuration
class AuthorizationRulesConfig {

    fun configure(auth: AuthRegistry) {
        auth
            .requestMatchers(
                "/api/v1/common/healthy",
                "/api/v1/auth/oauth2/*/authorize",
                "/api/v1/auth/oauth2/*/callback",
                "/api/v1/auth/token/refresh",
                "/api/v1/auth/dev/**",
                "/actuator/**",
                "/robots.txt",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            .anyRequest().authenticated()
    }
}
