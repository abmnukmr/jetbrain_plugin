package com.github.abmnukmr.jetbrainplugin.startup

import com.github.abmnukmr.jetbrainplugin.intellisence.GhostTextManager
import com.github.abmnukmr.jetbrainplugin.listener.SelectionGutterListener
import com.github.abmnukmr.jetbrainplugin.ui.InputPopup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.TextRange
import com.intellij.ui.awt.RelativePoint

class GhostTextActionActivity : ProjectActivity {



    override suspend fun execute(project: Project) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val ghostText = GhostTextManager(editor)



        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val editor = event.editor
                val project = editor.project ?: return   // project may be null if editor not tied to project

                val document = editor.document
                val caretOffset = editor.caretModel.offset
                val lineNumber = document.getLineNumber(caretOffset)
                val lineEnd = document.getLineEndOffset(lineNumber)

                // text to the right of caret until end of line

                val offset = editor.caretModel.offset
                val text = editor.document.charsSequence

                val textRight = document.getText(TextRange(caretOffset, lineEnd))

                val isInMiddleOfWord = offset > 0 &&
                        offset < text.length &&
                        Character.isJavaIdentifierPart(text[offset - 1]) && // left is part of word
                        Character.isJavaIdentifierPart(text[offset])
                if (!isInMiddleOfWord && textRight.isBlank()) {

                   ghostText.clearAllGhostText();
                    ghostText.showGhostText()
                } else {
                    println("➡ Text to right: '$'")
                }

            }


        }, project)
    }
}