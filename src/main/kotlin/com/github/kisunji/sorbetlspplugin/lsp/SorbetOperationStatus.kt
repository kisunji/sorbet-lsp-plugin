package com.github.kisunji.sorbetlspplugin.lsp

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
class SorbetOperationStatus {

    data class Operation(
        val operationName: String,
        val description: String,
        val status: String
    )

    private val currentOperations = Collections.synchronizedMap(LinkedHashMap<String, Operation>())
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun updateOperation(operationName: String, description: String, status: String) {
        synchronized(currentOperations) {
            if (status == "end") {
                currentOperations.remove(operationName)
            } else {
                currentOperations[operationName] = Operation(operationName, description, status)
            }
        }
        notifyListeners()
    }

    fun getCurrentOperation(): Operation? {
        synchronized(currentOperations) {
            return currentOperations.values.lastOrNull()
        }
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }

    companion object {
        fun getInstance(project: Project): SorbetOperationStatus {
            return project.getService(SorbetOperationStatus::class.java)
        }
    }
}
