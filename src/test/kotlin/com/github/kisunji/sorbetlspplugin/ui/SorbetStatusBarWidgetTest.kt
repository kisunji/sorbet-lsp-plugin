package com.github.kisunji.sorbetlspplugin.ui

import com.github.kisunji.sorbetlspplugin.lsp.SorbetOperationStatus
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SorbetStatusBarWidgetTest : BasePlatformTestCase() {

    private lateinit var factory: SorbetStatusBarWidgetFactory
    private lateinit var operationStatus: SorbetOperationStatus

    override fun setUp() {
        super.setUp()
        factory = SorbetStatusBarWidgetFactory()
        operationStatus = SorbetOperationStatus.getInstance(project)
        clearAllOperations()
    }

    override fun tearDown() {
        try {
            clearAllOperations()
        } finally {
            super.tearDown()
        }
    }

    private fun clearAllOperations() {
        val operations = listOf("Indexing", "Typechecking", "CustomOp")
        operations.forEach { operationStatus.updateOperation(it, "Clear", "end") }
    }

    fun testFactoryId() {
        assertEquals("SorbetOperationStatus", factory.id)
    }

    fun testFactoryDisplayName() {
        assertEquals("Sorbet Operation Status", factory.displayName)
    }

    fun testFactoryIsAvailable() {
        assertTrue(factory.isAvailable(project))
    }

    fun testWidgetCreation() {
        val widget = factory.createWidget(project)
        assertNotNull(widget)
        assertEquals("SorbetOperationStatus", widget.ID())
    }

    fun testWidgetIdleText() {
        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val text = presentation.getText()
            assertEquals("Sorbet: Idle", text)
        }
    }

    fun testWidgetIndexingText() {
        operationStatus.updateOperation("Indexing", "Building index", "begin")

        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val text = presentation.getText()
            assertEquals("Sorbet: Indexing...", text)
        }

        operationStatus.updateOperation("Indexing", "Done", "end")
    }

    fun testWidgetTypecheckingText() {
        operationStatus.updateOperation("Typechecking", "Checking types", "begin")

        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val text = presentation.getText()
            assertEquals("Sorbet: Typechecking...", text)
        }

        operationStatus.updateOperation("Typechecking", "Done", "end")
    }

    fun testWidgetCustomOperationText() {
        operationStatus.updateOperation("CustomOp", "Custom operation", "begin")

        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val text = presentation.getText()
            assertEquals("Sorbet: CustomOp...", text)
        }

        operationStatus.updateOperation("CustomOp", "Done", "end")
    }

    fun testWidgetTooltipIdle() {
        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val tooltip = presentation.getTooltipText()
            assertEquals("Sorbet LSP is idle. Click to open settings.", tooltip)
        }
    }

    fun testWidgetTooltipWithOperation() {
        operationStatus.updateOperation("Indexing", "Building index for project", "begin")

        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val tooltip = presentation.getTooltipText()
            assertNotNull(tooltip)
            assertTrue(tooltip!!.contains("Building index for project"))
            assertTrue(tooltip.contains("Click to open settings"))
        }

        operationStatus.updateOperation("Indexing", "Done", "end")
    }

    fun testWidgetAlignment() {
        val widget = factory.createWidget(project)
        val presentation = widget.getPresentation() as StatusBarWidget.TextPresentation

        ApplicationManager.getApplication().invokeAndWait {
            val alignment = presentation.getAlignment()
            assertEquals(0.0f, alignment)
        }
    }
}
