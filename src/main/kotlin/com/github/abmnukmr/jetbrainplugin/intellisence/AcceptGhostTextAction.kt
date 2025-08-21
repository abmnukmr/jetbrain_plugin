package com.github.abmnukmr.jetbrainplugin.intellisence

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.util.Key

class AcceptGhostTextAction : EditorAction(AcceptHandler()) {

    class AcceptHandler : EditorActionHandler() {
        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
            val manager = editor.getUserData(KEY)

            if (manager?.hasGhostText() == true) {
                manager.acceptGhostText()
                return
            }

            // Fallback → normal Tab
            val defaultTabHandler = EditorActionManager.getInstance()
                .getActionHandler(IdeActions.ACTION_EDITOR_TAB)

            defaultTabHandler.execute(editor, caret, dataContext)
        }
    }

    companion object {
        val KEY: Key<GhostTextManager> = Key.create("ghost.text.manager")
    }
}
