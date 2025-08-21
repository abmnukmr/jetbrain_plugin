package com.github.abmnukmr.jetbrainplugin.intellisence

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayModel
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import java.awt.*

/**
 * Manages inline "ghost text" (like GitHub Copilot style).
 */
class GhostTextManager(private val editor: Editor) {
    private var currentInlay: Inlay<*>? = null
    private var currentText: String = ""
    private val segmentBounds = mutableListOf<Pair<String, Rectangle>>() // bounds per segment
    private var listenersAdded = false

    fun showGhostText(str: String="") {
        clearAllGhostText()

        val offset = editor.caretModel.offset
        val inlayModel: InlayModel = editor.inlayModel

        val renderer = object : EditorCustomElementRenderer {

            private val parts: List<Pair<String, Color>> = if (!str.trim().equals("") ) {
                listOf(str to Color(120, 193, 243, 80)) // single segment with a color
            } else {
                listOf(
                    "  ⌘ + Enter " to Color(120, 193, 243, 80),
                    "to open " to Color(128, 128, 128, 70),
                    "Precommit AI" to Color(255, 170, 170, 80)
                )
            }

            override fun calcWidthInPixels(inlay: Inlay<*>): Int {
                val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
                val metrics = editor.contentComponent.getFontMetrics(font)
                return parts.sumOf { metrics.stringWidth(it.first) }
            }

            override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
                segmentBounds.clear()
                val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
                g.font = font

                var x = targetRegion.x
                val y = targetRegion.y + editor.ascent
                for ((part, color) in parts) {
                    g.color = color
                    g.drawString(part, x, y)

                    val width = g.fontMetrics.stringWidth(part)
                    val height = g.fontMetrics.height
                    segmentBounds.add(part to Rectangle(x, y - g.fontMetrics.ascent, width, height))

                    x += width
                }
            }
        }

        currentInlay = inlayModel.addInlineElement(offset, true, renderer)

        addListeners()
    }

    private fun addListeners() {
        if (listenersAdded) return
        listenersAdded = true

        // Click listener
        editor.addEditorMouseListener(object : EditorMouseListener {
            override fun mouseClicked(e: EditorMouseEvent) {
                val point = e.mouseEvent.point
                segmentBounds.forEach { (text, rect) ->
                    if (rect.contains(point)) {
                        println("Clicked on segment: $text")
                        // TODO: perform action per segment
                    }
                }
            }
        })

        // Hover listener
        editor.addEditorMouseMotionListener(object : EditorMouseMotionListener {
            override fun mouseMoved(e: EditorMouseEvent) {
                val point = e.mouseEvent.point
                val hovered = segmentBounds.any { it.second.contains(point) }
                editor.contentComponent.cursor =
                    if (hovered) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    else Cursor.getDefaultCursor()
            }
        })
    }




    fun acceptGhostText() {
        val inlay = currentInlay ?: return
        inlay.dispose()

        val document = editor.document
        val offset = editor.caretModel.offset

        WriteCommandAction.runWriteCommandAction(editor.project) {
            document.insertString(offset, currentText)
            editor.caretModel.moveToOffset(offset + currentText.length)
        }

        currentInlay = null
        currentText = ""
    }

    fun clearGhostText() {
        currentInlay?.dispose()
        currentInlay = null
        currentText = ""
    }

    fun clearAllGhostText() {
        editor.inlayModel.getInlineElementsInRange(0, editor.document.textLength)
            .filter { it.renderer is EditorCustomElementRenderer }
            .forEach { it.dispose() }

        currentInlay = null
        currentText = ""
    }
    fun hasGhostText(): Boolean = currentInlay != null
}
