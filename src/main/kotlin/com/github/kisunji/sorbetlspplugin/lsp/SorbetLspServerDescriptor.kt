package com.github.kisunji.sorbetlspplugin.lsp

import com.github.kisunji.sorbetlspplugin.settings.SorbetSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType

class SorbetLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Sorbet") {

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType == RubyFileType.RUBY
    }

    override fun createCommandLine(): GeneralCommandLine {
        val settings = SorbetSettings.getInstance(project)

        val commandLine = if (settings.state.customSorbetPath.isNotEmpty()) {
            createCustomCommand(settings.state.customSorbetPath)
        } else {
            createBundlerCommand()
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

        // TODO: There's a chance a project has multiple roots
        val rootPath = roots[0].toNioPathOrNull()
        commandLine.withWorkingDirectory(rootPath)
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

        LOG.info("Working directory: $rootPath")
        return commandLine
    }

    private fun createCustomCommand(customPath: String): GeneralCommandLine {
        return GeneralCommandLine(customPath.split(" "))
    }

    private fun createBundlerCommand(): GeneralCommandLine {
        return GeneralCommandLine("bundle", "exec", "srb", "tc", "--lsp", "--dir=.")
    }

    override fun createInitializationOptions(): Any {
        val settings = SorbetSettings.getInstance(project)
        return mapOf(
            "highlightUntyped" to settings.state.highlightUntyped,
            "enableTypedFalseCompletionNudges" to settings.state.enableCompletionNudges,
            "supportsOperationNotifications" to true,
            "supportsSorbetURIs" to true,
            // DiagnosticSeverity: 1=Error, 2=Warning, 3=Information, 4=Hint
            "highlightUntypedDiagnosticSeverity" to if (settings.state.highlightUntyped) 3 else null
        )
    }

    override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient {
        val client = SorbetLspClient(handler)
        client.setProject(project)
        return client
    }
}
