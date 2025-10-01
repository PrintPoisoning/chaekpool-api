package io.chaekpool.common.config

import java.net.InetAddress
import io.chaekpool.common.util.SnowflakeIdGenerator
import io.chaekpool.common.util.SnowflakeSpec
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SnowflakeConfig(
    private val props: SnowflakeProperties
) {

    @Bean
    fun snowflakeIdGenerator(): SnowflakeIdGenerator {
        val spec = SnowflakeSpec(
            workerBits = props.workerBits,
            datacenterBits = props.datacenterBits,
            sequenceBits = props.sequenceBits,
            epoch = props.epoch
        )
        val workerId = resolveWorkerId()
        val datacenterId = resolveDatacenterId()

        return SnowflakeIdGenerator(datacenterId, workerId, spec)
    }

    private fun resolveWorkerId(): Long {
        val hostname = try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            return 0L
        }

        return hostname?.substringAfterLast("-", "")?.toLongOrNull() ?: 0L
    }

    private fun resolveDatacenterId(): Long {
        return System.getenv("DATACENTER_ID")?.toLongOrNull() ?: 0L
    }
}
