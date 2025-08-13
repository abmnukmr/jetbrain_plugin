package com.github.abmnukmr.jetbrainplugin.EditorController

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel


class CodeContext {
    data class CodeContext(
        val fileName: String,
        val language: String,
        val fullText: String,
        val caretOffset: Int,
        val selectedText: String?  // nullable, null if no selection
    )

    fun getCodeContextWithSelection(project: Project, editor: Editor): CodeContext {
        val caretModel: CaretModel = editor.caretModel
        val caretOffset = caretModel.offset
        val document = editor.document

        val fullText = document.text

        val psiFile: PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(document)
        val fileName = psiFile?.name ?: "Unknown"
        val language = psiFile?.language?.id ?: "Unknown"

        val selectionModel = editor.selectionModel
        val selectedText = if (selectionModel.hasSelection()) selectionModel.selectedText else null

        return CodeContext(fileName, language, fullText, caretOffset, selectedText)
    }

    fun markdownToPlainText(markdown: String): String {
        // Remove language tag line if present (e.g. "java")
        val lines = markdown.lines()
        val withoutLang = if (lines.firstOrNull()?.trim()?.matches(Regex("^[a-zA-Z]+$")) == true) {
            lines.drop(1).joinToString("\n")
        } else {
            markdown
        }

        val parser = Parser.builder().build()
        val document: Document = parser.parse(withoutLang)
        val visitor = TextCollectingVisitor()
        return visitor.collectAndGetText(document)
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    fun insertAfterSelectionWithButtons(project: Project, codeToInsert: String) {
        ApplicationManager.getApplication().invokeLater {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@invokeLater
            val selectionModel = editor.selectionModel
            if (!selectionModel.hasSelection()) return@invokeLater

            val document = editor.document
            val endOffset = selectionModel.selectionEnd
            val endLine = document.getLineNumber(endOffset)

            // Get indentation from the start of selection
            val startLine = document.getLineNumber(selectionModel.selectionStart)
            val startLineText = document.getText(
                TextRange(document.getLineStartOffset(startLine), document.getLineEndOffset(startLine))
            )
            val indentation = startLineText.takeWhile { it.isWhitespace() }

            val indentedCode = codeToInsert.lines().joinToString("\n") { indentation + it }

            var insertedStartOffset = 0
            var insertedEndOffset = 0
            WriteCommandAction.runWriteCommandAction(project) {
                val insertOffset = document.getLineEndOffset(endLine)
                insertedStartOffset = insertOffset + 1
                document.insertString(insertOffset, "\n$indentedCode\n")
                insertedEndOffset = insertedStartOffset + indentedCode.length + 1
            }

            val inlayOffset = document.getLineStartOffset(endLine + 1)

            // Store button bounds for click/hover detection
            val buttonBounds = mutableListOf<Pair<String, Rectangle>>()

            val inlay = editor.inlayModel.addBlockElement(
                inlayOffset,
                true,
                true,
                0,
                object : EditorCustomElementRenderer {
                    override fun calcWidthInPixels(inlay: Inlay<*>): Int = 200
                    override fun calcHeightInPixels(inlay: Inlay<*>): Int = 30

                    override fun paint(
                        inlay: Inlay<*>,
                        g: Graphics,
                        targetRegion: Rectangle,
                        textAttributes: TextAttributes
                    ) {
                        val g2 = g as Graphics2D
                        g2.composite = AlphaComposite.SrcOver

                        val btnWidth = 70
                        val btnHeight = 30
                        val padding = 10
                        val marginFromColumn = 40 // float margin

                        buttonBounds.clear()

                        val acceptRect = Rectangle(
                            targetRegion.x + marginFromColumn,
                            targetRegion.y + 5,
                            btnWidth,
                            btnHeight
                        )

                        g2.color = Color(200, 255, 200)
                        g2.fillRect(acceptRect.x, acceptRect.y, acceptRect.width, acceptRect.height)

                        g2.color = Color(0, 128, 0)
                        g2.setStroke(BasicStroke(2f)) // optional: thicker border
                        g2.drawRect(acceptRect.x, acceptRect.y, acceptRect.width, acceptRect.height)

                        g2.color = Color.BLACK
                        g2.drawString("Accept", acceptRect.x + 14, acceptRect.y + 20)

                        buttonBounds.add("Accept" to acceptRect)

                        val rejectRect = Rectangle(
                            acceptRect.x + btnWidth + padding,
                            targetRegion.y + 5,
                            btnWidth,
                            btnHeight
                        )
                        g2.color = Color(255, 200, 200) // background
                        g2.fillRect(rejectRect.x, rejectRect.y, rejectRect.width, rejectRect.height)

                        // Border color (e.g., dark red)
                        g2.color = Color(150, 0, 0)
                        g2.setStroke(BasicStroke(2f))
                        g2.drawRect(rejectRect.x, rejectRect.y, rejectRect.width, rejectRect.height)
                        g2.color = Color.BLACK
                        g2.drawString("Reject", rejectRect.x + 14, rejectRect.y + 20)
                        buttonBounds.add("Reject" to acceptRect)
                    }
                }
            )

            val mouseAdapter = object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val point = e.point
                    for ((name, rect) in buttonBounds) {
                        if (rect.contains(point)) {
                            when (name) {
                                "Accept" -> {
                                    println("Accept clicked")
                                    inlay?.dispose()
                                }
                                "Reject" -> {
                                    WriteCommandAction.runWriteCommandAction(project) {
                                        document.deleteString(insertedStartOffset, insertedEndOffset)
                                    }
                                    inlay?.dispose()
                                    println("Reject clicked")
                                }
                            }
                        }
                    }
                }

                override fun mouseMoved(e: MouseEvent) {
                    val point = e.point
                    val overButton = buttonBounds.any { it.second.contains(point) }
                    editor.contentComponent.cursor =
                        if (overButton) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        else Cursor.getDefaultCursor()
                }
            }

            editor.contentComponent.addMouseListener(mouseAdapter)
            editor.contentComponent.addMouseMotionListener(mouseAdapter)
        }
    }

}