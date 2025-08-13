package com.github.abmnukmr.jetbrainplugin.startup

import com.github.abmnukmr.jetbrainplugin.listener.SelectionSuggestListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        thisLogger().info("Registering selection popup listener")

        val listener = object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor = event.editor
                if (editor.project == project) {
                    editor.selectionModel.addSelectionListener(SelectionSuggestListener(project))
                }
            }
        }

        EditorFactory.getInstance().addEditorFactoryListener(listener, project)
    }
}
