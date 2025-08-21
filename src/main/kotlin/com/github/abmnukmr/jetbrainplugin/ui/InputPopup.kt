package com.github.abmnukmr.jetbrainplugin.ui

import com.github.abmnukmr.jetbrainplugin.EditorController.CodeContext
import com.github.abmnukmr.jetbrainplugin.services.SSEClient
import com.intellij.icons.AllIcons
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.util.minimumHeight
import com.intellij.util.IconUtil
import com.intellij.util.animation.transparent
import java.awt.*
import javax.swing.*


object MLLIcons {
    val SendArrow: Icon = IconLoader.getIcon("/icon/send.svg", MLLIcons::class.java)
}

class InputPopup {
    var offSetStart = 0;
    var offSetEnd = 0;

    fun showDialog(editor: Editor, project: Project){
        val caretPos = editor.visualPositionToXY(editor.caretModel.visualPosition)
        val editorLocationOnScreen = editor.contentComponent.locationOnScreen
        val screenX = editorLocationOnScreen.x + caretPos.x
        val screenY = editorLocationOnScreen.y + caretPos.y + editor.lineHeight

// Owner from IDE frame
        val ideFrame = WindowManager.getInstance().getFrame(editor.project)

// A lightweight dialog instead of JWindow — better for focus inside IntelliJ
        val dialog = JDialog(ideFrame, false) // false = non-modal
        dialog.isUndecorated = true
        dialog.isAlwaysOnTop = true



        val panel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT, 5, 5))


        val textField = JTextArea(3, 35).apply {
            lineWrap = true
            wrapStyleWord = true
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE) // allow vertical expansion
        }

// Wrap in scroll pane for proper resizing
        val scrollPane = JScrollPane(textField).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            preferredSize = Dimension(300, 60) // initial size
            maximumSize = Dimension(Int.MAX_VALUE, 200) // max height
            minimumSize = Dimension(100, 30)
        }


        val scaledIcon = IconUtil.scale(MLLIcons.SendArrow, null, 1.5f)

        val comboBox = JComboBox(arrayOf("Gemini", "ChatGPT", "Cludae")).apply {
            preferredSize = Dimension(100, 30)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = "Select an option"
            isFocusable = false
        }

        val buttonAccept = JButton(scaledIcon).apply {
            border = ShimmeringGradientBorder().createRoundedBorder(Color(144, 238, 144), 1, 10)
            isContentAreaFilled = false
            isFocusPainted = false
            foreground = Color(144, 238, 144)
            isVisible = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }

        val buttonReject = JButton("Reject").apply {
            border = ShimmeringGradientBorder().createRoundedBorder(Color(255, 99, 71), 1, 10)
            isContentAreaFilled = false
            isFocusPainted = false
            isVisible = false
            foreground = Color(255, 99, 71)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }

        val closeButton = JButton(AllIcons.Actions.CloseHovered).apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isContentAreaFilled = false
            isBorderPainted = false
            isFocusPainted = false
            isOpaque = false
            preferredSize = Dimension(30, 30)
            addActionListener {
                SwingUtilities.getWindowAncestor(this)?.dispose()
            }
        }

        var chunking = false;
        val buttonSend = JButton("semd").apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isContentAreaFilled = false
            isBorderPainted = false
            isFocusPainted = false
            isOpaque = false
            margin = Insets(0, 0, 0, 0)
            preferredSize = Dimension(25, 25)
            toolTipText = "Send"

            addActionListener {
                panel.border = ShimmeringGradientBorder(
                    thickness = 3,
                    colors = arrayOf(Color.decode("#3F5EFB"), Color.decode("#FC466B")),
                    cornerRadius = 15,
                    animationSpeed = 30
                )
                this.isVisible =true

                val (fileName, language, fullCode, caretOffset, selectedText) =
                    CodeContext().getCodeContextWithSelection(project, editor)
                val selectedCodeContext = selectedText ?: ""

                SSEClient().fetchInlineCorrectionWordStream(
                    fullCode = fullCode,
                    selectedCode = selectedCodeContext,
                    query = textField.text,
                    language = language,
                    onChunk = { chunk ->
                        if(!chunking){

                            panel.border = ShimmeringGradientBorder(
                                thickness = 3,
                                colors = arrayOf(Color.decode("#57C785"), Color.decode("#EDDD53")),
                                cornerRadius = 15,
                                animationSpeed = 30
                            )
                            chunking = true;
                            panel.revalidate()
                            panel.repaint()
                        }
                        textField.text = ""

                        appendToEditor(project, editor, chunk)
                    },
                    onError = {
                        textField.text = ""
                        panel.border = ShimmeringGradientBorder(
                            thickness = 3,
                            colors = arrayOf(Color.RED, Color.RED),
                            cornerRadius = 15,
                            animationSpeed = 100
                        )
                        chunking = false
                        ;
                    },
                    onComplete = {

                        buttonAccept.isVisible=true
                        buttonReject.isVisible=true
                        textField.isVisible =true
                        comboBox.isVisible= true
                        this.isVisible =true
                        closeButton.isVisible=true  // This line is making it visible again
                        textField.text = ""
                        panel.border = null
                        chunking = false
                        panel.revalidate()
                        panel.repaint()
                    }
                )
            }

        }

        // Add initial components
        panel.add(buttonAccept)
        panel.add(buttonReject)
        panel.add(textField)
        panel.add(comboBox)
        panel.add(buttonSend)
        panel.add(closeButton)


        dialog.contentPane.add(panel)
        dialog.pack()
        dialog.setLocation(screenX, screenY)
        dialog.isVisible = true
       // dialog.jMenuBar()

