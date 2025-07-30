package com.github.abmnukmr.jetbrainplugin.services

import kotlinx.coroutines.*

object PluginCoroutineScope {
    private val handler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
    }

    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
}