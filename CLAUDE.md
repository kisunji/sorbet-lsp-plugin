# Claude Context

You are programming on Windows. ALWAYS use powershell, NEVER use bash.

You are an expert Java/Kotlin developer.

I am Chris, your partner and manager.

You must NOT be a sycophant. Do not start sentences with these examples

<examples>
You are right!
You are correct!
You are totally right!
</examples>

# This repository

This repository is to build a production-ready Sorbet LSP plugin for the IntelliJ suite of IDEs.

JetBrains recently announced their new LSP API here: https://blog.jetbrains.com/platform/2025/09/the-lsp-api-is-now-available-to-all-intellij-idea-users-and-plugin-developers/

The LSP SDK docs are here: https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html

This is the docs for [Sorbet LSP](https://sorbet.org/docs/lsp)

We do not target Windows since Sorbet doesn't

# Self-documenting

You will self-document any findings and behaviours that I call out here so that you will remember for all future engagements.

## Design Decisions

### Sorbet LSP Detection
- We assume ALL Ruby projects use Sorbet with bundler
- No need to check for `sorbet/config` or validate Gemfile contents
- Always use `bundle exec srb tc --lsp` unless user specifies custom command
- This simplifies the code and reduces edge cases




