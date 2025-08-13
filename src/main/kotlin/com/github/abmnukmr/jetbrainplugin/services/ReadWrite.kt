package com.github.abmnukmr.jetbrainplugin.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.EditorMouseAdapter
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.charset.StandardCharsets
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingUtilities

class ReadWrite {


    fun writeToAbsolutePath(absolutePath: String, content: String) {
        val file = File(absolutePath)

        val virtualFile: VirtualFile? = LocalFileSystem.getInstance()
            .refreshAndFindFileByIoFile(file)

        if (virtualFile != null && virtualFile.exists()) {
            // ✅ Overwrite content if file exists
            WriteAction.run<Throwable> {
                virtualFile.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
            }
        } else {
            // ❌ File does not exist — optionally create it
            val parentFile = file.parentFile
            val parentVirtual = LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(parentFile)

            if (parentVirtual != null) {
                WriteAction.run<Throwable> {
                    val created = parentVirtual.createChildData(null, file.name)
                    created.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
                }
            } else {
                println("⚠️ Could not find or create parent directory: $absolutePath")
            }
        }

    }

    fun readFileFromAbsolutePath(absolutePath: String): String? {
        val file = File(absolutePath)
        val virtualFile: VirtualFile? = LocalFileSystem.getInstance()
            .refreshAndFindFileByIoFile(file)

        return if (virtualFile != null && virtualFile.exists()) {
            val byteContent = virtualFile.contentsToByteArray()
            String(byteContent, StandardCharsets.UTF_8)
        } else {
            println("⚠️ File not found: $absolutePath")
            null
        }
    }





    fun insertSuggestionWithClickHandlers(project: Project, suggestionText: String) {
        ApplicationManager.getApplication().invokeLater {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@invokeLater
            val document = editor.document
            val caretOffset = editor.caretModel.offset

            val buttonsText = " Accept Reject"
            val fullText = "$suggestionText$buttonsText\n"

            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(caretOffset, fullText)
            }

            // Move caret
            editor.caretModel.moveToOffset(caretOffset + fullText.length)

            // Highlight buttons
            val lineNumber = document.getLineNumber(caretOffset)
            val lineStart = document.getLineStartOffset(lineNumber)
            val lineEnd = document.getLineEndOffset(lineNumber)
            val lineText = document.getText(TextRange(lineStart, lineEnd))

            highlightButtons(editor, lineStart, lineText)

            // Add click listener
            registerAcceptRejectClickHandler(editor)
        }
    }

    fun highlightButtons(editor: Editor, lineStart: Int, lineText: String) {
        val markupModel = editor.markupModel

        val acceptIndex = lineText.indexOf("Accept")
        val rejectIndex = lineText.indexOf("Reject")

        if (acceptIndex != -1) {
            val start = lineStart + acceptIndex
            val end = start + "Accept".length
            val attr = TextAttributes().apply {
                //foregroundColor = Color(0, 128, 0) // Green
                fontType = Font.PLAIN
                backgroundColor =Color(0, 128, 0)


            }
            markupModel.addRangeHighlighter(
                start, end, HighlighterLayer.SELECTION - 1, attr, HighlighterTargetArea.EXACT_RANGE
            )
        }

        if (rejectIndex != -1) {
            val start = lineStart + rejectIndex
            val end = start + "Reject".length
            val attr = TextAttributes().apply {
                //foregroundColor = Color(200, 0, 0) // Red
                fontType = Font.PLAIN
                backgroundColor = Color(200, 0, 0) // Red

            }
            markupModel.addRangeHighlighter(
                start, end, HighlighterLayer.SELECTION - 1, attr, HighlighterTargetArea.EXACT_RANGE
            )
        }
    }

    fun registerAcceptRejectClickHandler(editor: Editor) {
        editor.addEditorMouseListener(object : EditorMouseAdapter() {
            override fun mouseClicked(e: EditorMouseEvent) {
                val point = e.mouseEvent.point
                val logicalPos: LogicalPosition = editor.xyToLogicalPosition(point)
                val offset = editor.logicalPositionToOffset(logicalPos)

                val document = editor.document
                val lineNumber = logicalPos.line
                if (lineNumber >= document.lineCount) return

                val lineStart = document.getLineStartOffset(lineNumber)
                val lineEnd = document.getLineEndOffset(lineNumber)
                val lineText = document.getText(TextRange(lineStart, lineEnd))

                val acceptIndex = lineText.indexOf("Accept")
                val rejectIndex = lineText.indexOf("Reject")

                println("🔍 Offset: $offset")
                println("🔍 Line: $lineText")
                println("🔍 Accept at: $acceptIndex, Reject at: $rejectIndex")

                if (acceptIndex == -1 || rejectIndex == -1) return

                val acceptStart = lineStart + acceptIndex
                val acceptEnd = acceptStart + "Accept".length
                val rejectStart = lineStart + rejectIndex
                val rejectEnd = rejectStart + "Reject".length

                val clickedInAccept = offset in (acceptStart - 1)..(acceptEnd + 1)
                val clickedInReject = offset in (rejectStart - 1)..(rejectEnd + 1)

                if (clickedInAccept || clickedInReject) {
                    WriteCommandAction.runWriteCommandAction(editor.project) {
                        var deleteStart = acceptStart
                        if (document.charsSequence.getOrNull(acceptStart - 1) == ' ') {
                            deleteStart -= 1
                        }
                        document.deleteString(deleteStart, rejectEnd)
                    }
                }
            }
        })
    }
}