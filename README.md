# Dx-Linguist

Dx-Linguist is a jvm library for mapping extensions or filenames to languages based
on https://github.com/github/linguist

The mapping file can be found at https://github.com/github/linguist/blob/master/lib/linguist/languages.yml or as binary
at https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml

## Setup

```kotlin
Linguist(linguistFile: File?)
```

Linguist file defaults to {users.home}/.linguist/languages.yml if:

* the argument 'linguistFile' is null
* the file does not exist
* the file does not have the extension yml/yaml
* the files contents are not correctly formatted

## Usage

```kotlin
linguist.getLanguages(path: String)
```

returns the list of languages according to the given path

```kotlin
linguist.isOf(path: String, vararg languages : String)
```

returns true if the path corresponds to one of the languages given: matches name, parent, or aliases

To update the mappings file call the `updateLinguistFile()` method.
