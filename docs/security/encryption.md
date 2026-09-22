# Niva — local encryption (Sprint 2)

## Database

- **Engine:** SQLCipher via `net.zetetic:sqlcipher-android`
- **Integration:** Room `openHelperFactory(SupportFactory(passphrase))`
- **Key material:** 256-bit random passphrase in `EncryptedSharedPreferences`, wrapped with **Android Keystore** (`MasterKey` AES256-GCM)
- **Migration:** Plaintext `niva.db` from Sprint 1 is read once, deleted, and restored into the encrypted database.

## What is not logged

Passphrases, message bodies, and key material must never appear in production logs.

## User controls

Privacy screen: clear conversations, delete all local AI data (models kept unless explicitly extended).
