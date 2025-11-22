# Sorbet LSP Plugin

![Build](https://github.com/kisunji/sorbet-lsp-plugin/workflows/Build/badge.svg)

<!-- Plugin description -->
Language Server Protocol (LSP) integration for Sorbet, a fast, powerful type checker for Ruby. This plugin provides real-time type checking, code completion, diagnostics, and navigation features powered by the Sorbet language server.
<!-- Plugin description end -->

## Requirements

- IntelliJ IDEA Ultimate 2024.2+ or RubyMine
- Sorbet
- Watchman

## Installation

### From Marketplace
<kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > Search "Sorbet LSP" > <kbd>Install</kbd>

### From Disk
Download from [releases](https://github.com/kisunji/sorbet-lsp-plugin/releases) and install via:
<kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

### Build from Source
```bash
git clone https://github.com/kisunji/sorbet-lsp-plugin.git
cd sorbet-lsp-plugin
./gradlew buildPlugin
# Output: build/distributions/sorbet-lsp-plugin-*.zip
```

## Features

- Real-time type checking
- Go to definition
- Hover type information
- Code completion
- Find references
- Document symbols
- Code actions

## Configuration

<kbd>Settings</kbd> > <kbd>Tools</kbd> > <kbd>Sorbet</kbd>

- **Custom binary path**: Override auto-detection
- **Highlight untyped code**: Show warnings in `typed: false` files
- **Completion nudges**: Encourage upgrading strictness
- **Watchman path**: Custom watchman location
- **Additional flags**: Extra `srb tc` arguments

## Status Bar

Two widgets appear when Sorbet is active:

1. **"Sorbet"** - Server connection status
2. **"Sorbet: [Operation]"** - Current activity (Idle/Indexing/Typechecking)

Click either widget to access settings or logs.

## Troubleshooting

**Server won't start:**
- Verify: `bundle exec srb version` or `srb version`
- Check binary path in settings
- Review logs: <kbd>Help</kbd> > <kbd>Show Log in Explorer</kbd>

## Resources

- [Sorbet Documentation](https://sorbet.org/docs/)
- [Report Issues](https://github.com/kisunji/sorbet-lsp-plugin/issues)
- [JetBrains LSP API](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
