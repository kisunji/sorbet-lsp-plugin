package com.github.kisunji.sorbetlspplugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType

class SorbetLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (file.fileType == RubyFileType.RUBY) {
            serverStarter.ensureServerStarted(SorbetLspServerDescriptor(project))
        }
    }
}
