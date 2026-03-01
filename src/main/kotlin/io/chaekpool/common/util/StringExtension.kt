package io.chaekpool.common.util

fun String.truncate(maxLength: Int): String =
    if (length <= maxLength)
        this
    else
        "${substring(0, maxLength)}...(truncated)"
