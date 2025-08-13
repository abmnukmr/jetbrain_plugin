package com.github.abmnukmr.jetbrainplugin.services
import org.eclipse.jgit.ignore.FastIgnoreRule
import org.eclipse.jgit.ignore.IgnoreNode

class GitIgnoreParser(private val patterns: List<String>) {
    private val rules: List<Regex> = patterns
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map {
            val regexPattern = it.replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".")
            Regex("^$regexPattern$")
        }

    fun isIgnored(path: String): Boolean {
        return rules.any { it.matches(path) }
    }
}
