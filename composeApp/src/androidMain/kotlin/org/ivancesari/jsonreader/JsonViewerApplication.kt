package org.ivancesari.jsonreader

import android.app.Application
import org.ivancesari.jsonreader.util.ContextProvider

/**
 * Installs the application context before any Activity is created.
 *
 * The context is needed by the file utilities and by the DataStore factory, both of which can run
 * before (or without) a visible Activity — for example when the process is recreated in the
 * background. Holding the Application here rather than an Activity also avoids leaking the
 * Activity across configuration changes.
 */
class JsonViewerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.install(this)
    }
}
