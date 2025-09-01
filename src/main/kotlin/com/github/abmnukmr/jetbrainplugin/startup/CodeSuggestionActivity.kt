package com.github.abmnukmr.jetbrainplugin.startup

import com.github.abmnukmr.jetbrainplugin.EditorController.CodeContext
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
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import groovy.json.StringEscapeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Color

class CodeSuggestionActivity : ProjectActivity {

    var codeToWrite = "";
    var debounce = true;
    var textLeft = ""
    override suspend fun execute(project: Project) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return



        val ghostManager = GhostCodeManager(editor)
        val manager = editor.getUserData(AcceptGhostTextAction.KEY)
            ?: GhostTextManager(editor).also {
                editor.putUserData(AcceptGhostTextAction.KEY, it)
            }

        val textManager = GhostTextManager(editor);

        val actionManager = ActionManager.getInstance()

        val tabAction = actionManager.getAction(IdeActions.ACTION_EDITOR_TAB) as EditorAction
        val escAction  = actionManager.getAction(IdeActions.ACTION_EDITOR_ESCAPE) as EditorAction

        val originalHandler = tabAction.handler
        tabAction.setupHandler(object : EditorActionHandler() {

            override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
                debounce = false;
                if(!codeToWrite.isBlank()) {
                    ReadWrite().insertTextAtCaret(project, editor, "$codeToWrite \n")

                    codeToWrite = ""
                    CoroutineScope(Dispatchers.Default).launch {
                      //  delay(1000)
                        debounce = true
                    }
                }
                else {
                    // Fallback to normal Tab behavior
                    originalHandler.execute(editor, caret, dataContext)
                }
            }
        })

        escAction.setupHandler(object : EditorActionHandler() {

            override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
                debounce = false;
                ghostManager.clearAllGhostText()
                codeToWrite=""
                CoroutineScope(Dispatchers.Default).launch {
                   // delay(1000)
                    debounce =true
                }
            }
        })



        val client = MyHttpClient(project)




        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                ghostManager.clearAllGhostText();

                val document = event.document
                val editors = EditorFactory.getInstance().getEditors(document, project)
                val editor = editors.firstOrNull() ?: return

                val textBeforeCaret = CodeContext().getTextBeforeCaret(editor)
                val textAfterCaret = CodeContext().getTextAfterCaret(editor)
                val caretOffset = editor.caretModel.offset
                val lineNumber = document.getLineNumber(caretOffset)
                val lineStart = document.getLineStartOffset(lineNumber)
                val lineEnd = document.getLineEndOffset(lineNumber)
                val lineText = document.getText(TextRange(lineStart, lineEnd))
                val psiFile: PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(document)
                val language = psiFile?.language?.id ?: "Unknown"
                textLeft = document.getText(TextRange(lineStart, caretOffset))
                val textRight = document.getText(TextRange(caretOffset, lineEnd))
                val offset = editor.caretModel.offset
                val text = editor.document.charsSequence

                val isInMiddleOfWord = offset > 0 &&
                        offset < text.length &&
                        Character.isJavaIdentifierPart(text[offset - 1]) && // left is part of word
                        Character.isJavaIdentifierPart(text[offset])
                if (debounce && !isInMiddleOfWord) {
                    debounce = false;
                    // Call your HTTP client
                    client.fetchCompletionAsync(
                        codeBeforeCaret = textBeforeCaret,
                        codeAfterCaret = textAfterCaret,
                        query = "Generate the next logical code snippet at the caret, following $language idiomatic style.",
                        language = language,
                        onSuccess = { completionResponse ->
                            codeToWrite = completionResponse.completion
                                .trim()
                                .removePrefix("\"")
                                .removeSuffix("\"")
                                .let { StringEscapeUtils.unescapeJava(it) }
                                // remove triple backtick + optional language
                                .replaceFirst("^```[\\w-]*\\s*".toRegex(RegexOption.MULTILINE), "")
                                .replaceFirst("```\\s*$".toRegex(RegexOption.MULTILINE), "")
                                // remove any leading/trailing backticks (` or ````)
                                .replaceFirst("^`+".toRegex(), "")
                                .replaceFirst("`+$".toRegex(), "")

                            codeToWrite = codeToWrite.removePrefix(textLeft.takeIf { codeToWrite.startsWith(it) } ?: "")

                            if(textManager.getVisibilityStatus() && !codeToWrite.isBlank() ) textManager.clearAllGhostText()

                            val lineCount = codeToWrite.lines().size

                            val segment = GhostCodeManager.GhostSegment(codeToWrite, Color.GRAY)
                            ghostManager.showCodeGhostCode(listOf(segment))

                            CoroutineScope(Dispatchers.Default).launch {
                               // delay(1000)
                                debounce =true
                            }


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
        },project)
    }



}
