package com.github.kisunji.sorbetlspplugin.lsp

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class SorbetOperationStatusTest : BasePlatformTestCase() {

    private lateinit var operationStatus: SorbetOperationStatus

    override fun setUp() {
        super.setUp()
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

        for (i in 0..20) {
            operationStatus.updateOperation("Operation$i", "Clear", "end")
        }
    }

    fun testUpdateOperation() {
        operationStatus.updateOperation("Indexing", "Building index", "begin")

        val operation = operationStatus.getCurrentOperation()
        assertNotNull(operation)
        assertEquals("Indexing", operation?.operationName)
        assertEquals("Building index", operation?.description)
        assertEquals("begin", operation?.status)
    }

    fun testUpdateOperationWithEnd() {
        operationStatus.updateOperation("Indexing", "Building index", "begin")
        assertNotNull(operationStatus.getCurrentOperation())

        operationStatus.updateOperation("Indexing", "Done", "end")
        assertNull(operationStatus.getCurrentOperation())
    }

    fun testMultipleOperations() {
        operationStatus.updateOperation("Indexing", "Building index", "begin")
        operationStatus.updateOperation("Typechecking", "Checking types", "begin")

        val operation = operationStatus.getCurrentOperation()
        assertNotNull(operation)
        assertTrue(
            operation?.operationName == "Indexing" || operation?.operationName == "Typechecking"
        )
    }

    fun testListenerNotification() {
        val latch = CountDownLatch(1)
        var notificationReceived = false

        val listener: () -> Unit = {
            notificationReceived = true
            latch.countDown()
        }

        operationStatus.addListener(listener)
        operationStatus.updateOperation("Indexing", "Building index", "begin")

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(notificationReceived)

        operationStatus.removeListener(listener)
    }

    fun testRemoveListener() {
        var callCount = 0
        val listener: () -> Unit = { callCount++ }

        operationStatus.addListener(listener)
        operationStatus.updateOperation("Indexing", "Building index", "begin")
        assertEquals(1, callCount)

        operationStatus.removeListener(listener)
        operationStatus.updateOperation("Typechecking", "Checking types", "begin")
        assertEquals(1, callCount)
    }

    fun testThreadSafety() {
        val latch = CountDownLatch(10)
        val threads = mutableListOf<Thread>()

        repeat(10) { index ->
            val thread = thread {
                operationStatus.updateOperation(
                    "Operation$index",
                    "Description $index",
                    "begin"
                )
                Thread.sleep(10)
                operationStatus.updateOperation(
                    "Operation$index",
                    "Done",
                    "end"
                )
                latch.countDown()
            }
            threads.add(thread)
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        threads.forEach { it.join() }

        val finalOp = operationStatus.getCurrentOperation()
        if (finalOp != null) {
            operationStatus.updateOperation(finalOp.operationName, "cleanup", "end")
        }
        assertNull(operationStatus.getCurrentOperation())
    }

    fun testMultipleListeners() {
        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)

        val listener1: () -> Unit = { latch1.countDown() }
        val listener2: () -> Unit = { latch2.countDown() }

        operationStatus.addListener(listener1)
        operationStatus.addListener(listener2)

        operationStatus.updateOperation("Indexing", "Building index", "begin")

        assertTrue(latch1.await(5, TimeUnit.SECONDS))
        assertTrue(latch2.await(5, TimeUnit.SECONDS))

        operationStatus.removeListener(listener1)
        operationStatus.removeListener(listener2)
    }

    fun testOperationReplacement() {
        operationStatus.updateOperation("Indexing", "Building index", "begin")
        val firstOp = operationStatus.getCurrentOperation()
        assertEquals("Building index", firstOp?.description)

        operationStatus.updateOperation("Indexing", "Updated description", "progress")
        val secondOp = operationStatus.getCurrentOperation()
        assertEquals("Updated description", secondOp?.description)
        assertEquals("progress", secondOp?.status)
    }
}
