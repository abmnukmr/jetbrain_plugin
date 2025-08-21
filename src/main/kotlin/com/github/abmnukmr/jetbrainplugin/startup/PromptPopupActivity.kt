package com.github.abmnukmr.jetbrainplugin.startup

import com.github.abmnukmr.jetbrainplugin.intellisence.AcceptGhostTextAction
import com.github.abmnukmr.jetbrainplugin.intellisence.GhostCodeManager
import com.github.abmnukmr.jetbrainplugin.intellisence.GhostTextManager
import com.github.abmnukmr.jetbrainplugin.listener.SelectionGutterListener
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.TextRange
import java.awt.Color

class PromptPopupActivity: ProjectActivity {

    override suspend fun execute(project: Project) {

        EditorFactory.getInstance().eventMulticaster.addSelectionListener(SelectionGutterListener(project), project)

    }

}