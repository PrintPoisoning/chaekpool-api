package io.chaekpool.common.config

import io.chaekpool.auth.annotation.AccessTokenAnnotationResolver
import io.chaekpool.auth.annotation.AccessUserIdAnnotationResolver
import io.chaekpool.auth.annotation.RefreshTokenAnnotationResolver
import io.chaekpool.auth.annotation.RefreshUserIdAnnotationResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val accessUserIdAnnotationResolver: AccessUserIdAnnotationResolver,
    private val refreshUserIdAnnotationResolver: RefreshUserIdAnnotationResolver,
    private val accessTokenAnnotationResolver: AccessTokenAnnotationResolver,
    private val refreshTokenAnnotationResolver: RefreshTokenAnnotationResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        with(resolvers) {
            add(accessUserIdAnnotationResolver)
            add(accessTokenAnnotationResolver)
            add(refreshTokenAnnotationResolver)
            add(refreshUserIdAnnotationResolver)
        }
    }
}
