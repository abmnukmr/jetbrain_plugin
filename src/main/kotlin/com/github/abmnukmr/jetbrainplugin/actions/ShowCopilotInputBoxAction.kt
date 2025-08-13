package com.github.abmnukmr.jetbrainplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class ShowCopilotInputBoxAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

        if (!editor.selectionModel.hasSelection()) {
            Messages.showInfoMessage("Please select some text first.", "No Selection")
            return
        }

        showInlineInputBox(project, editor)
    }



    private fun showInlineInputBox(project: Project, editor: Editor) {
        val selectionModel = editor.selectionModel
        val startOffset = selectionModel.selectionStart
        val document = editor.document
        val lineNumber = document.getLineNumber(startOffset)

        val inlayModel = editor.inlayModel

        val inputField = JBTextField(20).apply {
            font = Font("JetBrains Mono", Font.PLAIN, 13)
            border = BorderFactory.createEmptyBorder(2, 6, 2, 6)
            background = JBColor.PanelBackground
            maximumSize = Dimension(300, 24) // Optional cap
            preferredSize = Dimension(300, 24)
        }

        val sendButton = JButton("➤").apply {
            preferredSize = Dimension(32, 24)
            minimumSize = Dimension(32, 24)
            maximumSize = Dimension(32, 24)
            font = Font("Dialog", Font.PLAIN, 12)
            isFocusPainted = false
            background = JBColor.PanelBackground
            border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
        }

        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            border = JBUI.Borders.empty(2)
            //add(inputField)
            //add(Box.createRigidArea(Dimension(4, 0)))
            add(sendButton)
        }
        sendButton.addActionListener {
            val input = inputField.text
            Messages.showInfoMessage("Sent: $input", "Input Sent")
            inputField.text = ""
        }


        val renderer = object : EditorCustomElementRenderer {
            override fun calcWidthInPixels(inlay: Inlay<*>): Int = panel.preferredSize.width
            override fun calcHeightInPixels(inlay: Inlay<*>): Int = panel.preferredSize.height

//            override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttributes: TextAttributes) {
//                panel.setBounds(r)
//                val g2 = g.create(r.x, r.y, r.width, r.height)
//                panel.paint(g2)
//                g2.dispose()
//            }

            override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttrs: TextAttributes) {
                panel.setBounds(r)
                panel.paint(g.create(r.x, r.y, r.width, r.height))
            }

        }

        inlayModel.addBlockElement(
            document.getLineStartOffset(lineNumber),
            true,  // relatesToPrecedingText
            true,  // showAbove
            0,     // priority
            renderer
        )
    }


}
