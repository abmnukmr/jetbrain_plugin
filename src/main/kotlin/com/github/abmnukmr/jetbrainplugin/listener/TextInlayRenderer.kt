package com.github.abmnukmr.jetbrainplugin.listener

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle

class TextInlayRenderer(private val text: String) : EditorCustomElementRenderer {
    private val font = Font("Dialog", Font.ITALIC, 12)

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val metrics = inlay.editor.contentComponent.getFontMetrics(font)
        return metrics.stringWidth(text)
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        g.font = font
        g.color = JBColor.GRAY
        g.drawString(text, targetRegion.x, targetRegion.y + g.fontMetrics.ascent)
    }
}
