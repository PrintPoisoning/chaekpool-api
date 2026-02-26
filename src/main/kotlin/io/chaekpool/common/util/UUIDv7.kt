package io.chaekpool.common.util

import com.fasterxml.uuid.Generators
import java.time.Instant
import java.util.UUID

object UUIDv7 {

    private val generator = Generators.timeBasedEpochGenerator()

    fun generate(): UUID = generator.generate()

    fun extractTimestamp(uuid: UUID?): Instant {
        require(isUuidV7(uuid)) { "UUID version 7 required, but got version ${uuid?.version()}" }

        val epochMillis = uuid!!.mostSignificantBits ushr 16 // 64 bits -> 48 bits
        return Instant.ofEpochMilli(epochMillis)
    }

    fun extractTimestamp(uuidString: String): Instant {
        return extractTimestamp(UUID.fromString(uuidString))
    }

    fun isUuidV7(uuid: UUID?): Boolean {
        return uuid?.version() == 7
    }
}
