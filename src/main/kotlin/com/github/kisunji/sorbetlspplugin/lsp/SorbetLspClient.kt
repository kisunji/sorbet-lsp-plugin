package com.github.kisunji.sorbetlspplugin.lsp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
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
                SorbetOperationStatus.getInstance(it).updateOperation(operationName, description, status)
            }
        } catch (e: Exception) {
            LOG.error("Failed to handle showOperation", e)
        }
    }
}
