package com.github.abmnukmr.jetbrainplugin.intellisence

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class GhostCodeManager(private val editor: Editor) {
    private var currentInlay: Inlay<*>? = null
    private val segmentBounds = mutableListOf<Pair<String, Rectangle>>()

    data class GhostSegment(val text: String, val color: Color)

    fun showCodeGhostText(segments: List<GhostSegment>) {
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

                // Fallback: return at least 1 to avoid "positive width" exception
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

                var y = targetRegion.y + editor.ascent

                for (segment in segments) {
                    val lines = segment.text.lines()
                    var x = targetRegion.x
                    for (line in lines) {
                        g.color = segment.color
                        g.drawString(line, x, y)

                        val width = g.fontMetrics.stringWidth(line)
                        val height = g.fontMetrics.height
                        segmentBounds.add(line to Rectangle(x, y - g.fontMetrics.ascent, width, height))

                        y += lineHeight
                        x = targetRegion.x
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