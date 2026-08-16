package org.ivancesari.jsonreader.util

import android.content.Context

/**
 * Holds the application context for the platform file utilities.
 *
 * Only ever stores an application context — storing an Activity here would keep it alive for the
 * lifetime of the process. [install] is called from [org.ivancesari.jsonreader.JsonViewerApplication].
 */
object ContextProvider {

    private var applicationContext: Context? = null

    fun install(context: Context) {
        applicationContext = context.applicationContext
    }

    /** The application context, or null if the process has not finished starting up. */
    val context: Context?
        get() = applicationContext

    /**
     * The application context, or throws when it is missing.
     *
     * A missing context means [JsonViewerApplication] was not registered in the manifest, which is
     * a wiring mistake rather than a runtime condition worth recovering from.
     */
    fun requireContext(): Context = checkNotNull(applicationContext) {
        "ContextProvider was not installed. Is JsonViewerApplication declared in AndroidManifest.xml?"
    }
}
