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
                "/api/v1/auth/oauth/**",
                "/api/v1/auth/token/refresh",
                "/api/v1/monitoring/**",
                "/actuator/**",
                "/robots.txt"
            ).permitAll()
            .anyRequest().authenticated()
    }
}
