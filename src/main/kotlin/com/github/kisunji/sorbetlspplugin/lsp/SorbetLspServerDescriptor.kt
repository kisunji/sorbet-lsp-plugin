package com.github.kisunji.sorbetlspplugin.lsp

import com.github.kisunji.sorbetlspplugin.settings.SorbetSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.bundler.BundlerGemInfrastructure
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil
import java.io.File

class SorbetLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Sorbet") {

    companion object {
        private val LOG = Logger.getInstance(SorbetLspServerDescriptor::class.java)
    }

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType == RubyFileType.RUBY
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

        LOG.info("Sorbet LSP command: ${commandLine.commandLineString}")
        LOG.info("Working directory: ${workDir.absolutePath}")
        return commandLine
    }

    private fun createCustomCommand(customPath: String): GeneralCommandLine {
        LOG.info("Using custom Sorbet command: $customPath")

        // If custom path contains spaces, use shell execution for complex commands
        return if (customPath.contains(" ")) {
            GeneralCommandLine("/bin/sh", "-c", "$customPath tc --lsp --dir=.")
        } else {
            GeneralCommandLine(customPath, "tc", "--lsp", "--dir=.")
        }
    }

    private fun createBundlerCommand(): GeneralCommandLine {
        val bundleExe = findExecutableInPath("bundle") ?: "bundle"
        LOG.info("Using bundler: $bundleExe")
        return GeneralCommandLine(bundleExe, "exec", "srb", "tc", "--lsp", "--dir=.")
    }

    private fun getWorkingDirectory(): File {
        val basePath = project.basePath ?: return File(".")

        // Try to use Gemfile parent directory (Ruby plugin integration)
        val gemfile = findGemfile()
        return gemfile?.parent?.let { File(it.path) } ?: File(basePath)
    }

    private fun findGemfile(): VirtualFile? {
        // Try to find Gemfile using Ruby plugin's BundlerUtil
        val baseDir = project.baseDir
        if (baseDir != null) {
            val module = ModuleUtilCore.findModuleForFile(baseDir, project)
            module?.let {
                val gemfile = BundlerUtil.getGemfile(it)
                if (gemfile != null) return gemfile
            }
        }

        // Fallback: look for Gemfile in project base directory
        return baseDir?.findChild("Gemfile")
    }

    private fun tryCreateRubyGemExecution(file: VirtualFile): RubyGemExecutionContext? {
        val module = ModuleUtilCore.findModuleForFile(file, project) ?: return null
        val gemfile = BundlerUtil.getGemfile(module) ?: return null

        // Check if bundle install needs to be run
        if (BundlerGemInfrastructure.hasMissingGems(gemfile)) {
            LOG.warn("Bundle install needs to be run - missing gems detected")
            return null
        }

        return RubyGemExecutionContext.tryCreate(null, module, "sorbet")
            ?.withWorkingDir(gemfile.parent)
            ?.withGemScriptName("srb")
            ?.withArguments(listOf("tc", "--lsp", "--dir=."))
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

    private fun findExecutableInPath(name: String): String? {
        return PathEnvironmentVariableUtil.findInPath(name)?.absolutePath
    }
}
