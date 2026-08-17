package org.ivancesari.jsonreader.util

/**
 * Shares the file located at [path], returning false if the share sheet could not be opened.
 *
 * Suspending because platforms may need to stage a copy of the file before handing it to another
 * app; callers should invoke this from a coroutine rather than blocking the UI thread.
 */
expect suspend fun shareFile(path: String): Boolean
