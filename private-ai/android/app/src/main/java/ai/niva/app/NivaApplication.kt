package ai.niva.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
@HiltAndroidApp
class NivaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
    }
}
