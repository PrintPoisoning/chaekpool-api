package io.chaekpool.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.postgresql.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer =
        PostgreSQLContainer("postgres:18.2-alpine3.23")
            .withReuse(true)

    @Bean
    @ServiceConnection(name = "redis")
    fun valkey(): GenericContainer<*> =
        GenericContainer("valkey/valkey:9.0.2-alpine")
            .withExposedPorts(6379)
            .withReuse(true)
}
