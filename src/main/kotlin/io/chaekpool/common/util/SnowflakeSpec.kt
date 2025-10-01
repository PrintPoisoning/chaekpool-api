package io.chaekpool.common.util

data class SnowflakeSpec(
    val workerBits: Int,
    val datacenterBits: Int,
    val sequenceBits: Int,
    val epoch: Long
) {

    val maxWorkerId: Long get() = (1L shl workerBits) - 1
    val maxDatacenterId: Long get() = (1L shl datacenterBits) - 1
    val sequenceMask: Long get() = (1L shl sequenceBits) - 1

    val workerShift: Int get() = sequenceBits
    val datacenterShift: Int get() = sequenceBits + workerBits
    val timestampShift: Int get() = sequenceBits + workerBits + datacenterBits
}
