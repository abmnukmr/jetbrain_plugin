package com.github.abmnukmr.jetbrainplugin.startup

import com.github.abmnukmr.jetbrainplugin.intellisence.GhostTextManager
import com.github.abmnukmr.jetbrainplugin.listener.SelectionGutterListener
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.TextRange

class GhostTextActionActivity : ProjectActivity {
    override suspend fun execute(project: Project) {

        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val editor = event.editor
                val project = editor.project ?: return   // project may be null if editor not tied to project

                val ghostText = GhostTextManager(editor) // pass editor, not project
                val document = editor.document
                val caretOffset = editor.caretModel.offset
                val lineNumber = document.getLineNumber(caretOffset)
                val lineEnd = document.getLineEndOffset(lineNumber)

// text to the right of caret until end of line
                val textRight = document.getText(TextRange(caretOffset, lineEnd))

                if (textRight.isBlank()) {

                   ghostText.clearAllGhostText();
                    ghostText.showGhostText()
                } else {
                    println("➡ Text to right: '$textRight'")
                }

            }


        }, project)
    }
}