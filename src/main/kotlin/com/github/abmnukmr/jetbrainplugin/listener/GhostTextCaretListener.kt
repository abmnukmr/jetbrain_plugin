package com.github.abmnukmr.jetbrainplugin.listener

import com.github.abmnukmr.jetbrainplugin.intellisence.GhostTextManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener

class GhostTextCaretListener(
    private val editor: Editor
) : CaretListener {

    override fun caretPositionChanged(event: CaretEvent) {
        updateGhostText(editor)
    }

    private fun updateGhostText(editor: Editor) {
        val caret = editor.caretModel.currentCaret
        val offset = caret.offset
        val doc = editor.document
        val text = doc.charsSequence

        // if caret is not at the end of doc, check what's next
        val hasCharRight = offset < text.length && !text[offset].isWhitespace()

        val manager = GhostTextManager(editor)


        if (!hasCharRight) {
            manager.showGhostText("Press Tab to accept suggestion")
        } else {
            manager.clearGhostText()
        }
    }
}
