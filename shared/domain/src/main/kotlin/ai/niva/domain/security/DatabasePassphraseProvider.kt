package ai.niva.domain.security

/**
 * Supplies the SQLCipher database passphrase. Keys must originate from the platform keystore layer,
 * never from hardcoded values or ordinary preferences.
 */
interface DatabasePassphraseProvider {
    fun getOrCreatePassphrase(): ByteArray
    fun clearPassphraseMaterial()
}