// Request focus after showing
        SwingUtilities.invokeLater {
            textField.requestFocusInWindow()
        }
    }

    var undoUp = false;
    fun showInputPopup(editor: Editor, point: RelativePoint, project: Project) {
        var popup: JBPopup? = null
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.border = BorderFactory.createEmptyBorder(0, 5, 0, 5)

        mainPanel.add(Box.createVerticalGlue())


        val contentPanel = JPanel()
        val fixedSize = Dimension( editor.getComponent().width-100, 50)
        contentPanel.setPreferredSize(fixedSize)
        contentPanel.setMaximumSize(fixedSize)
        contentPanel.isOpaque = false
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.X_AXIS)


        // JTextArea allows for multi-line input and vertical growth.
        val textField = JBTextField(70).apply {
            border = null
        }

        val scaledIcon = IconUtil.scale(MLLIcons.SendArrow, null, 1.5f)

        val comboBox = JComboBox(arrayOf("Gemini", "ChatGPT", "Cludae")).apply {
            preferredSize = Dimension(100, 30)
            maximumSize = Dimension(100, 30)
            minimumSize = Dimension(100, 30)
            prototypeDisplayValue = "ChatGPT"
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = "Select an option"
            isFocusable = false
        }

        val buttonAccept = JButton("Accept").apply {
            border = ShimmeringGradientBorder().createRoundedBorder(Color(144, 238, 144), 1, 10)
            isContentAreaFilled = false
            isFocusPainted = false
            foreground = Color(144, 238, 144)
            isVisible = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            margin = Insets(0, 0, 0, 0)
            preferredSize = Dimension(100, 35)
            maximumSize = Dimension(100, 35)
            minimumSize = Dimension(100, 35)
            addActionListener {
                removeHighlightOnAccept(editor)
                undoUp = false
                SwingUtilities.getWindowAncestor(this)?.dispose()
            }
        }

        val buttonReject = JButton("Reject").apply {
            border = ShimmeringGradientBorder().createRoundedBorder(Color(255, 99, 71), 1, 10)
            isContentAreaFilled = false
            isFocusPainted = false
            foreground = Color(255, 99, 71)
            isVisible = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            margin = Insets(0, 0, 0, 0)
            preferredSize = Dimension(100, 35)
            maximumSize = Dimension(100, 35)
            minimumSize = Dimension(100, 35)
            addActionListener{
                undoOnReject(project, editor)
            }
        }

        val closeButton = JButton(AllIcons.Actions.CloseHovered).apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isContentAreaFilled = false
            isBorderPainted = false
            isFocusPainted = false
            isOpaque = false
            preferredSize = Dimension(30, 30)
            maximumSize = Dimension(30, 30)
            minimumSize = Dimension(30, 30)
            addActionListener {
                SwingUtilities.getWindowAncestor(this)?.dispose()
                undoUp = false
                undoOnReject(project, editor)
            }
        }

        var chunking = false
        val buttonSend = JButton(scaledIcon).apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isContentAreaFilled = false
            isBorderPainted = false
            isFocusPainted = false
            isOpaque = false
            margin = Insets(0, 0, 0, 0)
            preferredSize = Dimension(25, 25)
            maximumSize = Dimension(25, 25)
            minimumSize = Dimension(25, 25)
            toolTipText = "Send"
            addActionListener {
                mainPanel.border = ShimmeringGradientBorder(
                    thickness = 3,
                    colors = arrayOf(Color.decode("#3F5EFB"), Color.decode("#FC466B")),
                    cornerRadius = 15,
                    animationSpeed = 30
                )
                this.isVisible = true

                val (fileName, language, fullCode, caretOffset, selectedText) =
                    CodeContext().getCodeContextWithSelection(project, editor)
                val selectedCodeContext = selectedText ?: ""

                SSEClient().fetchInlineCorrectionWordStream(
                    fullCode = fullCode,
                    selectedCode = selectedCodeContext,
                    query = textField.text, // 👈 Use the new textArea
                    language = language,
                    onChunk = { chunk ->
                        if(!undoUp){
                            val caret = editor.caretModel
                            offSetStart = caret.offset
                            undoUp = true
                        }
                        if (!chunking) {

                            mainPanel.border = ShimmeringGradientBorder(
                                thickness = 3,
                                colors = arrayOf(Color.decode("#57C785"), Color.decode("#EDDD53")),
                                cornerRadius = 15,
                                animationSpeed = 30
                            )
                            chunking = true
                            mainPanel.revalidate()
                            mainPanel.repaint()
                        }
                        appendToEditor(project, editor, chunk)
                    },
                    onError = {
                        mainPanel.border = ShimmeringGradientBorder(
                            thickness = 3,
                            colors = arrayOf(Color.RED, Color.RED),
                            cornerRadius = 15,
                            animationSpeed = 100
                        )
                        chunking = false
                    },
                    onComplete = {
                        buttonAccept.isVisible = true
                        buttonReject.isVisible = true
                        mainPanel.border = null

                        chunking = false
                        textField.text=""
                        mainPanel.revalidate()
                        mainPanel.repaint()
                    }
                )
            }
        }

        contentPanel.add(buttonAccept)
        contentPanel.add(Box.createHorizontalStrut(5))
        contentPanel.add(buttonReject)
        contentPanel.add(Box.createHorizontalStrut(10))
        contentPanel.add(textField) // 👈 Use the scrollPane
        contentPanel.add(Box.createHorizontalGlue()) // Pushes components to the right
        contentPanel.add(comboBox)
        contentPanel.add(Box.createHorizontalStrut(5))
        contentPanel.add(buttonSend)
        contentPanel.add(Box.createHorizontalStrut(5))
        contentPanel.add(closeButton)

        mainPanel.add(contentPanel)

         popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(mainPanel, mainPanel)
            .setRequestFocus(true)
            .setCancelOnClickOutside(false)
            .setCancelOnWindowDeactivation(false)
            .setResizable(true)
            .setMovable(true)
            .createPopup()

            popup.show(point)


    }

    fun appendToEditor(project: Project, editor: Editor, text: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val caret = editor.caretModel
            val offset = caret.offset

            // Insert the text
            editor.document.insertString(offset, text)

            // Calculate the range of the inserted text
            val startOffset = offset
            val endOffset = offset + text.length

            // Move caret to the end of inserted text
            caret.moveToOffset(endOffset)

            // Highlight the inserted text
            val markupModel: MarkupModel = editor.markupModel
            val attributes = TextAttributes()
            attributes.backgroundColor = Color(144, 238, 144, 40) // semi-transparent yellow


            offSetEnd = endOffset
            markupModel.addRangeHighlighter(
                startOffset,
                endOffset,
                0, // layer
                attributes,
                HighlighterTargetArea.EXACT_RANGE
            )
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    private fun undoOnReject(project: Project, editor: Editor) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.deleteString(offSetStart, offSetEnd)
        }
    }

    private fun removeHighlightOnAccept(editor: Editor) {
        val markupModel = editor.markupModel
        markupModel.allHighlighters.forEach { it.dispose() }
    }

}