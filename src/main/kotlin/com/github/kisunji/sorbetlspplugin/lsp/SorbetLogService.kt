package com.github.kisunji.sorbetlspplugin.lsp

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LogDirection {
    INCOMING,
    OUTGOING
}

@Service(Service.Level.PROJECT)
class SorbetLogService {

    private var consoleView: ConsoleView? = null
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    fun setConsoleView(view: ConsoleView) {
        consoleView = view
    }

    fun log(direction: LogDirection, method: String, content: String? = null) {
        val timestamp = LocalDateTime.now().format(formatter)
        val arrow = when (direction) {
            LogDirection.INCOMING -> "<--"
            LogDirection.OUTGOING -> "-->"
        }

        ApplicationManager.getApplication().invokeLater {
            consoleView?.let { console ->
                console.print("[$timestamp] $arrow $method\n", ConsoleViewContentType.NORMAL_OUTPUT)
                if (!content.isNullOrBlank()) {
                    console.print("$content\n", ConsoleViewContentType.LOG_DEBUG_OUTPUT)
                }
                console.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
            }
        }
    }

    fun logError(method: String, error: String) {
        val timestamp = LocalDateTime.now().format(formatter)

        ApplicationManager.getApplication().invokeLater {
            consoleView?.let { console ->
                console.print("[$timestamp] ERROR $method\n", ConsoleViewContentType.ERROR_OUTPUT)
                console.print("$error\n\n", ConsoleViewContentType.ERROR_OUTPUT)
            }
        }
    }

    fun clear() {
        ApplicationManager.getApplication().invokeLater {
            consoleView?.clear()
        }
    }

    companion object {
        fun getInstance(project: Project): SorbetLogService {
            return project.getService(SorbetLogService::class.java)
        }
    }
}
