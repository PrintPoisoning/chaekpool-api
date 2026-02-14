package io.chaekpool.common.config

import io.chaekpool.auth.token.filter.JwtAuthenticationFilter
import io.chaekpool.common.filter.UserMetadataFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val userMetadataFilter: UserMetadataFilter,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authorizationRulesConfig: AuthorizationRulesConfig,
    private val exceptionRules: Customizer<ExceptionHandlingConfigurer<HttpSecurity>>
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return with(http) {
            cors {}
            csrf { it.disable() }
            formLogin { it.disable() }
            requestCache { it.disable() }
            httpBasic { it.disable() }
            rememberMe { it.disable() }
            logout { it.disable() }
            sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            exceptionHandling(exceptionRules)
            authorizeHttpRequests { auth -> authorizationRulesConfig.configure(auth) }
            addFilterBefore(userMetadataFilter, UsernamePasswordAuthenticationFilter::class.java)
            addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            build()
        }
    }
}
