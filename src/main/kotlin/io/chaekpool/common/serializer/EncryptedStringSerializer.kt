package io.chaekpool.common.serializer

import io.chaekpool.common.provider.CryptoProvider
import org.springframework.stereotype.Component
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer

@Component
class EncryptedStringSerializer(
    private val cryptoProvider: CryptoProvider
) : ValueSerializer<String>() {
    override fun serialize(value: String, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeString(cryptoProvider.encrypt(value))
    }
}

@Component
class EncryptedStringDeserializer(
    private val cryptoProvider: CryptoProvider
) : ValueDeserializer<String>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
        return cryptoProvider.decrypt(p.getString())
    }
}
