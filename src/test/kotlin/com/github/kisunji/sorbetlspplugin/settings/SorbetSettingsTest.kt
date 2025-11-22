package com.github.kisunji.sorbetlspplugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SorbetSettingsTest : BasePlatformTestCase() {

    private lateinit var settings: SorbetSettings

    override fun setUp() {
        super.setUp()
        settings = SorbetSettings.getInstance(project)
        resetSettings()
    }

    override fun tearDown() {
        try {
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

    fun testDefaultSettings() {
        val state = settings.state

        assertEquals("", state.customSorbetPath)
        assertTrue(state.highlightUntyped)
        assertTrue(state.enableCompletionNudges)
        assertTrue(state.enableOperationNotifications)
        assertEquals("", state.watchmanPath)
        assertFalse(state.disableWatchman)
        assertEquals("", state.additionalServerFlags)
    }

    fun testSetCustomSorbetPath() {
        settings.state.customSorbetPath = "/usr/local/bin/srb"

        assertEquals("/usr/local/bin/srb", settings.state.customSorbetPath)

        val reloadedSettings = SorbetSettings.getInstance(project)
        assertEquals("/usr/local/bin/srb", reloadedSettings.state.customSorbetPath)
    }

    fun testSetHighlightUntyped() {
        settings.state.highlightUntyped = false
        assertFalse(settings.state.highlightUntyped)

        settings.state.highlightUntyped = true
        assertTrue(settings.state.highlightUntyped)
    }

    fun testSetEnableCompletionNudges() {
        settings.state.enableCompletionNudges = false
        assertFalse(settings.state.enableCompletionNudges)

        settings.state.enableCompletionNudges = true
        assertTrue(settings.state.enableCompletionNudges)
    }

    fun testSetEnableOperationNotifications() {
        settings.state.enableOperationNotifications = false
        assertFalse(settings.state.enableOperationNotifications)

        settings.state.enableOperationNotifications = true
        assertTrue(settings.state.enableOperationNotifications)
    }

    fun testSetWatchmanPath() {
        settings.state.watchmanPath = "/opt/homebrew/bin/watchman"

        assertEquals("/opt/homebrew/bin/watchman", settings.state.watchmanPath)
    }

    fun testSetDisableWatchman() {
        settings.state.disableWatchman = true
        assertTrue(settings.state.disableWatchman)

        settings.state.disableWatchman = false
        assertFalse(settings.state.disableWatchman)
    }

    fun testSetAdditionalServerFlags() {
        settings.state.additionalServerFlags = "--enable-experimental-hover"

        assertEquals("--enable-experimental-hover", settings.state.additionalServerFlags)
    }

    fun testMultipleSettingsChanges() {
        settings.state.customSorbetPath = "/custom/srb"
        settings.state.highlightUntyped = false
        settings.state.watchmanPath = "/custom/watchman"
        settings.state.disableWatchman = true
        settings.state.additionalServerFlags = "--verbose"

        assertEquals("/custom/srb", settings.state.customSorbetPath)
        assertFalse(settings.state.highlightUntyped)
        assertEquals("/custom/watchman", settings.state.watchmanPath)
        assertTrue(settings.state.disableWatchman)
        assertEquals("--verbose", settings.state.additionalServerFlags)
    }

    fun testStateModification() {
        val originalState = settings.state

        originalState.customSorbetPath = "/modified/path"
        originalState.highlightUntyped = false

        assertEquals("/modified/path", settings.state.customSorbetPath)
        assertFalse(settings.state.highlightUntyped)
    }

    fun testGetInstanceReturnsSameInstance() {
        val instance1 = SorbetSettings.getInstance(project)
        val instance2 = SorbetSettings.getInstance(project)

        assertSame(instance1, instance2)
    }
}
