package com.github.kisunji.sorbetlspplugin.lsp

import com.google.gson.JsonObject
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito

class SorbetLspClientTest : BasePlatformTestCase() {

    private lateinit var client: SorbetLspClient
    private lateinit var operationStatus: SorbetOperationStatus

    override fun setUp() {
        super.setUp()
        val notificationsHandler = Mockito.mock(LspServerNotificationsHandler::class.java)
        client = SorbetLspClient(notificationsHandler)
        client.setProject(project)
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

    fun testShowOperationWithJsonObject() {
        val params = JsonObject().apply {
            addProperty("operationName", "Indexing")
            addProperty("description", "Building index")
            addProperty("status", "begin")
        }

        client.showOperation(params)

        val operation = operationStatus.getCurrentOperation()
        assertNotNull(operation)
        assertEquals("Indexing", operation?.operationName)
        assertEquals("Building index", operation?.description)
        assertEquals("begin", operation?.status)
    }

    fun testShowOperationWithMap() {
        val params = mapOf(
            "operationName" to "Typechecking",
            "description" to "Checking types",
            "status" to "progress"
        )

        client.showOperation(params)

        val operation = operationStatus.getCurrentOperation()
        assertNotNull(operation)
        assertEquals("Typechecking", operation?.operationName)
        assertEquals("Checking types", operation?.description)
        assertEquals("progress", operation?.status)
    }

    fun testShowOperationEndStatus() {
        operationStatus.updateOperation("Indexing", "Building index", "begin")
        assertNotNull(operationStatus.getCurrentOperation())

        val params = JsonObject().apply {
            addProperty("operationName", "Indexing")
            addProperty("description", "Done")
            addProperty("status", "end")
        }

        client.showOperation(params)
        assertNull(operationStatus.getCurrentOperation())
    }

    fun testShowOperationWithMissingFields() {
        val params = JsonObject().apply {
            addProperty("operationName", "Indexing")
        }

        client.showOperation(params)

        val operation = operationStatus.getCurrentOperation()
        assertNotNull(operation)
        assertEquals("Indexing", operation?.operationName)
        assertEquals("", operation?.description)
        assertEquals("", operation?.status)
    }

    fun testMultipleOperationUpdates() {
        val params1 = JsonObject().apply {
            addProperty("operationName", "Indexing")
            addProperty("description", "Step 1")
            addProperty("status", "begin")
        }
        client.showOperation(params1)
        assertEquals("Step 1", operationStatus.getCurrentOperation()?.description)

        val params2 = JsonObject().apply {
            addProperty("operationName", "Indexing")
            addProperty("description", "Step 2")
            addProperty("status", "progress")
        }
        client.showOperation(params2)
        assertEquals("Step 2", operationStatus.getCurrentOperation()?.description)

        val params3 = JsonObject().apply {
            addProperty("operationName", "Indexing")
            addProperty("description", "Complete")
            addProperty("status", "end")
        }
        client.showOperation(params3)
        assertNull(operationStatus.getCurrentOperation())
    }
}
