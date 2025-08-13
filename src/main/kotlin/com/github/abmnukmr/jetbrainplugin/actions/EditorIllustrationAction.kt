package com.github.abmnukmr.jetbrainplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class EditorIllustrationAction : AnAction(){
    override fun actionPerformed(event: AnActionEvent) {
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document

        val caret = editor.caretModel.primaryCaret
        val start = caret.selectionStart
        val end = caret.selectionEnd

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, "editor_basics")
        }

        caret.removeSelection()
    }
}