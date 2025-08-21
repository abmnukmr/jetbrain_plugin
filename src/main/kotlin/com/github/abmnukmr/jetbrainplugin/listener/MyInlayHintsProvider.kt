package com.github.abmnukmr.jetbrainplugin.hints

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.lang.javascript.psi.JSFunction
import com.jetbrains.python.psi.PyFunction

@Suppress("UnstableApiUsage")
class MyInlayHintsProvider : InlayHintsProvider<NoSettings> {
    override val name: String = "My Function Hints"
    override val key: SettingsKey<NoSettings> = SettingsKey("my.function.hints")
    override val previewText: String = """
        class Demo {
            @RequestMapping("/ping")
            public String ping() { return "Ping!"; }
        }
    """.trimIndent()

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is PsiMethod) {
                    val factory = factory
                    val presentation = factory.smallText("  | fix · Add") // 👈 custom text

                    // Insert right at the end of method signature
                    val offset = element.textRange.endOffset
                    sink.addInlineElement(
                        offset,
                        relatesToPrecedingText = true,
                        presentation = presentation
                    )
                }
                return true
            }
        }
    }
    // 🔹 Must return non-null ImmediateConfigurable
    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable =
        object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener) =
                javax.swing.JPanel() // empty panel (no settings UI for now)
        }
}
