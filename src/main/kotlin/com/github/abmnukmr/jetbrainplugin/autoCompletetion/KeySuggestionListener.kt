package com.github.abmnukmr.jetbrainplugin.autoCompletetion

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class KeySuggestionListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor

        // Attach a KeyListener directly to the editor's component
        editor.contentComponent.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                // Print caret position while typing
                val offset = editor.caretModel.offset
                println("Caret offset: $offset, Key: ${e.keyChar}")
                Messages.showInfoMessage("This is an info alert", "Information")

                // Example: trigger completion on "."
                if (e.keyChar == '.') {
                    triggerCompletion(editor)
                }
            }
        })
    }

    private fun triggerCompletion(editor: com.intellij.openapi.editor.Editor) {
        val project = editor.project ?: return
        ApplicationManager.getApplication().invokeLater {
            CodeCompletionHandlerBase(CompletionType.BASIC)
                .invokeCompletion(project, editor, 1)
        }
    }

}
