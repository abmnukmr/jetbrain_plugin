
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange


class CodeBlockEditor(private val project: Project) {


    fun readCodeAroundCaret(project: Project): String? {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
        val document = editor.document
        val caretLine = document.getLineNumber(editor.caretModel.offset)

        // Define range: 4 lines above and 4 lines below
        val startLine = (caretLine).coerceAtLeast(0)
        val endLine = (caretLine).coerceAtMost(document.lineCount - 1)

        val startOffset = document.getLineStartOffset(startLine)
        val endOffset = document.getLineEndOffset(endLine)

        return document.getText(TextRange(startOffset, endOffset)).trim()
    }


}
