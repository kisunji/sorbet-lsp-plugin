package com.github.kisunji.sorbetlspplugin.lsp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification

class SorbetLspClient(
    notificationsHandler: LspServerNotificationsHandler
) : Lsp4jClient(notificationsHandler) {

    companion object {
        private val LOG = Logger.getInstance(SorbetLspClient::class.java)
        private val gson = Gson()
    }

    private var _project: Project? = null

    fun setProject(project: Project) {
        _project = project
    }

    @JsonNotification("sorbet/showOperation")
    fun showOperation(params: Any?) {
        try {
            val json = when (params) {
                is JsonObject -> params
                else -> gson.toJsonTree(params).asJsonObject
            }

            val operationName = json.get("operationName")?.asString ?: "Unknown"
            val description = json.get("description")?.asString ?: ""
            val status = json.get("status")?.asString ?: ""

            LOG.info("Operation: $operationName ($status)")

            _project?.let {
                SorbetLogService.getInstance(it).log(
                    LogDirection.INCOMING,
                    "sorbet/showOperation",
                    "operationName=$operationName, description=$description, status=$status"
                )
                SorbetOperationStatus.getInstance(it).updateOperation(operationName, description, status)
            }
        } catch (e: Exception) {
            LOG.error("Failed to handle showOperation", e)
        }
    }

    override fun publishDiagnostics(params: PublishDiagnosticsParams) {
        _project?.let {
            SorbetLogService.getInstance(it).log(
                LogDirection.INCOMING,
                "textDocument/publishDiagnostics",
                "uri=${params.uri}, diagnostics=${params.diagnostics.size}"
            )
        }
        super.publishDiagnostics(params)
    }

    override fun showMessage(params: MessageParams) {
        _project?.let {
            SorbetLogService.getInstance(it).log(
                LogDirection.INCOMING,
                "window/showMessage",
                "[${params.type}] ${params.message}"
            )
        }
        super.showMessage(params)
    }

    override fun logMessage(params: MessageParams) {
        _project?.let {
            SorbetLogService.getInstance(it).log(
                LogDirection.INCOMING,
                "window/logMessage",
                "[${params.type}] ${params.message}"
            )
        }
        super.logMessage(params)
    }

    override fun notifyProgress(params: ProgressParams) {
        _project?.let {
            val value = params.value
            SorbetLogService.getInstance(it).log(
                LogDirection.INCOMING,
                "$/progress",
                "token=${params.token}, value=$value"
            )
        }
        super.notifyProgress(params)
    }
}
