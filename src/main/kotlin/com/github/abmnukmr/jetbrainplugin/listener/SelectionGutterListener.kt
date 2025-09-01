package com.github.abmnukmr.jetbrainplugin.listener

import com.github.abmnukmr.jetbrainplugin.ui.InputPopup
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.Project
import com.intellij.ui.awt.RelativePoint
import javax.swing.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import kotlinx.html.ButtonType


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
                        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
                        val project = e.project ?: return
                        val logicalPosition = editor.offsetToLogicalPosition(startOffset)
                        val point = editor.logicalPositionToXY(logicalPosition).apply {
                            x = 0
                            translate(0, editor.lineHeight)
                        }
                        val relativePoint = RelativePoint(editor.contentComponent, point)

                        // Create a small action group menu
                        val actionGroup = DefaultActionGroup().apply {
                            add(object : AnAction("Fix", "Apply a quick fix", AllIcons.Actions.QuickfixBulb) {
                                override fun actionPerformed(e: AnActionEvent) {
                                    println("Fix chosen")
                                    InputPopup().showInputPopup(editor, relativePoint, project)
                                }
                            })

                            add(object : AnAction("Review", "Review this code", AllIcons.Actions.ToggleVisibility) {
                                override fun actionPerformed(e: AnActionEvent) {
                                    println("Review chosen")
                                    InputPopup().showInputPopup(editor, relativePoint, project)
                                }
                            })

                            add(object : AnAction("Search", "Search related context", AllIcons.Actions.Search) {
                                override fun actionPerformed(e: AnActionEvent) {
                                    println("Search chosen")
                                    InputPopup().showInputPopup(editor, relativePoint, project)
                                }
                            })
                        }


                        // Calculate popup position near gutter icon

                        // Show popup
                        JBPopupFactory.getInstance()
                            .createActionGroupPopup(
                                "Precommit AI",
                                actionGroup,
                                e.dataContext,
                                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                true
                            )
                            .show(relativePoint)

                    }
                }
            }


            override fun equals(other: Any?) = false
            override fun hashCode(): Int = icon.hashCode()
        }
    }

    enum class ActionType { FIX, REVIEW, SEARCH }

    fun createPanel(): JPanel {
        val panel = JPanel()
        val button = JButton("Precommit AI")

        button.addActionListener {
            val popup = createPopupMenu()
            popup.showUnderneathOf(button) // shows directly under the button
        }

        panel.add(button)
        return panel
    }

    private fun createPopupMenu(): ListPopup {
        val actionGroup = DefaultActionGroup().apply {
            add(object : AnAction("Fix") {
                override fun actionPerformed(e: AnActionEvent) {
                    println("Fix selected")
                }
            })
            add(object : AnAction("Review") {
                override fun actionPerformed(e: AnActionEvent) {
                    println("Review selected")
                }
            })
            add(object : AnAction("Search") {
                override fun actionPerformed(e: AnActionEvent) {
                    println("Search selected")
                }
            })
        }
        val dataContext = DataManager.getInstance().getDataContext()
        return JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Precommit AI",     // title
                actionGroup,
                dataContext,               // data context
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )
    }


}
