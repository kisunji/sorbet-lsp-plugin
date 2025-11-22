package com.github.kisunji.sorbetlspplugin.lsp

import com.github.kisunji.sorbetlspplugin.settings.SorbetSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import java.io.File

class SorbetLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Sorbet") {

    companion object {
        private val LOG = Logger.getInstance(SorbetLspServerDescriptor::class.java)
    }

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.extension == "rb" && isSorbetProject()
    }

    override fun createCommandLine(): GeneralCommandLine {
        val settings = SorbetSettings.getInstance(project)

        val commandLine = when {
            settings.state.customSorbetPath.isNotEmpty() -> {
                LOG.info("Using custom Sorbet path: ${settings.state.customSorbetPath}")
                GeneralCommandLine(settings.state.customSorbetPath, "tc", "--lsp", "--dir=.")
            }
            isUsingBundler() -> {
                val bundleExe = findExecutableInPath("bundle")
                LOG.info("Using bundler: $bundleExe")
                GeneralCommandLine(bundleExe, "exec", "srb", "tc", "--lsp", "--dir=.")
            }
            else -> {
                val srbExe = findExecutableInPath("srb")
                LOG.info("Using srb from PATH: $srbExe")
                GeneralCommandLine(srbExe, "tc", "--lsp", "--dir=.")
            }
        }

        if (settings.state.disableWatchman) {
            commandLine.addParameter("--disable-watchman")
        } else if (settings.state.watchmanPath.isNotEmpty()) {
            commandLine.addParameter("--watchman-path=${settings.state.watchmanPath}")
        }

        if (settings.state.additionalServerFlags.isNotEmpty()) {
            val flags = settings.state.additionalServerFlags.split(" ").filter { it.isNotEmpty() }
            commandLine.addParameters(flags)
        }

        val workDir = File(project.basePath ?: "")
        commandLine.withWorkDirectory(workDir)
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

        LOG.info("Command: ${commandLine.commandLineString}")
        return commandLine
    }

    override fun createInitializationOptions(): Any {
        val settings = SorbetSettings.getInstance(project)
        return mapOf(
            "highlightUntyped" to settings.state.highlightUntyped,
            "enableTypedFalseCompletionNudges" to settings.state.enableCompletionNudges,
            "supportsOperationNotifications" to settings.state.enableOperationNotifications,
            "supportsSorbetURIs" to true,
            "highlightUntypedDiagnosticSeverity" to if (settings.state.highlightUntyped) "everywhere" else "none"
        )
    }

    override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient {
        val client = SorbetLspClient(handler)
        client.setProject(project)
        return client
    }

    private fun findExecutableInPath(name: String): String {
        PathEnvironmentVariableUtil.findInPath(name)?.let { return it.absolutePath }

        LOG.warn("$name not found in PATH")
        return name
    }

    private fun isSorbetProject(): Boolean {
        val basePath = project.basePath ?: return false

        if (File(basePath, "sorbet/config").exists()) return true

        val gemfile = File(basePath, "Gemfile")
        return gemfile.exists() && gemfile.readText().contains("sorbet")
    }

    private fun isUsingBundler(): Boolean {
        val basePath = project.basePath ?: return false
        val gemfile = File(basePath, "Gemfile")
        return gemfile.exists() && gemfile.readText().contains("sorbet")
    }
}
