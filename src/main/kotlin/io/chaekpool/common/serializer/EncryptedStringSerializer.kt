package io.chaekpool.common.serializer

import io.chaekpool.common.util.CryptoUtil
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer

class EncryptedStringSerializer : ValueSerializer<String>() {
    override fun serialize(value: String, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeString(CryptoUtil.encrypt(value))
    }
}

class EncryptedStringDeserializer : ValueDeserializer<String>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
        return CryptoUtil.decrypt(p.getString())
    }
}
