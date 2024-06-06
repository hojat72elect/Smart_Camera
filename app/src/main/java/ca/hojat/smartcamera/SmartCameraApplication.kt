package ca.hojat.smartcamera

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class SmartCameraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // plant Timber debugger
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Start Koin for dependency injection
        startKoin {
            androidLogger()
            androidContext(this@SmartCameraApplication)
            modules(AppDi.appModule)
        }

    }
}