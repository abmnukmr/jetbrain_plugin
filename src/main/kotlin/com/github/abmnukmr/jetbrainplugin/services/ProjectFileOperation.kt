package com.github.abmnukmr.jetbrainplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class ProjectFileOperation {
    fun getAllProjectFilePaths(project: Project): List<String> {
        val basePath = project.basePath ?: return emptyList()
        val root = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return emptyList()
        val paths = mutableListOf<String>()
        collectPathsRecursively(root, paths, basePath)
        return paths
    }

    private fun collectPathsRecursively(file: VirtualFile, paths: MutableList<String>, basePath: String) {
        if (!file.isDirectory) {
            paths.add(file.path.removePrefix("$basePath/"))
        } else {
            file.children.forEach { child ->
                collectPathsRecursively(child, paths, basePath)
            }
        }
    }

    fun getAllGitIncludedFilePaths(project: Project): List<String> {
        val basePath = project.basePath ?: return emptyList()

        fun runGitCommand(vararg args: String): List<String> {
            return try {
                val process = ProcessBuilder("git", *args)
                    .directory(File(basePath))
                    .redirectErrorStream(true)
                    .start()

                process.inputStream.bufferedReader().readLines()
            } catch (e: Exception) {
                emptyList()
            }
        }

        // Tracked files
        val trackedFiles = runGitCommand("ls-files")

        // Untracked but unignored files
        val untrackedFiles = runGitCommand("ls-files", "--others", "--exclude-standard")

        // Combine both
        return (trackedFiles + untrackedFiles).map { "$basePath/$it" }
    }
}