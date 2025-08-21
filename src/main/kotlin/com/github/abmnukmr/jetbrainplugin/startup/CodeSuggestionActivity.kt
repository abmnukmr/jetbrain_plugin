package com.github.abmnukmr.jetbrainplugin.startup

import com.github.abmnukmr.jetbrainplugin.intellisence.AcceptGhostTextAction
import com.github.abmnukmr.jetbrainplugin.intellisence.GhostCodeManager
import com.github.abmnukmr.jetbrainplugin.intellisence.GhostTextManager
import com.github.abmnukmr.jetbrainplugin.services.MyHttpClient
import com.github.abmnukmr.jetbrainplugin.services.ReadWrite
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import groovy.json.StringEscapeUtils
import java.awt.Color

class CodeSuggestionActivity : ProjectActivity {


    var codeToWrite = "";
    override suspend fun execute(project: Project) {
        val actionManager = ActionManager.getInstance()
        val tabAction = actionManager.getAction(IdeActions.ACTION_EDITOR_TAB) as EditorAction

        tabAction.setupHandler(object : EditorActionHandler() {
            override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
                ReadWrite().insertTextAtCaret(project,editor,codeToWrite)
            }
        })

        val client = MyHttpClient(project)

        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val editor = event.editor
                val ghostManager = GhostCodeManager(editor)
                val manager = editor.getUserData(AcceptGhostTextAction.KEY)
                    ?: GhostTextManager(editor).also {
                        editor.putUserData(AcceptGhostTextAction.KEY, it)
                    }

                val textBeforeCaret = getTextBeforeCaret(editor)
                val textAfterCaret = getTextAfterCaret(editor)
                val caretOffset = editor.caretModel.offset
                val document = editor.document
                val lineNumber = document.getLineNumber(caretOffset)
                val lineStart = document.getLineStartOffset(lineNumber)
                val lineEnd = document.getLineEndOffset(lineNumber)
                val lineText = document.getText(TextRange(lineStart, lineEnd))
                val psiFile: PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(document)
                val language = psiFile?.language?.id ?: "Unknown"

                if (lineText.isBlank() || lineText.contains("=")) {
                    // Call your HTTP client
                    client.fetchCompletionAsync(
                        codeBeforeCaret = textBeforeCaret,
                        codeAfterCaret = textAfterCaret,
                        query = "",
                        language = language,
                        onSuccess = { completionResponse ->
                            codeToWrite = completionResponse.completion
                                .trim()                     // remove leading/trailing whitespace
                                .removePrefix("\"")         // remove leading quote
                                .removeSuffix("\"")         // remove trailing quote
                                .let { StringEscapeUtils.unescapeJava(it) }  // handle \n, \"
                                .replaceFirst("^```[a-zA-Z0-9]*\\s*".toRegex(), "")  // remove opening fence
                                .replaceFirst("```$".toRegex(RegexOption.MULTILINE), "") // remove closing fence
                            ;
                            val segment = GhostCodeManager.GhostSegment(codeToWrite, Color.GRAY)

                            ghostManager.showCodeGhostText(listOf(segment))

                        },
                        onError = { error ->
                            println("Error fetching completion: $error")

                        }
                    )
                } else {
                    ApplicationManager.getApplication().invokeLater {
                        manager.clearAllGhostText()
                    }
                }
            }

        }, project)
    }

    private fun getTextBeforeCaret(editor: Editor, maxLines: Int = 60): String {
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val caretLine = document.getLineNumber(caretOffset)

        // Get start line ensuring we don't go negative
        val startLine = (caretLine - maxLines).coerceAtLeast(0)
        val startOffset = document.getLineStartOffset(startLine)

        return document.getText(TextRange(startOffset, caretOffset))
    }
    private fun getTextAfterCaret(editor: Editor, maxLines: Int = 20): String {
        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val caretLine = document.getLineNumber(caretOffset)

        val endLine = (caretLine + maxLines).coerceAtMost(document.lineCount - 1)
        val endOffset = document.getLineEndOffset(endLine)

        return document.getText(TextRange(caretOffset, endOffset))
    }

}
