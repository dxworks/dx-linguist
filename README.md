# dx-linguist

A Kotlin/JVM library for mapping file extensions and filenames to programming languages based on [GitHub Linguist](https://github.com/github/linguist).

[![Build](https://github.com/dxworks/dx-linguist/actions/workflows/build.yml/badge.svg)](https://github.com/dxworks/dx-linguist/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.dxworks.utils/dx-linguist)](https://central.sonatype.com/artifact/org.dxworks.utils/dx-linguist)

## Installation

### Gradle (Kotlin DSL)
```kotlin
implementation("org.dxworks:dx-linguist:1.0.0")
```

### Gradle (Groovy)
```groovy
implementation 'org.dxworks:dx-linguist:1.0.0'
```

### Maven
```xml
<dependency>
    <groupId>org.dxworks</groupId>
    <artifactId>dx-linguist</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Features

- **Extension-based language detection** - Map file extensions to programming languages
- **Filename-based detection** - Recognize special files like `Makefile`, `Dockerfile`
- **Language validation** - Check if a language name or alias is registered
- **Auto-updating** - Download latest language definitions from GitHub Linguist
- **Alias support** - Match languages by name, alias, or group

## Usage

### Basic Usage

```kotlin
val linguist = Linguist()

// Get languages for a file path
val languages = linguist.getLanguages("src/main/kotlin/App.kt")
// Returns: [Language(name="Kotlin", ...)]

// Check if a file is of a specific language
linguist.isOf("Main.java", "Java")        // true
linguist.isOf("app.ts", "TypeScript")     // true
linguist.isOf("style.css", "JavaScript")  // false
```

### Custom Linguist File

```kotlin
// Use a custom language definitions file
val linguist = Linguist(File("/path/to/custom/languages.yml"))
```

### Update Language Definitions

```kotlin
// Download the latest language definitions from GitHub Linguist
linguist.updateLinguistFile()
```

### Check Language Registration

```kotlin
// Verify if a language is recognized
linguist.isRegistered("Kotlin")   // true
linguist.isRegistered("golang")   // true (alias for Go)
linguist.isRegistered("unknown")  // false
```

## API Reference

### Linguist

- `getLanguages(path: String): List<Language>` - Get all languages matching a file path
- `isOf(path: String, vararg languages: String): Boolean` - Check if a file matches any of the given languages
- `isRegistered(language: String): Boolean` - Check if a language name/alias is registered
- `updateLinguistFile()` - Download latest language definitions from GitHub

### Language

Data class with the following properties:
- `name: String` - Canonical language name
- `type: String` - Language type (programming, markup, data, etc.)
- `group: String?` - Parent language group
- `extensions: List<String>` - File extensions (e.g., [".kt", ".kts"])
- `filenames: List<String>` - Specific filenames (e.g., ["Makefile"])
- `aliases: List<String>` - Alternative names

## License

Apache License 2.0
