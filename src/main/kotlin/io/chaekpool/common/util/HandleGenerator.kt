package io.chaekpool.common.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.SecureRandom

object HandleGenerator {

    private const val PREFIX = "user_"
    private const val HANDLE_LENGTH = 8
    private const val CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789"
    private const val DEFAULT_MAX_RETRY = 3

    private val log = KotlinLogging.logger {}
    private val random = SecureRandom()

    fun generate(): String {
        val suffix = StringBuilder(HANDLE_LENGTH)
        repeat(HANDLE_LENGTH) {
            suffix.append(CHARACTERS[random.nextInt(CHARACTERS.length)])
        }
        return "$PREFIX$suffix"
    }

    fun generateUnique(
        maxRetry: Int = DEFAULT_MAX_RETRY,
        existsByHandle: (String) -> Boolean
    ): String {
        repeat(maxRetry) {
            val handle = generate()
            if (!existsByHandle(handle)) return handle
            log.warn { "Handle 충돌 발생, 재시도 중: attempt=${it + 1}" }
        }
        throw IllegalStateException("고유 handle 생성 실패: ${maxRetry}회 재시도 초과")
    }
}
