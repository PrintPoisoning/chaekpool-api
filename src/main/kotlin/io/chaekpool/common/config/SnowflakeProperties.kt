package io.chaekpool.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "snowflake")
data class SnowflakeProperties(
    val workerBits: Int = 5,
    val datacenterBits: Int = 5,
    val sequenceBits: Int = 12,
    val epoch: Long = 1735689600000L  // 2025-01-01 UTC
)
