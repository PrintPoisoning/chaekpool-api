package io.chaekpool.common.util

import io.chaekpool.common.exception.internal.BadRequestException
import io.chaekpool.common.exception.internal.ForbiddenException
import io.chaekpool.common.exception.internal.NotFoundException

// Assert by Boolean
inline fun Boolean.isTrueOrThrow(exception: () -> Throwable) {
    if (!this) {
        throw exception()
    }
}

fun Boolean.isTrueOrBadRequest(message: String? = null) {
    if (!this) {
        throw message?.let { BadRequestException(it) } ?: BadRequestException()
    }
}

fun Boolean.isTrueOrForbidden(message: String? = null) {
    if (!this) {
        throw message?.let { ForbiddenException(it) } ?: ForbiddenException()
    }
}

fun Boolean.isTrueOrNotFound(message: String? = null) {
    if (!this) {
        throw message?.let { NotFoundException(it) } ?: NotFoundException()
    }
}

// Assert by null
inline fun <T> T?.notNullOrThrow(exception: () -> Throwable): T {
    return this ?: throw exception()
}

fun <T> T?.notNullOrBadRequest(message: String? = null): T {
    return this ?: throw message?.let { BadRequestException(it) } ?: BadRequestException()
}

fun <T> T?.notNullOrNotFound(message: String? = null): T {
    return this ?: throw message?.let { NotFoundException(it) } ?: NotFoundException()
}


// Assert String
inline fun String?.hasTextOrThrow(exception: () -> Throwable): String {
    if (this.isNullOrBlank()) throw exception()

    return this
}

fun String?.hasTextOrBadRequest(message: String? = null): String {
    if (this.isNullOrBlank()) {
        throw message?.let { BadRequestException(it) } ?: BadRequestException()
    }

    return this
}

// Assert by size
inline fun <T, C : Collection<T>> C?.notEmptyOrThrow(exception: () -> Throwable): C {
    if (this.isNullOrEmpty()) {
        throw exception()
    }

    return this
}

fun <T, C : Collection<T>> C?.notEmptyOrNotFound(message: String? = null): C {
    if (this.isNullOrEmpty()) {
        throw message?.let { NotFoundException(it) } ?: NotFoundException()
    }

    return this
}

fun <T, C : Collection<T>> C?.hasSizeOrBadRequest(expected: Int, message: String? = null): C {
    if (this == null || this.size != expected) {
        throw message?.let { BadRequestException(it) } ?: BadRequestException()
    }

    return this
}

// Assert by iterator
inline fun <T> T.requireOrThrow(predicate: (T) -> Boolean, exception: () -> Throwable): T {
    if (!predicate(this)) throw exception()

    return this
}
