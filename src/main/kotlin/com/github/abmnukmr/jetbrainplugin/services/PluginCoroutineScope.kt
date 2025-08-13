package com.github.abmnukmr.jetbrainplugin.services

import kotlinx.coroutines.*

object PluginCoroutineScope {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    var sseJob: Job? = null
}