package com.github.abmnukmr.jetbrainplugin.services

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import java.awt.*
import javax.swing.*
import com.intellij.openapi.editor.EditorCustomElementRenderer
import java.awt.image.BufferedImage

class InlineSuggestionRenderer(
    private val project: Project,
    private val editor: Editor,
    private val startOffset: Int,
    private val endOffset: Int,
    private val replacement: String
) : EditorCustomElementRenderer {

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val panel = createPanel(inlay)
        return panel.preferredSize.width
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttributes: TextAttributes) {
        val panel = createPanel(inlay)
        val img = BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB)
        val g2 = img.createGraphics()
        panel.setBounds(0, 0, r.width, r.height)
        panel.paint(g2)
        g.drawImage(img, r.x, r.y, null)
    }

    private fun createPanel(inlay: Inlay<*>): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.background = JBColor(0xE6FFED, 0x2A2A2A)

        val textArea = JTextArea(replacement).apply {
            isEditable = false
            background = JBColor(0xE6FFED, 0x2A2A2A)
            foreground = JBColor.BLACK
            font = Font("Monospaced", Font.PLAIN, 12)
            border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        }

        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            background = JBColor(0xE6FFED, 0x2A2A2A)
        }

        val accept = JButton("✅ Accept").apply {
            addActionListener {
                WriteCommandAction.runWriteCommandAction(project) {
                    editor.document.replaceString(startOffset, endOffset, replacement)
                    editor.inlayModel.getBlockElementsInRange(startOffset, startOffset)
                        .forEach { it.dispose() }
                }
            }
        }

        val reject = JButton("❌ Reject").apply {
            addActionListener {
                editor.inlayModel.getBlockElementsInRange(startOffset, startOffset)
                    .forEach { it.dispose() }
            }
        }

        buttonPanel.add(accept)
        buttonPanel.add(Box.createHorizontalStrut(10))
        buttonPanel.add(reject)

        panel.add(textArea, BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        return panel
    }
}
