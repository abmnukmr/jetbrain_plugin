package com.github.abmnukmr.jetbrainplugin.services

import fi.iki.elonen.NanoHTTPD
import java.io.File

class StaticFileServer(private val rootDir: File, port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val path = session.uri.trimStart('/')
        println("Rootdir: $rootDir")
        val target = File(rootDir, if (path.isEmpty()) "index.html" else path)

        if (target.exists() && target.isFile) {
            return newChunkedResponse(
                Response.Status.OK,
                getMimeTypeForFile(target.name),
                target.inputStream()
            )
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
    }
}
