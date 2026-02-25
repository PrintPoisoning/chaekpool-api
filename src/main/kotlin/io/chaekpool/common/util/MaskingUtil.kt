package io.chaekpool.common.util

object MaskingUtil {

    private val IPV4_REGEX = Regex(
        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    )

    fun String.maskIpLastOctets(octetsToMask: Int): String {
        if (!IPV4_REGEX.matches(this)) return this

        require(octetsToMask in 1..3) { "octetsToMask must be between 1 and 3, but got $octetsToMask" }

        val parts = this.split(".")

        return parts.take(4 - octetsToMask)
            .plus(List(octetsToMask) { "xxx" })
            .joinToString(".")
    }
}
