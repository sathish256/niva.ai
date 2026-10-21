package ai.niva.storage

import ai.niva.domain.security.DatabasePassphraseProvider
import ai.niva.storage.local.NivaDatabase
import ai.niva.storage.local.NivaMigrations
import ai.niva.storage.local.PlaintextDatabaseMigrator
import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedNivaDatabaseFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val passphraseProvider: DatabasePassphraseProvider,
) {
    @Volatile
    private var database: NivaDatabase? = null

    fun getDatabase(): NivaDatabase {
        return database ?: synchronized(this) {
            database ?: buildDatabase().also { database = it }
        }
    }

    fun closeAndClearInstance() {
        synchronized(this) {
            database?.close()
            database = null
        }
    }

    private fun buildDatabase(): NivaDatabase {
        val snapshot = PlaintextDatabaseMigrator.readPlaintextIfPresent(context)
        if (snapshot != null) {
            PlaintextDatabaseMigrator.deletePlaintextFiles(context)
        }
        val factory = SupportOpenHelperFactory(passphraseProvider.getOrCreatePassphrase())
        val db = Room.databaseBuilder(
            context,
            NivaDatabase::class.java,
            PlaintextDatabaseMigrator.DATABASE_NAME,
        )
            .openHelperFactory(factory)
            .addMigrations(NivaMigrations.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
        if (snapshot != null) {
            runBlocking { PlaintextDatabaseMigrator.restore(snapshot, db) }
        }
        return db
    }
}
