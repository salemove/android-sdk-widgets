# GliaWidgets Android SDK

Android UI library (AAR) for customer engagement widgets - chat, audio/video calls, secure conversations. Consumes Glia Core SDK. Published to Maven Central as `com.glia:android-widgets`.

**iOS Counterpart**: [ios-sdk-widgets](https://github.com/salemove/ios-sdk-widgets) - Reference for cross-platform feature parity

**Language**: Kotlin preferred, Java legacy exists. No Jetpack Compose - traditional View system only.

## Module Structure
```
widgetssdk/           # Main SDK (published)
├── src/main/java/com/glia/widgets/
│   ├── GliaWidgets.kt       # Entry point - init(), getEngagementLauncher()
│   ├── GliaWidgetsConfig.kt # Configuration
│   ├── di/Dependencies.kt   # DI container - all dependencies here
│   └── view/unifiedui/      # Theme system
├── src/test/           # Unit tests (JUnit, MockK, Robolectric)
└── src/testSnapshot/   # Paparazzi snapshot tests
app/                    # Demo app
```

## Key Architecture

**MVP Pattern**: Views + Controllers + Contracts
- Controllers contain business logic
- Contracts define interfaces between View and Controller
- **ALWAYS implement Controller cleanup**: Every Controller must have a cleanup/dispose method called when detached from view to prevent memory leaks

**Dependency Injection**: Centralized `Dependencies` object (not Dagger/Hilt)
- All SDK dependencies through `Dependencies.*`
- Lazy initialization with `by lazy`

**Unified Theme System**: Remote JSON config via `GliaWidgetsConfig.setUiJsonRemoteConfig()`
- Location: `view/unifiedui/`
- All UI styling flows through this system
- Legacy `UiTheme.kt` is deprecated

**Reactive**: RxJava 3 with RxAndroid
- `io.reactivex.rxjava3.*` namespace
- Always dispose subscriptions in lifecycle methods
- Always provide error handlers in `.subscribe()`

## Testing Commands
```bash
# Snapshot tests (UI validation)
./gradlew widgetssdk:recordPaparazziSnapshot  # Generate/update snapshots
./gradlew widgetssdk:verifyPaparazziSnapshot  # Verify against existing

# Unit tests
./gradlew widgetssdk:test

# Lint
./gradlew lint
```

## Testing Requirements
- **ALWAYS add unit tests for changes**: Every code change must include corresponding unit tests
- **Add tests for touched layers**: If modifying code without tests, add tests for that layer/component
- **Update snapshot tests**: Any UI change requires updating Paparazzi snapshots

## Critical Rules

### Security & Logging
- **NEVER use Android Log** - Use `GliaLogger`/`GliaTracer` from telemetry SDK
- **NEVER log PII** - No user input, operator names, or chat content in logs
- **NEVER bypass Core SDK** - All networking and data storage through Core SDK
- **NEVER store sensitive data** - Delegate to Core SDK

### Code Standards
- **NEVER hardcode colors/dimensions** - Use theme system and resources
- **NEVER hardcode strings** - Use string resources
- **NEVER use `!!`** - Use safe calls (`?.`) and null handling
- **NEVER block main thread** - Use RxJava schedulers
- **ALWAYS implement Controller cleanup** - Dispose subscriptions when Controller detaches from view
- Document all `public` APIs with KDoc

## Kotlin Style
- **Explicit return types**: All functions must explicitly declare return types
- **Variable type declarations**: Class and file-level variables must explicitly declare types; local variables may use type inference

### UI Standards
- **NEVER skip snapshot tests for UI changes** - Run record/verify before commits
- **NEVER ignore Unified Theme** - All styling through theme configuration
- **NEVER skip accessibility** - Content descriptions required, TalkBack support mandatory
- Use `dp` for dimensions, `sp` for text sizes

### Architecture
- **NEVER create singletons** - Use `Dependencies` object
- **NEVER hold Activity/Fragment in static fields** - Memory leaks
- **NEVER modify public APIs without coordination** - Breaking changes need major version

## Custom Skills
- `/gh` - GitHub operations (PRs, issues, CI status) - see `.claude/skills/gh/`

## Resources
- [Developer Docs](https://developer.glia.com/api-usage-refs/android-api)
- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Developer Guide](.claude/DEVELOPER_GUIDE.md) - Setup and workflows
- Version catalog: `gradle/libs.versions.toml`
