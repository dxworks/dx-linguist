# AGENTS.md

This file provides guidance to AI coding agents when working with code in this repository.

## Project Overview

dx-linguist is a Kotlin library for mapping file extensions and filenames to programming languages. It uses language definitions from [GitHub Linguist](https://github.com/github/linguist). Published to Maven Central under `org.dxworks`.

## Build Commands

```bash
./mvnw clean verify    # Build and run tests
./mvnw test            # Run tests only
./mvnw install         # Install to local ~/.m2
```

## Release Workflow

- **Snapshots**: Auto-published on push to `main` branch
- **Releases**: Triggered by pushing a `v*` tag (e.g., `v1.0.0`)

To release (version is automatically taken from tag):
```bash
git tag v1.0.0
git push --tags
```

The CI workflow extracts the version from the tag (strips the `v` prefix) and publishes to Maven Central.

## Architecture

### Core Components

- **Linguist** (`Linguist.kt`) - Main facade providing language lookup by file path, extension, or filename
- **Language** (`Language.kt`) - Data class representing a language with name, type, extensions, filenames, and aliases

### Key Functionality

1. **Extension-based lookup** - Maps file extensions (e.g., `.kt`) to languages
2. **Filename-based lookup** - Maps specific filenames (e.g., `Makefile`) to languages
3. **Language validation** - Check if a language name/alias is registered
4. **Auto-update** - Downloads latest language definitions from GitHub Linguist

### Data Flow

1. On initialization, `Linguist` loads language definitions from `~/.linguist/languages.yml`
2. If the file doesn't exist, it's automatically downloaded from GitHub Linguist
3. Languages are indexed by extension and filename for fast lookup
4. `getLanguages(path)` returns matching languages for any file path

## Tech Stack

- Kotlin 2.3.0 targeting Java 11
- JUnit 6.0.2 for testing
- Jackson YAML for parsing language definitions
- Maven with central-publishing-maven-plugin for Maven Central Portal publishing
