package com.github.abmnukmr.jetbrainplugin.listener

import com.github.abmnukmr.jetbrainplugin.ui.InputPopup
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.Project
import com.intellij.ui.awt.RelativePoint
import javax.swing.*
import com.intellij.openapi.diagnostic.Logger


class SelectionGutterListener(private val project: Project) : SelectionListener {
    companion object {
        private val log = Logger.getInstance(SelectionGutterListener::class.java)
    }

    private var highlighter: RangeHighlighter? = null


    override fun selectionChanged(e: SelectionEvent) {
        val editor = e.editor
        if (editor.project != project) return

        highlighter?.dispose()
        highlighter = null

        val range = e.newRange
        if (range.isEmpty) return
        val startOffset = range.startOffset
        val markupModel = editor.markupModel

        highlighter = markupModel.addRangeHighlighter(
            startOffset,
            startOffset,
            HighlighterLayer.WARNING,
            null,
            HighlighterTargetArea.LINES_IN_RANGE
        )

        highlighter?.gutterIconRenderer = object : GutterIconRenderer() {
            override fun getIcon(): Icon {
                return AllIcons.Gutter.WriteAccess // 💡 classic lightbulb icon
            }

            override fun getTooltipText(): String = "Click to suggest"
            override fun isNavigateAction(): Boolean = true

            override fun getClickAction(): AnAction? {
                return object : AnAction() {
                    override fun actionPerformed(e: AnActionEvent) {
                        val logicalPosition = editor.offsetToLogicalPosition(startOffset)
                        val point = editor.logicalPositionToXY(logicalPosition).apply {
                            translate(0, editor.lineHeight)
                        }
                        val relativePoint = RelativePoint(editor.contentComponent, point)
                        InputPopup().showInputPopup(editor, relativePoint, project)
                    }
                }
            }


            override fun equals(other: Any?) = false
            override fun hashCode(): Int = icon.hashCode()
        }
    }


}
