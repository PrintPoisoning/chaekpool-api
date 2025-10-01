package io.chaekpool.common.logger

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggerDelegate : ReadOnlyProperty<Any?, Logger> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        return LoggerFactory.getLogger(thisRef?.javaClass)
    }

}
