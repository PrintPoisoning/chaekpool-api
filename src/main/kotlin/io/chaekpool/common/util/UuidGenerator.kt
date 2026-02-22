package io.chaekpool.common.util

import com.fasterxml.uuid.Generators
import java.util.UUID

object UuidGenerator {

    private val v7 = Generators.timeBasedEpochGenerator()

    fun generate(): UUID = v7.generate()
}
