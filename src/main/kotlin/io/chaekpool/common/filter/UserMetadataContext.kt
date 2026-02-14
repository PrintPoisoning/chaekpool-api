package io.chaekpool.common.filter

import io.chaekpool.common.dto.UserMetadata
import org.springframework.stereotype.Component
import kotlin.reflect.KProperty

@Component
class UserMetadataContext {

    private val holder = ThreadLocal<UserMetadata>()

    fun set(metadata: UserMetadata) = holder.set(metadata)

    fun clear() = holder.remove()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): UserMetadata? = holder.get()
}
