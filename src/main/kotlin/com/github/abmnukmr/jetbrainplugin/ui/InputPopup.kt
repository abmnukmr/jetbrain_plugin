package com.github.abmnukmr.jetbrainplugin.ui

import com.github.abmnukmr.jetbrainplugin.EditorController.CodeContext
import com.github.abmnukmr.jetbrainplugin.services.SSEClient
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.IconUtil
import groovy.json.StringEscapeUtils
import java.awt.*
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.SwingUtilities

object MLLIcons {
    val SendArrow: Icon = IconLoader.getIcon("/icon/send.svg", MLLIcons::class.java)
}

class InputPopup {


     fun showInputPopup(editor: Editor, point: RelativePoint, project: Project) {

        val panel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT))

        val textField = JBTextField(35).apply {
            border=null
        }
        val scaledIcon = IconUtil.scale(MLLIcons.SendArrow, null, 1.8f)
        val dropdownItems = arrayOf("Gemini", "ChatGPT", "Cludae")
        val comboBox = JComboBox(dropdownItems).apply {
            preferredSize = Dimension(100, 30)  // Set preferred size for the dropdown
            cursor= Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = "Select an option"
            isFocusable = false                // Optional: avoid focus border if desired
            addActionListener {
                val selected = selectedItem as String
                println("Dropdown selected: $selected")
            }
        }
        val button = JButton(scaledIcon).apply {
            cursor= Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isContentAreaFilled = false  // No background fill
            isBorderPainted = false      // No border
            isFocusPainted = false       // No focus border on click
            isOpaque = false
            margin = Insets(0, 0, 0, 0) // Remove extra internal padding
            preferredSize = Dimension(25, 25)// Transparent background
            toolTipText = "Send"
            addActionListener {
                panel.border = ShimmeringGradientBorder(
                    thickness = 3,
                    colors =  arrayOf(
                        Color.decode("#3F5EFB"),
                        Color.decode("#FC466B")
                    ),

                    cornerRadius = 15,  // Set your desired border radius here
                    animationSpeed = 100
                )

                val (fileName, language, fullCode, caretOffset, selectedText) = CodeContext().getCodeContextWithSelection(project, editor)
                val selectedCodeContext = selectedText?: ""
                SSEClient().fetchInlineCorrectionWordStream(
                    fullCode = fullCode ,
                    selectedCode = selectedCodeContext,
                    query= textField.text,
                    language = language,
                    onChunk = { chunk ->
                        panel.border = null


                        //val code = extractCodeFromMarkdownChunk(project,chunk)
                        appendToEditor(project, editor, chunk )
                    },
                    onError = { error ->
                        panel.border = ShimmeringGradientBorder(
                            thickness = 3,
                            colors =  arrayOf(
                                Color.RED,
                                Color.RED

                            ),

                            cornerRadius = 15,  // Set your desired border radius here
                            animationSpeed = 100
                        )
                    },
                    onComplete = {
                        SwingUtilities.getWindowAncestor(this)?.dispose()

                    }
                )

            }
        }

        panel.add(textField)
        panel.add(comboBox)
        panel.add(button)

        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, panel)
            .setRequestFocus(true)
            .setCancelOnClickOutside(true)
            .createPopup()
            .show(point)
    }


    fun appendToEditor(project: Project, editor: Editor, text: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val caret = editor.caretModel
            val offset = caret.offset
            editor.document.insertString(offset, text)
            caret.moveToOffset(offset + text.length)
        }
    }
}