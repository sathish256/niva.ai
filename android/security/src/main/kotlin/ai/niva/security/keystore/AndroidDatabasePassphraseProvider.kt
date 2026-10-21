package ai.niva.security.keystore

import ai.niva.domain.security.DatabasePassphraseProvider
import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDatabasePassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : DatabasePassphraseProvider {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getOrCreatePassphrase(): ByteArray {
        val existing = prefs.getString(KEY_PASSPHRASE, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }
        val generated = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(generated, Base64.NO_WRAP))
            .apply()
        return generated
    }

    override fun clearPassphraseMaterial() {
        prefs.edit().remove(KEY_PASSPHRASE).apply()
    }

    companion object {
        private const val PREFS_FILE = "niva_db_key"
        private const val KEY_PASSPHRASE = "sqlcipher_passphrase"
        private const val PASSPHRASE_BYTES = 32
    }
}
