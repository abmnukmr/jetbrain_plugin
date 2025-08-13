package com.github.abmnukmr.jetbrainplugin.services

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange

class InputBoxInEditor {

    fun showInputBoxInEditor(project: Project) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

        val offset = editor.caretModel.offset
        val inlayModel = editor.inlayModel

        // Create input + button UI
        val panel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            isOpaque = false
            border = JBUI.Borders.empty(4)

            val textField = JTextField(30).apply {
                font = font.deriveFont(12f)
                margin = Insets(2, 6, 2, 6)
            }

            val sendButton = JButton("Send").apply {
                font = font.deriveFont(11f)
                background = Color(0xE0F2F1)
                foreground = Color(0x00796B)
                isFocusable = false
                border = BorderFactory.createLineBorder(Color(0x80CBC4))
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }

            add(textField)
            add(sendButton)

            // Send button handler
            sendButton.addActionListener {
                val input = textField.text.trim()
                if (input.isNotEmpty()) {
                    println("🚀 Input submitted: $input")

                    // Example: insert the response text (you can replace this with LLM result)
                    WriteCommandAction.runWriteCommandAction(project) {
                        editor.document.insertString(editor.caretModel.offset, "\nYou said: $input\n")
                    }

                    // Remove inlay after sending
                    //inlay?.dispose()
                }
            }
        }

        // Add block inlay below current line
        var inlay: Inlay<*>? = null
        inlay = inlayModel.addBlockElement(
            offset,
            true,  // relates to above line
            true,  // show above if possible
            0,
            object : EditorCustomElementRenderer {
                override fun calcWidthInPixels(inlay: Inlay<*>): Int = panel.preferredSize.width
                override fun calcHeightInPixels(inlay: Inlay<*>): Int = panel.preferredSize.height

                override fun paint(
                    inlay: Inlay<*>,
                    g: Graphics,
                    targetRegion: Rectangle,
                    textAttributes: TextAttributes
                ) {
                    SwingUtilities.paintComponent(
                        g, panel, null,
                        targetRegion.x, targetRegion.y,
                        targetRegion.width, targetRegion.height
                    )
                }
            }
        )
    }

    fun showInputBoxInEditor(project: Project, editor: Editor, offset: Int) {
        val inlayModel = editor.inlayModel

        val panel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 5, 0)
            isOpaque = false
            border = JBUI.Borders.empty(4)

            val textField = JTextField(30).apply {
                font = font.deriveFont(12f)
                margin = Insets(2, 6, 2, 6)
            }

            val sendButton = JButton("Send").apply {
                font = font.deriveFont(11f)
                background = Color(0xE0F2F1)
                foreground = Color(0x00796B)
                isFocusable = false
                border = BorderFactory.createLineBorder(Color(0x80CBC4))
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }

            add(textField)
            add(sendButton)

            sendButton.addActionListener {
                val input = textField.text.trim()
                if (input.isNotEmpty()) {
                    println("✉️ User input for selected text: $input")

                    // Example: insert comment below
                    WriteCommandAction.runWriteCommandAction(project) {
                        editor.document.insertString(
                            editor.caretModel.offset,
                            "\n// Comment: $input\n"
                        )
                    }

                   // inlay?.dispose()
                }
            }
        }

        var inlay: Inlay<*>? = null
        inlay = inlayModel.addBlockElement(
            offset,
            true, true, 0,
            object : EditorCustomElementRenderer {
                override fun calcWidthInPixels(inlay: Inlay<*>): Int = panel.preferredSize.width
                override fun calcHeightInPixels(inlay: Inlay<*>): Int = panel.preferredSize.height
                override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, attrs: TextAttributes) {
                    //SwingUtilities.paintComponent(g, panel, panel, r.x, r.y, r.width, r.height)
                }
            }
        )
    }

    fun attachSelectionListener(project: Project, editor: Editor) {
        val editorEx = editor as? EditorEx ?: return

        val disposable = Disposer.newDisposable("SelectionListener-${editor.hashCode()}")
        Disposer.register(project, disposable) // ✅ fallback if editorEx.disposable is not available

        editorEx.selectionModel.addSelectionListener(object : SelectionListener {
            override fun selectionChanged(e: SelectionEvent) {
                val range = e.newRange ?: return
                if (range.isEmpty) return

                val selectedText = editorEx.document.getText(TextRange(range.startOffset, range.endOffset))
                if (selectedText.isNotBlank()) {
                    showInputBoxInEditor(project, editorEx, range.endOffset)
                }
            }
        }, disposable)
    }

}