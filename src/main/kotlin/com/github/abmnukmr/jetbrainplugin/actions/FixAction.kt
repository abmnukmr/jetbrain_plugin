package com.github.abmnukmr.jetbrainplugin.actions

import com.github.abmnukmr.jetbrainplugin.ui.InputPopup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.ui.awt.RelativePoint

class FixAction: AnAction()  {
    override fun actionPerformed(e: AnActionEvent) {
        println("Action===========: $e")
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val caretOffset = editor.caretModel.offset
        val logicalPosition = editor.offsetToLogicalPosition(caretOffset)
        val point = editor.logicalPositionToXY(logicalPosition).apply {
            translate(0, editor.lineHeight) // show below caret
        }

        val relativePoint = RelativePoint(editor.contentComponent, point)
        InputPopup().showInputPopup(editor, relativePoint, project)
    }
}
