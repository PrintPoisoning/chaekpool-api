package io.chaekpool.common.provider

import io.chaekpool.common.config.CryptoProperties
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.keygen.KeyGenerators
import org.springframework.stereotype.Component

@Component
class CryptoProvider(cryptoProperties: CryptoProperties) {

    private val secret: String = cryptoProperties.secretKey

    fun encrypt(plainText: String): String {
        val salt = KeyGenerators.string().generateKey()
        val encryptor = Encryptors.text(secret, salt)
        val encrypted = encryptor.encrypt(plainText)
        return "$PREFIX$salt$SEPARATOR$encrypted$SUFFIX"
    }

    fun decrypt(encryptedText: String): String {
        if (!encryptedText.startsWith(PREFIX)) return encryptedText

        val content = encryptedText.removePrefix(PREFIX).removeSuffix(SUFFIX)
        val parts = content.split(SEPARATOR, limit = 2)
        require(parts.size == 2) { "Invalid encrypted format" }

        val (salt, encrypted) = parts
        val encryptor = Encryptors.text(secret, salt)
        return encryptor.decrypt(encrypted)
    }

    companion object {
        private const val PREFIX = "ENC("
        private const val SUFFIX = ")"
        private const val SEPARATOR = ":"
    }
}
