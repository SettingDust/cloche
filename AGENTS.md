# AGENTS.md

## Project Overview

Cloche is a Gradle plugin for Minecraft mod development. It provides multi-target workflows across loaders (Fabric/Forge/NeoForge) and Minecraft versions, with support for metadata generation, remapping, run configurations, and source-set orchestration.

Primary stack:
- Kotlin Gradle Plugin development (`kotlin-dsl`, `java-gradle-plugin`)
- Gradle wrapper build (`gradlew` / `gradlew.bat`)
- JUnit Platform via `kotlin("test")` + `gradleTestKit()`

Repository layout highlights:
- `src/main/kotlin`: plugin implementation code
- `src/test/kotlin`: unit/integration tests for plugin behavior
- `integration-test/`: separate Gradle project used for plugin integration scenarios

## Setup Commands

Use the Gradle wrapper from the repository root.

```bash
# Windows
.\gradlew.bat -v

# macOS/Linux
./gradlew -v
```

Expected toolchain context:
- Build uses Gradle 9.x wrapper
- Kotlin JVM toolchain target is Java 17 (`kotlin.jvmToolchain(17)`)
- CI currently runs on Temurin 21 (keep local setup compatible with CI if possible)

## Development Workflow

From repository root:

```bash
# Show all tasks
./gradlew tasks --all --console=plain

# Build plugin and run checks
./gradlew build

# Clean build outputs
./gradlew clean
```

On Windows, replace `./gradlew` with `.\gradlew.bat`.

## Testing Instructions

Main test workflow:

```bash
# Run all tests in root project
./gradlew test --console=plain

# Run full verification pipeline
./gradlew check --console=plain

# Dry-run test task graph (fast sanity check)
./gradlew test --dry-run --console=plain
```

Single test selection examples:

```bash
# Run a specific test class
./gradlew test --tests "earth.terrarium.cloche.ClocheIntegrationTests"

# Run tests matching a pattern
./gradlew test --tests "*Cloche*"
```

Integration test subproject (separate Gradle build):

```bash
./gradlew -p integration-test build
./gradlew -p integration-test test
```

Testing expectations for agent changes:
- Add or update tests when behavior changes.
- Prefer narrow test runs while iterating, then run `test` (or `check`) before finalizing.

## Code Style and Conventions

- Follow Kotlin official style (`kotlin.code.style=official`).
- Preserve existing package structure under `earth.terrarium.cloche`.
- Keep public Gradle API changes backward-compatible unless intentionally breaking behavior.
- Avoid broad refactors unrelated to the task.
- Prefer focused, minimal diffs.

## Build and Publish

Useful publish commands:

```bash
# Publish artifacts to local Maven cache
./gradlew publishToMavenLocal

# Publish all configured publications/repositories
./gradlew publish
```

Repository publishing notes:
- CI workflow `.github/workflows/maven-publish.yml` publishes on pushes to `main` when `gradle.properties` changes.
- Version is read from `gradle.properties`.
- Artifacts are staged via local Maven repository before being copied to external Maven repo.

## Debugging and Troubleshooting

- If dependency resolution behaves unexpectedly, rerun with stacktrace/info:

```bash
./gradlew <task> --stacktrace --info
```

- For configuration-cache or parallelism issues during debugging, temporarily disable advanced options in `gradle.properties` for local troubleshooting only, then restore defaults.
- When changing plugin behavior that affects task wiring, inspect task graph with `--dry-run` first.

## Pull Request Guidelines

Before opening a PR:

```bash
./gradlew clean test
```

Recommended:
- Keep PR scope small and focused.
- Include tests for functional changes.
- Mention impacted loaders/targets (Fabric/Forge/NeoForge) in PR description when relevant.

## Agent Notes

- Prefer wrapper commands over system Gradle.
- Do not commit credentials; publishing credentials are expected from environment/project properties.
- If you modify integration behavior, validate both root project tests and `integration-test` build where feasible.
