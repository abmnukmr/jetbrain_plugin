package com.github.abmnukmr.jetbrainplugin.startup


import com.github.abmnukmr.jetbrainplugin.intellisence.AcceptGhostTextAction
import com.github.abmnukmr.jetbrainplugin.intellisence.GhostTextManager
import com.github.abmnukmr.jetbrainplugin.listener.SelectionSuggestListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiDocumentManager

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        thisLogger().info("Registering selection popup listener")

        val listener = object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor = event.editor
               // if (editor.project == project) {
                    println("listening... ")
                    editor.selectionModel.addSelectionListener(SelectionSuggestListener(project))
                //}
            }
        }


        EditorFactory.getInstance().addEditorFactoryListener(listener, project)





    }
}


