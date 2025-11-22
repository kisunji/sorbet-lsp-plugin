package com.github.kisunji.sorbetlspplugin.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class SorbetSettingsConfigurable(project: Project) : BoundConfigurable("Sorbet") {

    private val settings = SorbetSettings.getInstance(project)

    override fun createPanel(): DialogPanel = panel {
        group("Sorbet Binary") {
            row("Custom Sorbet path:") {
                textField()
                    .bindText(settings.state::customSorbetPath)
                    .columns(COLUMNS_LARGE)

                    .comment("Leave empty to auto-detect (bundle exec srb → srb from PATH)")
            }
        }

        group("Features") {
            row {
                checkBox("Highlight untyped code")
                    .bindSelected(settings.state::highlightUntyped)
                    .comment("Show diagnostics for untyped code (typed: false)")
            }
            row {
                checkBox("Enable completion nudges for typed: false files")
                    .bindSelected(settings.state::enableCompletionNudges)
                    .comment("Show notices encouraging upgrade to typed: true")
            }
            row {
                checkBox("Show operation notifications")
                    .bindSelected(settings.state::enableOperationNotifications)
                    .comment("Display status updates (Indexing, Typechecking, etc.)")
            }
        }

        group("Watchman") {
            row("Watchman path:") {
                textField()
                    .bindText(settings.state::watchmanPath)
                    .columns(COLUMNS_LARGE)

                    .comment("Leave empty to auto-detect")
            }
            row {
                checkBox("Disable Watchman")
                    .bindSelected(settings.state::disableWatchman)
                    .comment("Disable Watchman even if available (not recommended)")
            }
        }

        group("Advanced") {
            row("Additional server flags:") {
                textField()
                    .bindText(settings.state::additionalServerFlags)
                    .columns(COLUMNS_LARGE)

                    .comment("e.g., --cache-dir=.sorbet-cache")
            }
        }
    }
}
