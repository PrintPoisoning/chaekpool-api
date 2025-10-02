package io.chaekpool.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer

typealias AuthRegistry = AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry

@Configuration
class AuthorizationRulesConfig {

    @Bean
    fun authorizationRules(): Customizer<AuthRegistry> {
        return Customizer { auth ->
            auth.requestMatchers("/api/v1/common/healthy")
                .permitAll()
                .requestMatchers("/api/v1/auth/oauth/**")
                .permitAll()
                .requestMatchers("/api/v1/auth/token/refresh")
                .permitAll()
                .anyRequest()
                .authenticated()
        }
    }
}
