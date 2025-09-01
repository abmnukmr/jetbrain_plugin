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
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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
        var language = psiFile?.language?.id ?: "Unknown"
        val extension = psiFile?.virtualFile?.extension?: "Unknown"
        if(language.trim() == "Unknown" || language.trim() == "TEXT"){
            language = extension
        }

        val selectionModel = editor.selectionModel
        val selectedText = if (selectionModel.hasSelection()) selectionModel.selectedText else null

        return CodeContext(fileName, language, fullText, caretOffset, selectedText)
    }

    fun getTextBeforeCaret(editor: Editor, maxLines: Int = 60): String {
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val caretLine = document.getLineNumber(caretOffset)

        // Get start line ensuring we don't go negative
        val startLine = (caretLine - maxLines).coerceAtLeast(0)
        val startOffset = document.getLineStartOffset(startLine)

        return document.getText(TextRange(startOffset, caretOffset))
    }
    fun getTextAfterCaret(editor: Editor, maxLines: Int = 20): String {
        val document = editor.document
        if (document.lineCount == 0) return "" // empty document

        val caretOffset = editor.caretModel.offset.coerceAtMost(document.textLength)
        val caretLine = document.getLineNumber(caretOffset)

        val endLine = (caretLine + maxLines).coerceAtMost(document.lineCount - 1)
        val endOffset = document.getLineEndOffset(endLine)

        return document.getText(TextRange(caretOffset, endOffset))
    }




}