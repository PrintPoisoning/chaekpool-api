package io.chaekpool.common.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.chaekpool.common.util.CryptoUtil

class EncryptedStringSerializer : JsonSerializer<String>() {
    override fun serialize(value: String, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(CryptoUtil.encrypt(value))
    }
}

class EncryptedStringDeserializer : com.fasterxml.jackson.databind.JsonDeserializer<String>() {
    override fun deserialize(p: com.fasterxml.jackson.core.JsonParser, ctxt: com.fasterxml.jackson.databind.DeserializationContext): String {
        return CryptoUtil.decrypt(p.text)
    }
}
