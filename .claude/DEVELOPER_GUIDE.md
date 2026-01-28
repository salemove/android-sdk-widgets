## Agentic Development Tools

This repository is configured with **Claude Code custom commands** to streamline development workflows.


### GitHub Integration

Claude has direct access to GitHub repositories via GitHub CLI (`gh`):

**Setup:**
- **Pre-configured:** `gh` CLI commands are pre-approved in [.claude/settings.json](.claude/settings.json)
- **Uses GitHub CLI:** Direct `gh` command execution (no MCP server needed)
- **Token-efficient:** Direct CLI is more efficient than MCP for GitHub operations

**Authentication (One-time per machine):**

1. **Install GitHub CLI** (if not already installed):
   ```bash
   brew install gh  # macOS
   # Or download from: https://cli.github.com/
   ```

2. **Authenticate with GitHub** (browser-based OAuth):
   ```bash
   gh auth login
   ```
  - Select: **GitHub.com**
  - Select: **HTTPS**
  - Authenticate Git: **Yes**
  - How to authenticate: **Login with a web browser**
  - Copy the one-time code → press Enter → browser opens → paste code → authorize

**Using the `/gh` Skill:**

The repository includes a custom `/gh` skill for GitHub operations:

```
/gh create a pull request for my changes
/gh show me all open PRs
/gh check the CI status for PR #123
/gh create an issue for the memory leak
```

**Common Operations:**
- **Create PR**: "create a pull request" or "make a PR for these changes"
- **View PRs**: "show me open PRs" or "list my PRs"
- **Check CI**: "check CI status for PR 123" or "are the tests passing?"
- **Issues**: "create an issue" or "view issue 456"
- **Search**: "search the repo for GliaWidgets"

**Manual `gh` Commands:**

You can also ask Claude to run `gh` commands directly:
- "Run gh pr view 123"
- "Show me gh pr status"
- "Use gh to list recent commits"

**Benefits:**
- ✅ Browser-based OAuth (no manual token creation)
- ✅ No tokens stored in files (uses `gh` credentials)
- ✅ Automatic token refresh handled by `gh`
- ✅ Same authentication used for git operations
- ✅ Direct CLI execution (more token-efficient)
- ✅ Repository-specific PR templates and conventions
- ✅ Easy team onboarding


### Android Emulator Integration

Claude has direct access to Android Emulators via the Mobile MCP server for UI testing and debugging:

**Mobile MCP (`mobile-mcp`):**
- **Pre-configured:** Set up in [.mcp.json](.mcp.json)
- **Capabilities:** Advanced device control, app management, orientation changes, screenshots
- **Use for:** Comprehensive mobile testing automation on Android emulators

**Usage Examples:**
- "Take a screenshot of the current emulator screen"
- "List all running Android emulators"
- "Install the debug APK on the emulator"
- "Change emulator orientation to landscape"
- "Tap at specific coordinates on the screen"

**Benefits:**
- ✅ No manual emulator navigation needed
- ✅ Automated UI testing workflows
- ✅ Quick visual debugging
- ✅ Integration with test automation

## Parallel Execution Best Practices

Claude Code performs better when you request multiple independent operations in parallel. This reduces context switching and speeds up analysis.

### Recommended Patterns

**File Exploration** - Request multiple file reads together:
```
Read these files to understand the engagement system:
- widgetssdk/src/main/java/com/glia/widgets/GliaWidgets.kt
- widgetssdk/src/main/java/com/glia/widgets/launcher/EngagementLauncher.kt
- widgetssdk/src/main/java/com/glia/widgets/di/Dependencies.kt
```

**Codebase Search** - Parallel grep for related patterns:
```
Find all usages of: GliaWidgets, UiTheme, ChatActivity, EngagementLauncher
```

**Build Validation** - Run checks concurrently:
```
In parallel: Run lint, build the SDK, and execute unit tests
```

### Benefits
- **Faster responses**: Multiple tool calls in single message
- **Lower token usage**: Less context switching overhead
- **Better workflow**: Complete analysis before making changes
