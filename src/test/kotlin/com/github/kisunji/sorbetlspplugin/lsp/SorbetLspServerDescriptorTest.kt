package com.github.kisunji.sorbetlspplugin.lsp

import com.github.kisunji.sorbetlspplugin.settings.SorbetSettings
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito
import java.io.File

class SorbetLspServerDescriptorTest : BasePlatformTestCase() {

    private lateinit var descriptor: SorbetLspServerDescriptor
    private lateinit var settings: SorbetSettings

    override fun setUp() {
        super.setUp()
        descriptor = SorbetLspServerDescriptor(project)
        settings = SorbetSettings.getInstance(project)
        resetSettings()
    }

    override fun tearDown() {
        try {
            val projectDir = project.basePath
            if (projectDir != null) {
                File(projectDir, "Gemfile").delete()
                File(projectDir, "sorbet/config").delete()
                File(projectDir, "sorbet").delete()
            }
            resetSettings()
        } finally {
            super.tearDown()
        }
    }

    private fun resetSettings() {
        settings.state.customSorbetPath = ""
        settings.state.highlightUntyped = true
        settings.state.enableCompletionNudges = true
        settings.state.enableOperationNotifications = true
        settings.state.watchmanPath = ""
        settings.state.disableWatchman = false
        settings.state.additionalServerFlags = ""
    }

    fun testIsSupportedFile() {
        val projectDir = project.basePath ?: return
        val configDir = File(projectDir, "sorbet")
        configDir.mkdirs()
        val configFile = File(configDir, "config")
        configFile.writeText(".")

        try {
            val rubyFile = myFixture.addFileToProject("test.rb", "# ruby file")
            assertTrue(descriptor.isSupportedFile(rubyFile.virtualFile))

            val javaFile = myFixture.addFileToProject("Test.java", "// java file")
            assertFalse(descriptor.isSupportedFile(javaFile.virtualFile))

            val kotlinFile = myFixture.addFileToProject("Test.kt", "// kotlin file")
            assertFalse(descriptor.isSupportedFile(kotlinFile.virtualFile))
        } finally {
            configFile.delete()
            configDir.delete()
        }
    }

    fun testCommandLineWithBundleExec() {
        val projectDir = project.basePath ?: return

        val gemfilePath = File(projectDir, "Gemfile")
        gemfilePath.parentFile?.mkdirs()
        gemfilePath.writeText("source 'https://rubygems.org'\ngem 'sorbet'")

        try {
            val commandLine = descriptor.createCommandLine()
            val command = commandLine.commandLineString

            assertTrue(
                "Expected bundle exec srb, got: $command",
                command.contains("bundle") && command.contains("exec") && command.contains("srb")
            )
            assertTrue("Expected tc flag", command.contains("tc"))
            assertTrue("Expected --lsp flag", command.contains("--lsp"))
        } finally {
            gemfilePath.delete()
        }
    }

    fun testCommandLineWithCustomPath() {
        settings.state.customSorbetPath = "/custom/path/to/srb"

        val commandLine = descriptor.createCommandLine()
        val command = commandLine.commandLineString

        assertTrue("Expected custom path in command", command.contains("/custom/path/to/srb"))
        assertTrue("Expected tc flag", command.contains("tc"))
        assertTrue("Expected --lsp flag", command.contains("--lsp"))
    }

    fun testCommandLineWithWatchmanEnabled() {
        settings.state.disableWatchman = false
        settings.state.watchmanPath = "/usr/local/bin/watchman"

        val commandLine = descriptor.createCommandLine()
        val command = commandLine.commandLineString

        assertFalse("Should not contain --disable-watchman", command.contains("--disable-watchman"))
        assertTrue(
            "Expected watchman path in command",
            command.contains("--watchman-path=/usr/local/bin/watchman")
        )
    }

    fun testCommandLineWithWatchmanDisabled() {
        settings.state.disableWatchman = true

        val commandLine = descriptor.createCommandLine()
        val command = commandLine.commandLineString

        assertTrue("Expected --disable-watchman flag", command.contains("--disable-watchman"))
    }

    fun testCommandLineWithAdditionalFlags() {
        settings.state.additionalServerFlags = "--enable-experimental-lsp-hover"

        val commandLine = descriptor.createCommandLine()
        val command = commandLine.commandLineString

        assertTrue(
            "Expected additional flags in command",
            command.contains("--enable-experimental-lsp-hover")
        )
    }

    fun testInitializationOptions() {
        settings.state.highlightUntyped = true
        settings.state.enableCompletionNudges = false
        settings.state.enableOperationNotifications = true

        val options = descriptor.createInitializationOptions() as Map<*, *>

        assertEquals(true, options["highlightUntyped"])
        assertEquals(false, options["enableTypedFalseCompletionNudges"])
        assertEquals(true, options["supportsOperationNotifications"])
        assertEquals(true, options["supportsSorbetURIs"])
        assertEquals("everywhere", options["highlightUntypedDiagnosticSeverity"])
    }

    fun testInitializationOptionsWithHighlightUntypedDisabled() {
        settings.state.highlightUntyped = false

        val options = descriptor.createInitializationOptions() as Map<*, *>

        assertEquals(false, options["highlightUntyped"])
        assertEquals("none", options["highlightUntypedDiagnosticSeverity"])
    }

    fun testLsp4jClientCreation() {
        val notificationsHandler = Mockito.mock(LspServerNotificationsHandler::class.java)
        val client = descriptor.createLsp4jClient(notificationsHandler)

        assertNotNull(client)
        assertTrue(client is SorbetLspClient)
    }

    fun testCommandLineWorkingDirectory() {
        val commandLine = descriptor.createCommandLine()
        val workingDir = commandLine.workingDirectory

        assertNotNull(workingDir)
        val expectedPath = File(project.basePath ?: "").canonicalPath
        val actualPath = File(workingDir.toString()).canonicalPath
        assertEquals(expectedPath, actualPath)
    }
}
