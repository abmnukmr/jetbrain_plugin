package com.github.abmnukmr.jetbrainplugin.listener

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBPanel
import com.intellij.ui.awt.RelativePoint
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

class SelectionInputPopupListener(private val project: Project) : SelectionListener {
    override fun selectionChanged(e: SelectionEvent) {
        val editor = e.editor
        if (editor.project != project) return

        val selectedTextRange = e.newRange
        if (selectedTextRange.isEmpty) return

        val startOffset = selectedTextRange.startOffset
        val visualPos = editor.offsetToVisualPosition(startOffset)
        val point = editor.visualPositionToXY(visualPos)

        val inputPanel = createPopupPanel { text ->
            println("User entered: $text")
        }

        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(inputPanel, inputPanel)
            .setRequestFocus(true)
            .setCancelOnClickOutside(true)
            .createPopup()
            .show(RelativePoint(editor.contentComponent, point))
    }

    private fun createPopupPanel(onSubmit: (String) -> Unit): JPanel {
        val panel = JBPanel<JBPanel<*>>() // or simply JBPanel<*>()
        panel.layout = FlowLayout(FlowLayout.LEFT)


        val input = JBTextField(20)
        val button = JButton("Accept").apply {
            addActionListener {
                onSubmit(input.text)
            }
        }

        panel.add(input)
        panel.add(button)
        return panel
    }
}
