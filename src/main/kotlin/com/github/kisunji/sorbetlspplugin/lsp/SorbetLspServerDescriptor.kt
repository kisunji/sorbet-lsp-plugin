package com.github.kisunji.sorbetlspplugin.lsp

import com.github.kisunji.sorbetlspplugin.settings.SorbetSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType
import java.io.File

class SorbetLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Sorbet") {

    override fun isSupportedFile(file: VirtualFile): Boolean {
        // Check if it's a Ruby file by extension or file type
        return file.extension == "rb" || file.fileType == RubyFileType.RUBY
    }

    override fun createCommandLine(): GeneralCommandLine {
        val settings = SorbetSettings.getInstance(project)

        // Use custom command if specified, otherwise always use bundler
        val commandLine = if (settings.state.customSorbetPath.isNotEmpty()) {
            createCustomCommand(settings.state.customSorbetPath)
        } else {
            createBundlerCommand()
        }

        // Add Sorbet-specific flags
        if (settings.state.disableWatchman) {
            commandLine.addParameter("--disable-watchman")
        } else if (settings.state.watchmanPath.isNotEmpty()) {
            commandLine.addParameter("--watchman-path=${settings.state.watchmanPath}")
        }

        if (settings.state.additionalServerFlags.isNotEmpty()) {
            val flags = settings.state.additionalServerFlags.split(" ").filter { it.isNotEmpty() }
            commandLine.addParameters(flags)
        }

        // Use Gemfile parent as working directory if available
        val workDir = getWorkingDirectory()
        commandLine.withWorkDirectory(workDir)
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

        LOG.info("Working directory: ${workDir.absolutePath}")
        return commandLine
    }

    private fun createCustomCommand(customPath: String): GeneralCommandLine {
        // If custom path contains spaces, use shell execution for complex commands
        return if (customPath.contains(" ")) {
            GeneralCommandLine("/bin/sh", "-c", "$customPath tc --lsp --dir=.")
        } else {
            GeneralCommandLine(customPath, "tc", "--lsp", "--dir=.")
        }
    }

    private fun createBundlerCommand(): GeneralCommandLine {
        return GeneralCommandLine("bundle", "exec", "srb", "tc", "--lsp", "--dir=.")
    }

    private fun getWorkingDirectory(): File {
        val basePath = project.basePath ?: return File(".")

        // Use Gemfile parent directory if available, otherwise use project base
        @Suppress("DEPRECATION")
        val gemfile = project.baseDir?.findChild("Gemfile")
        return gemfile?.parent?.let { File(it.path) } ?: File(basePath)
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
}
