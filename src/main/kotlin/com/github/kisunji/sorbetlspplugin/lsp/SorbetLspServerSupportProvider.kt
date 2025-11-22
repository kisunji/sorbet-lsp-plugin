package com.github.kisunji.sorbetlspplugin.lsp

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import java.io.File

class SorbetLspServerSupportProvider : LspServerSupportProvider {

    companion object {
        private val LOG = Logger.getInstance(SorbetLspServerSupportProvider::class.java)
    }

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (file.extension != "rb") return
        if (!isSorbetProject(project)) return

        LOG.info("Starting Sorbet LSP for ${project.name}")
        serverStarter.ensureServerStarted(SorbetLspServerDescriptor(project))
    }

    private fun isSorbetProject(project: Project): Boolean {
        val basePath = project.basePath ?: return false

        if (File(basePath, "sorbet/config").exists()) return true

        val gemfile = File(basePath, "Gemfile")
        return gemfile.exists() && gemfile.readText().contains("sorbet")
    }
}
