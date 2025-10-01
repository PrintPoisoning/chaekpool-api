package io.chaekpool.common.util

import java.util.concurrent.atomic.AtomicLong

class SnowflakeIdGenerator(
    private val datacenterId: Long,
    private val workerId: Long,
    private val spec: SnowflakeSpec
) : IdGenerator {

    private val state = AtomicLong(-1L shl spec.sequenceBits)

    init {
        require(workerId in 0..spec.maxWorkerId) { "Invalid workerId $workerId" }
        require(datacenterId in 0..spec.maxDatacenterId) { "Invalid datacenterId $datacenterId" }
    }

    override fun nextId(): Long = generateId()

    private tailrec fun generateId(): Long {
        val old = state.get()
        val lastTs = old ushr spec.sequenceBits
        val seq = old and spec.sequenceMask
        var ts = System.currentTimeMillis()

        if (ts < lastTs) {
            ts = lastTs
        }

        val nextSeq = if (ts == lastTs) {
            (seq + 1) and spec.sequenceMask
        } else {
            0L
        }

        val newState = (ts shl spec.sequenceBits) or nextSeq

        if (!state.compareAndSet(old, newState)) {
            return generateId()
        }

        return ((ts - spec.epoch) shl spec.timestampShift) or
                (datacenterId shl spec.datacenterShift) or
                (workerId shl spec.workerShift) or
                nextSeq
    }
}
