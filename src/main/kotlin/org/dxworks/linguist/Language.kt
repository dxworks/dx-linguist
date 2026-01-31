package org.dxworks.linguist

/**
 * Represents a programming language with its associated metadata.
 *
 * This data class holds information about a language as defined by
 * [GitHub Linguist](https://github.com/github/linguist).
 *
 * @property name The canonical name of the language (e.g., "Java", "Kotlin").
 * @property type The type classification of the language (e.g., "programming", "markup", "data").
 * @property group The parent language group, if this language is a variant (e.g., "C" for "C++").
 * @property extensions List of file extensions associated with this language (e.g., [".kt", ".kts"]).
 * @property filenames List of specific filenames associated with this language (e.g., ["Makefile", "Dockerfile"]).
 * @property aliases Alternative names for this language (e.g., ["golang"] for "Go").
 */
data class Language(
    val name: String,
    val type: String,
    val group: String?,
    val extensions: List<String>,
    val filenames: List<String>,
    val aliases: List<String>
)
