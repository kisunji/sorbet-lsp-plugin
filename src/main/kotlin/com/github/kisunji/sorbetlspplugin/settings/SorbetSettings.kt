package com.github.kisunji.sorbetlspplugin.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "SorbetSettings",
    storages = [Storage("sorbet-lsp.xml")]
)
class SorbetSettings : PersistentStateComponent<SorbetSettings.State> {

    data class State(
        var customSorbetPath: String = "",
        var highlightUntyped: Boolean = true,
        var enableCompletionNudges: Boolean = true,
        var enableOperationNotifications: Boolean = true,
        var watchmanPath: String = "",
        var disableWatchman: Boolean = false,
        var additionalServerFlags: String = ""
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(project: Project): SorbetSettings {
            return project.service()
        }
    }
}
