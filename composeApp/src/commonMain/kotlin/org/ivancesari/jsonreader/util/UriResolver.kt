package org.ivancesari.jsonreader.util

import org.ivancesari.jsonreader.model.JsonFileInfo

expect fun resolveJsonFileInfo(path: String): JsonFileInfo?
