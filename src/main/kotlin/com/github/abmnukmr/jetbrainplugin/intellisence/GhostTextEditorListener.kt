package com.github.abmnukmr.jetbrainplugin.intellisence

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class GhostTextEditorListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val manager = GhostTextManager(editor)
        editor.putUserData(AcceptGhostTextAction.KEY, manager)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor
        val manager = editor.getUserData(AcceptGhostTextAction.KEY)
        manager?.clearGhostText()
    }
}