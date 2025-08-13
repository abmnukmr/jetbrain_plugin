package com.github.abmnukmr.jetbrainplugin.listener

import CodeBlockEditor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JButton
import javax.swing.JPanel
import com.intellij.openapi.editor.InlayModel
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.InlayModelImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.editor.EditorCustomElementRenderer

class SelectionInputInlayListener(val project: Project) : SelectionListener {
    private var currentInlay: Inlay<*>? = null

    override fun selectionChanged(e: SelectionEvent) {
        val editor = e.editor
        if (editor.project != project) return

        val selectedText = e.newRange
        if (selectedText.isEmpty) {
            // Clear inlay if selection is removed
            currentInlay?.let {
                it.dispose()
                currentInlay = null
            }
            return
        }

        val offset = selectedText.endOffset

        val panel = createInputPanel { userInput ->
            println("Accepted: $userInput") // You can replace this with whatever logic you need
            currentInlay?.dispose()
        }

        val renderer = object : EditorCustomElementRenderer {
            override fun calcWidthInPixels(inlay: Inlay<*>): Int = panel.preferredSize.width
            override fun calcHeightInPixels(inlay: Inlay<*>): Int = panel.preferredSize.height
            override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttrs: TextAttributes) {
                panel.setBounds(r)
                val g2 = g.create(r.x, r.y, r.width, r.height)
                panel.paint(g2)
                g2.dispose()
            }
        }

        currentInlay?.dispose() // Dispose any previous inlay
        val props = InlayProperties().showAbove(true)

        currentInlay = editor.inlayModel.addBlockElement(
            offset,
            props,
            renderer
        )
    }

    private fun createInputPanel(onSubmit: (String) -> Unit): JPanel {
        val panel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT))
            .apply {
            border = JBUI.Borders.empty(5)
            background = Color(245, 245, 245)
        }

        val inputField = JBTextField().apply {
            columns = 30
            emptyText.text = "Type your suggestion..."
        }

        val acceptButton = JButton("Accept").apply {
            println(CodeBlockEditor(project).readCodeAroundCaret(project))
            addActionListener {
                onSubmit(inputField.text)
            }
        }



        panel.add(inputField)
        panel.add(acceptButton)
        return panel
    }
}
