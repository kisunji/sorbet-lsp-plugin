package com.github.kisunji.sorbetlspplugin.ui

import com.github.kisunji.sorbetlspplugin.lsp.SorbetLogService
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class SorbetLogToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val consoleView = TextConsoleBuilderFactory.getInstance()
            .createBuilder(project)
            .console

        SorbetLogService.getInstance(project).setConsoleView(consoleView)

        val toolWindowPanel = SimpleToolWindowPanel(true, true)
        toolWindowPanel.setContent(consoleView.component)

        val actionGroup = DefaultActionGroup()
        actionGroup.add(ClearLogAction(project))

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "SorbetLogToolbar",
            actionGroup,
            true
        )
        toolbar.targetComponent = toolWindowPanel
        toolWindowPanel.toolbar = toolbar.component

        val content = toolWindow.contentManager.factory.createContent(
            toolWindowPanel,
            "",
            false
        )
        toolWindow.contentManager.addContent(content)
    }

    private class ClearLogAction(private val project: Project) : AnAction(
        "Clear",
        "Clear log output",
        AllIcons.Actions.GC
    ), DumbAware {

        override fun actionPerformed(e: AnActionEvent) {
            SorbetLogService.getInstance(project).clear()
        }
    }
}
