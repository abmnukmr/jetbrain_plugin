package com.github.abmnukmr.jetbrainplugin.intellisence

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.KeyStroke

class GhostCodeManager(private val editor: Editor) {
    private var currentInlay: Inlay<*>? = null
    private val segmentBounds = mutableListOf<Pair<String, Rectangle>>()

    data class GhostSegment(val text: String, val color: Color)

    fun showCodeGhostCode(segments: List<GhostSegment>) {
        clearAllGhostText()
        val offset = editor.caretModel.offset
        val inlayModel = editor.inlayModel

        val renderer = object : EditorCustomElementRenderer {
            override fun calcWidthInPixels(inlay: Inlay<*>): Int {
                val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
                val metrics = editor.contentComponent.getFontMetrics(font)

                // Compute max width among all non-empty lines
                val maxWidth = segments
                    .flatMap { it.text.lines() }
                    .filter { it.isNotEmpty() }
                    .maxOfOrNull { metrics.stringWidth(it) }

                return maxWidth ?: metrics.stringWidth(" ")
            }
            override fun paint(
                inlay: Inlay<*>,
                g: Graphics,
                targetRegion: Rectangle,
                textAttributes: TextAttributes
            ) {
                segmentBounds.clear()
                val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
                g.font = font
                val lineHeight = g.fontMetrics.height

                val caretXY = editor.visualPositionToXY(editor.caretModel.visualPosition)
                var x = caretXY.x
                var y = targetRegion.y + editor.ascent

                // 1. Draw hint above caret
                val tabShortcut = KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))
                val hint = "Press $tabShortcut to accept · Press Esc to dismiss"

                val hintWidth = g.fontMetrics.stringWidth(hint) + 20
                val hintHeight = lineHeight
                val hintX = caretXY.x
                val hintY = y - lineHeight - 6

                g.color = Color(24, 24, 24)
                g.fillRoundRect(hintX, hintY - g.fontMetrics.ascent, hintWidth, hintHeight, 10, 10)

                g.color = Color(160, 160, 160) // lighter gray for text
                g.drawString(hint, hintX + 10, hintY)

                // 2. Draw ghost suggestion after caret
                for (segment in segments) {
                    val lines = segment.text.lines()
                    for (line in lines) {
                        g.color = segment.color
                        g.drawString(line, x, y)

                        val width = g.fontMetrics.stringWidth(line)
                        val height = g.fontMetrics.height
                        segmentBounds.add(line to Rectangle(x, y - g.fontMetrics.ascent, width, height))

                        y += lineHeight
                        x = caretXY.x // reset for new line
                    }
                }
            }

        }

        currentInlay = inlayModel.addInlineElement(offset, true, renderer)
        addClickListener()
    }



    private fun addClickListener() {
        editor.contentComponent.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val point = e.point
                segmentBounds.forEach { (text, rect) ->
                    if (rect.contains(point)) {
                        println("Clicked on code segment: $text")
                        // You can trigger further actions here
                    }
                }
            }
        })
    }

    fun clearAllGhostText() {
        editor.inlayModel.getInlineElementsInRange(0, editor.document.textLength)
            .filter { it.renderer is EditorCustomElementRenderer }
            .forEach { it.dispose() }

        currentInlay = null
        //currentText = ""
    }
}