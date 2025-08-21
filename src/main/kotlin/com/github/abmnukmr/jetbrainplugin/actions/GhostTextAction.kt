package com.github.abmnukmr.jetbrainplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.TextRange
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

class GhostTextAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val caret = editor.caretModel.primaryCaret

        // Remove existing ghost text
        editor.inlayModel.getInlineElementsInRange(0, editor.document.textLength)
            .filter { it.renderer is GhostTextRenderer }
            .forEach { it.dispose() }

        // Insert ghost text at caret position
        editor.inlayModel.addInlineElement(
            caret.offset,
            true,
            GhostTextRenderer(" ← AI Suggestion")
        )
    }
}

class GhostTextRenderer(private val text: String) : EditorCustomElementRenderer {
    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val fontMetrics = inlay.editor.contentComponent.getFontMetrics(
            inlay.editor.colorsScheme.getFont(EditorFontType.PLAIN)
        )
        return fontMetrics.stringWidth(text)
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRect: Rectangle,
        textAttributes: com.intellij.openapi.editor.markup.TextAttributes
    ) {
        g.color = Color.GRAY
        g.font = inlay.editor.colorsScheme.getFont(EditorFontType.ITALIC)
        g.drawString(text, targetRect.x, targetRect.y + g.fontMetrics.ascent)
    }
}
