package com.github.abmnukmr.jetbrainplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.ui.JBColor
import com.intellij.ui.popup.PopupFactoryImpl
import com.intellij.ui.components.JBLabel
import java.awt.*
import javax.swing.*


class ShowMessageOverTextAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val selectionModel = editor.selectionModel

        val selectedText = if (selectionModel.hasSelection()) {
            editor.document.getText(TextRange(selectionModel.selectionStart, selectionModel.selectionEnd))
        } else {
            "No text selected"
        }

        showMessageBoxOverText(editor, selectedText)
    }

    private fun showMessageBoxOverText(editor: Editor, message: String) {
        val caretOffset = editor.caretModel.offset
        val inlayModel = editor.inlayModel

        val inlay = inlayModel.addInlineElement(
            caretOffset,
            true,
            object : EditorCustomElementRenderer {
                override fun calcWidthInPixels(inlay: Inlay<*>): Int = 220
                override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, attrs: TextAttributes) {
                    val label = JBLabel(message).apply {
                        isOpaque = true
                        background = JBColor.YELLOW
                        foreground = JBColor.BLACK
                        border = BorderFactory.createLineBorder(JBColor.GRAY)
                        font = font.deriveFont(Font.PLAIN, 12f)
                    }

                    val rendererPane = CellRendererPane()
                    SwingUtilities.paintComponent(g, label, rendererPane, r.x, r.y, r.width, r.height)
                }
            }
        )

        // Optional auto-remove inlay after 5 seconds
        javax.swing.Timer(5000) {
            inlay?.dispose()
        }.start()
    }


    fun replaceTextWithInlineControls(editor: Editor, project: Project, start: Int, end: Int, replacementText: String) {
        val originalText = editor.document.getText(com.intellij.openapi.util.TextRange(start, end))

        ApplicationManager.getApplication().runWriteAction {
            editor.document.replaceString(start, end, replacementText)
            editor.selectionModel.removeSelection()

            val inlayModel = editor.inlayModel

            val panel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 2)).apply {
                background = Color(255, 255, 200) // light yellow
                add(JButton("✅ Accept").apply {
                    addActionListener {
                        removeInlays(editor)
                    }
                })
                add(JButton("❌ Reject").apply {
                    addActionListener {
                        ApplicationManager.getApplication().runWriteAction {
                            editor.document.replaceString(start, start + replacementText.length, originalText)
                        }
                        removeInlays(editor)
                    }
                })
            }

            inlayModel.addInlineElement(start + replacementText.length, true, object : EditorCustomElementRenderer {
                override fun calcWidthInPixels(inlay: Inlay<*>): Int = panel.preferredSize.width
                override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
                    SwingUtilities.paintComponent(g, panel, null, targetRegion.x, targetRegion.y, panel.width, panel.height)
                }
            })
        }
    }

    fun removeInlays(editor: Editor) {
        val inlays = editor.inlayModel.getInlineElementsInRange(0, editor.document.textLength)
        inlays.forEach { it.dispose() }
    }

}
