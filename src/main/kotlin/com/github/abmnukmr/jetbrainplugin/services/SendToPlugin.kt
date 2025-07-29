package com.github.abmnukmr.jetbrainplugin.services
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.json.JSONObject

class SendToPlugin (browser: JBCefBrowser?){
    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
    }
    fun sendMessageToWebView(browser: JBCefBrowser, message: String) {
        val escaped = JSONObject.quote(message) // escape quotes
        val js = "window.__receiveFromPlugin($escaped);"
        browser.cefBrowser.executeJavaScript(js, browser.cefBrowser.url, 0)
    }
}