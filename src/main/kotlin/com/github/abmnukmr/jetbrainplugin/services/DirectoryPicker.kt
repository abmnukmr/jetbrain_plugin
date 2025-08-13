package com.github.abmnukmr.jetbrainplugin.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import org.cef.browser.CefBrowser
import org.json.JSONObject
import java.io.File

class DirectoryPicker {
    fun createDirectoryPickerPanel(project: Project, cefBrowser: CefBrowser) {
        ApplicationManager.getApplication().invokeLater {
            val rootDir = project.guessProjectDir()

            val descriptor = FileChooserDescriptor(
                true,  // chooseFiles
                true,  // chooseFolders
                false, // chooseJars
                false, // chooseJarsAsFiles
                false, // chooseJarContents
                true   // allowMultiple
            ).withRoots(rootDir)
                .withShowHiddenFiles(false)
                .withTitle("Select file(s) or a folder")

            FileChooser.chooseFiles(descriptor, project, rootDir) { selectedFiles: List<VirtualFile> ->
                if (selectedFiles.isEmpty()) return@chooseFiles

                val allPaths = mutableListOf<String>()
                fun recurse(file: VirtualFile) {
                    if (!file.isDirectory) {
                        allPaths.add(file.path)
                    } else {
                        file.children.forEach { recurse(it) }
                    }
                }

                selectedFiles.forEach {
                    if (rootDir != null && VfsUtilCore.isAncestor(rootDir, it, false)) {
                        recurse(it)
                    }
                }

                val quotedPaths = allPaths.map { JSONObject.quote(it) }.joinToString(", ")
                val rootPath = JSONObject.quote(rootDir?.path ?: "")

                val js = """
                    window.postMessage({
                        command: "directorySelected",
                        files: [ $quotedPaths ],
                        rootPath: $rootPath
                    }, "*");
                """.trimIndent()

                cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
            }
        }
    }



    fun getFilesNotIgnored(project: Project, scanDir: File? = null, cefBrowser: CefBrowser) {
        val basePath = project.basePath ?: return
        val projectDir = File(basePath)
        val directoryToScan = scanDir ?: projectDir

        val gitignoreFile = File(projectDir, ".gitignore")
        val parser = if (gitignoreFile.exists()) {
            GitIgnoreParser(gitignoreFile.readLines())
        } else {
            GitIgnoreParser(emptyList())
        }

        val allPaths = directoryToScan.walkTopDown()
            .filter { it.isFile }
            .filterNot { parser.isIgnored(it.relativeTo(projectDir).path) }
            .map { it.absolutePath }
            .toList()

        val quotedPaths = allPaths.map { JSONObject.quote(it) }.joinToString(", ")
        val rootPath = JSONObject.quote(basePath ?: "")

        println("[ $quotedPaths ]");

        val js = """
            window.postMessage({
                command: "initiallyDirectorySelected",
                files: [ $quotedPaths ],
                rootPath: $rootPath
            }, "*");
            """.trimIndent()

        cefBrowser.executeJavaScript(js, cefBrowser.url, 0)
    }

}
