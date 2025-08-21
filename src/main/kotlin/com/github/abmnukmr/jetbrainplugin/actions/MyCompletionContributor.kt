package com.github.abmnukmr.jetbrainplugin.actions


import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class MyCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),  // you can narrow down by language/element
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    // Add static suggestion
                    resultSet.addElement(LookupElementBuilder.create("printHello()"))

                    // You could call your AI backend here and stream results
                    // Example: resultSet.addElement(LookupElementBuilder.create(aiSuggestion))
                }
            }
        )
    }
}
