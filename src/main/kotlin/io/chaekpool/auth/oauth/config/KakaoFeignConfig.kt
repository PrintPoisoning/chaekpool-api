package io.chaekpool.auth.oauth.config

import feign.Logger
import feign.Request
import feign.codec.ErrorDecoder
import io.chaekpool.common.logger.FeignErrorDecoder
import io.chaekpool.common.logger.SingleLineFeignLogger
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableFeignClients(basePackages = ["io.chaekpool.auth.client.kakao"])
class KakaoFeignConfig {

    @Bean
    fun feignLogger(): Logger = SingleLineFeignLogger()

    @Bean
    fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL

    @Bean
    fun options(): Request.Options =
        Request.Options(
            Duration.ofMillis(5_000),   // connectTimeout
            Duration.ofMillis(10_000),  // readTimeout
            true
        )

    @Bean
    fun feignRetryer(): feign.Retryer = feign.Retryer.Default(100, 1000, 3)

    @Bean
    fun kakaoErrorDecoder(): ErrorDecoder = FeignErrorDecoder()
}
