package com.github.kisunji.sorbetlspplugin.lsp

import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class SorbetLspServerSupportProviderTest : BasePlatformTestCase() {

    private lateinit var provider: SorbetLspServerSupportProvider

    override fun setUp() {
        super.setUp()
        provider = SorbetLspServerSupportProvider()

        val projectDir = project.basePath ?: return
        val configDir = File(projectDir, "sorbet")
        configDir.mkdirs()
        val configFile = File(configDir, "config")
        configFile.writeText(".")
    }

    override fun tearDown() {
        try {
            val projectDir = project.basePath
            if (projectDir != null) {
                File(projectDir, "sorbet/config").delete()
                File(projectDir, "sorbet").delete()
            }
        } finally {
            super.tearDown()
        }
    }

    fun testFileSupport() {
        var started = false
        val mockStarter = object : LspServerSupportProvider.LspServerStarter {
            override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                started = true
                assertTrue(descriptor is SorbetLspServerDescriptor)
            }
        }

        val rubyFile = myFixture.addFileToProject("test.rb", "puts 'hello'")
        provider.fileOpened(project, rubyFile.virtualFile, mockStarter)

        assertTrue("LSP server should have started for .rb file", started)
    }

    fun testNonRubyFileSupport() {
        var started = false
        val mockStarter = object : LspServerSupportProvider.LspServerStarter {
            override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                started = true
            }
        }

        val javaFile = myFixture.addFileToProject("Test.java", "public class Test {}")
        provider.fileOpened(project, javaFile.virtualFile, mockStarter)

        assertFalse("LSP server should not have started for .java file", started)
    }

    fun testRubyExtensions() {
        var started = false
        val mockStarter = object : LspServerSupportProvider.LspServerStarter {
            override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                started = true
            }
        }

        val rbFile = myFixture.addFileToProject("example.rb", "# Ruby")
        provider.fileOpened(project, rbFile.virtualFile, mockStarter)

        assertTrue("LSP server should have started for .rb file", started)
    }

    fun testUnsupportedExtensions() {
        val files = listOf(
            myFixture.addFileToProject("test.kt", "// Kotlin"),
            myFixture.addFileToProject("test.java", "// Java"),
            myFixture.addFileToProject("test.py", "# Python"),
            myFixture.addFileToProject("test.js", "// JavaScript"),
            myFixture.addFileToProject("test.txt", "Text")
        )

        files.forEach { file ->
            var started = false
            val mockStarter = object : LspServerSupportProvider.LspServerStarter {
                override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                    started = true
                }
            }

            provider.fileOpened(project, file.virtualFile, mockStarter)
            assertFalse("LSP server should not have started for ${file.name}", started)
        }
    }

    fun testRakefile() {
        var started = false
        val mockStarter = object : LspServerSupportProvider.LspServerStarter {
            override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                started = true
            }
        }

        val rakefile = myFixture.addFileToProject("Rakefile", "task :default")
        provider.fileOpened(project, rakefile.virtualFile, mockStarter)

        assertFalse("LSP server should not have started for Rakefile", started)
    }

    fun testGemfile() {
        var started = false
        val mockStarter = object : LspServerSupportProvider.LspServerStarter {
            override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                started = true
            }
        }

        val gemfile = myFixture.addFileToProject("Gemfile", "source 'https://rubygems.org'")
        provider.fileOpened(project, gemfile.virtualFile, mockStarter)

        assertFalse("LSP server should not have started for Gemfile", started)
    }

    fun testProjectWithoutSorbetConfig() {
        val projectDir = project.basePath
        if (projectDir != null) {
            File(projectDir, "sorbet/config").delete()
            File(projectDir, "sorbet").delete()
        }

        var started = false
        val mockStarter = object : LspServerSupportProvider.LspServerStarter {
            override fun ensureServerStarted(descriptor: com.intellij.platform.lsp.api.LspServerDescriptor) {
                started = true
            }
        }

        val rubyFile = myFixture.addFileToProject("test2.rb", "puts 'hello'")
        provider.fileOpened(project, rubyFile.virtualFile, mockStarter)

        assertFalse("LSP server should not have started without sorbet/config", started)
    }
}
