package com.github.kisunji.sorbetlspplugin.ui

import com.github.kisunji.sorbetlspplugin.lsp.SorbetOperationStatus
import com.github.kisunji.sorbetlspplugin.settings.SorbetSettingsConfigurable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class SorbetStatusBarWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = "SorbetOperationStatus"

    override fun getDisplayName(): String = "Sorbet Operation Status"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget {
        return SorbetStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    private class SorbetStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

        @Volatile
        private var statusBar: StatusBar? = null
        private val updateListener: () -> Unit = {
            ApplicationManager.getApplication().invokeLater {
                statusBar?.updateWidget(ID())
            }
        }

        override fun ID(): String = "SorbetOperationStatus"

        override fun install(statusBar: StatusBar) {
            this.statusBar = statusBar
            SorbetOperationStatus.getInstance(project).addListener(updateListener)
        }

        override fun dispose() {
            SorbetOperationStatus.getInstance(project).removeListener(updateListener)
            statusBar = null
        }

        override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

        override fun getText(): String {
            val operationStatus = SorbetOperationStatus.getInstance(project)
            val operation = operationStatus.getCurrentOperation()

            return operation?.description ?: "Sorbet: Idle"
        }

        override fun getTooltipText(): String {
            val operationStatus = SorbetOperationStatus.getInstance(project)
            val operation = operationStatus.getCurrentOperation()

            return when {
                operation == null -> "Sorbet LSP is idle. Click to open settings."
                else -> "Sorbet: ${operation.description}\nClick to open settings."
            }
        }

        override fun getClickConsumer(): Consumer<MouseEvent> {
            return Consumer {
                ShowSettingsUtil.getInstance().showSettingsDialog(
                    project,
                    SorbetSettingsConfigurable::class.java
                )
            }
        }

        override fun getAlignment(): Float = 0.0f
    }
}
