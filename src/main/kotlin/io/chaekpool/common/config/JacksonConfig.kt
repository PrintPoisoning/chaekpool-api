package io.chaekpool.common.config

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.support.JacksonHandlerInstantiator

@Configuration
class JacksonConfig {

    @Bean
    fun jacksonHandlerInstantiatorCustomizer(
        beanFactory: AutowireCapableBeanFactory
    ): JsonMapperBuilderCustomizer {
        return JsonMapperBuilderCustomizer { builder ->
            builder.handlerInstantiator(JacksonHandlerInstantiator(beanFactory))
        }
    }
}
