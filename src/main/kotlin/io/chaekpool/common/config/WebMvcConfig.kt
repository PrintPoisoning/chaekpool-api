package io.chaekpool.common.config

import io.chaekpool.auth.annotation.AccessTokenAnnotationResolver
import io.chaekpool.auth.annotation.RefreshTokenAnnotationResolver
import io.chaekpool.auth.annotation.UserIdAnnotationResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val userIdAnnotationResolver: UserIdAnnotationResolver,
    private val accessTokenAnnotationResolver: AccessTokenAnnotationResolver,
    private val refreshTokenAnnotationResolver: RefreshTokenAnnotationResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        with(resolvers) {
            add(userIdAnnotationResolver)
            add(accessTokenAnnotationResolver)
            add(refreshTokenAnnotationResolver)
        }
    }
}
