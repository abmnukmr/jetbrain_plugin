package com.github.abmnukmr.jetbrainplugin.listener

import com.github.abmnukmr.jetbrainplugin.ui.InputPopup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel
import com.intellij.openapi.editor.EditorCustomElementRenderer
import java.awt.event.MouseMotionAdapter
import javax.swing.JLabel

class SelectionSuggestListener(private val project: Project) : SelectionListener {

    private var currentInlay: Inlay<*>? = null
    private var popup: JBPopup? = null
    private var lastOffset: Int? = null
    private var mouseListenerAdded = false

    override fun selectionChanged(e: SelectionEvent) {
        val editor = e.editor
        if (editor.project != project) return

        val range = e.newRange
        if (range.isEmpty || range == e.oldRange) return

        val offset = range.endOffset

        // Dispose old inlay and popup
        currentInlay?.let {
            Disposer.dispose(it)
            currentInlay = null
        }
        popup?.cancel()
        popup = null

        // Create renderer with a lightbulb
        val renderer = object : EditorCustomElementRenderer {
            override fun calcWidthInPixels(inlay: Inlay<*>): Int = 24
            override fun calcHeightInPixels(inlay: Inlay<*>): Int = 24
            override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttrs: TextAttributes) {
                val g2 = g.create(r.x, r.y, r.width, r.height)
                g2.font = Font("Dialog", Font.PLAIN, 18)

                // Align icon to the very left
                g2.drawString("💡", 0, r.height - 6)
                g2.dispose()
            }
        }

        currentInlay = editor.inlayModel.addInlineElement(
            offset,
            InlayProperties().relatesToPrecedingText(true),
            renderer
        )
        lastOffset = offset

        // Add listener once
        if (!mouseListenerAdded) {
            editor.contentComponent.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount != 1 || lastOffset == null) return

                    val clickPos = editor.xyToVisualPosition(e.point)
                    val clickOffset = editor.visualPositionToOffset(clickPos)

                    if (clickOffset == lastOffset) {
                        val point = editor.visualPositionToXY(editor.offsetToVisualPosition(offset)).apply {
                            translate(0, editor.lineHeight)
                        }

                        // Convert to RelativePoint for the popup
                        val relativePoint = RelativePoint(editor.contentComponent, point)

                        InputPopup().showInputPopup(editor, relativePoint, project)
                    }
                }

            })
            editor.contentComponent.addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    val pos = editor.xyToVisualPosition(e.point)
                    val hoverOffset = editor.visualPositionToOffset(pos)
                    editor.contentComponent.cursor =
                        if (hoverOffset == lastOffset) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        else Cursor.getDefaultCursor()
                }
            })
            mouseListenerAdded = true
        }
    }

    private fun showPopupAtOffset(editor: Editor, offset: Int) {
        if (popup?.isVisible == true) return

        val point = editor.visualPositionToXY(editor.offsetToVisualPosition(offset)).apply {
            translate(0, editor.lineHeight)
        }

        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(createInputPanel { text ->
                println("User submitted: $text")
                popup?.cancel()
                currentInlay?.dispose()
            }, null)
            .setRequestFocus(true)
            .setCancelOnClickOutside(true)
            .createPopup()

        popup?.show(RelativePoint(editor.contentComponent, point))
    }

    private fun createInputPanel(onSubmit: (String) -> Unit): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            layout = FlowLayout(FlowLayout.LEFT)
            val input = JBTextField(20)
            val button = JButton("Accept").apply {
                addActionListener {
                    onSubmit(input.text)
                }
            }
            add(input)
            add(button)
        }
    }
}
