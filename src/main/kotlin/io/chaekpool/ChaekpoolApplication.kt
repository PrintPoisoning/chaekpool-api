package io.chaekpool

import io.chaekpool.common.config.CryptoProperties
import io.chaekpool.common.util.CryptoUtil
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@SpringBootApplication
@ConfigurationPropertiesScan
class ChaekpoolApplication

@Configuration
class CryptoConfig(
    private val cryptoProperties: CryptoProperties
) {
    @PostConstruct
    fun initCrypto() {
        CryptoUtil.init(cryptoProperties.secretKey)
    }
}

fun main(args: Array<String>) {
    runApplication<ChaekpoolApplication>(*args)
}
