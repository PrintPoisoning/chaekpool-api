package io.chaekpool.common.util

// Assert by Boolean
inline fun Boolean.isTrueOrThrow(exception: () -> Throwable) {
    if (!this) {
        throw exception()
    }
}

// Assert by null
inline fun <T> T?.notNullOrThrow(exception: () -> Throwable): T {
    return this ?: throw exception()
}


// Assert String
inline fun String?.hasTextOrThrow(exception: () -> Throwable): String {
    if (this.isNullOrBlank()) throw exception()

    return this
}

// Assert by size
inline fun <T, C : Collection<T>> C?.notEmptyOrThrow(exception: () -> Throwable): C {
    if (this.isNullOrEmpty()) {
        throw exception()
    }

    return this
}

// Assert by iterator
inline fun <T> T.requireOrThrow(predicate: (T) -> Boolean, exception: () -> Throwable): T {
    if (!predicate(this)) throw exception()

    return this
}
