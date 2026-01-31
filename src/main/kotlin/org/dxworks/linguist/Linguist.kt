package org.dxworks.linguist

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Paths

private const val githubLinguistFileUrl =
    "https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml"

/**
 * Default home directory for storing linguist language files.
 *
 * Resolves to `~/.linguist` and creates the directory if it doesn't exist.
 */
val dxLinguistHome by lazy {
    Paths.get(System.getProperty("user.home")).resolve(".linguist").toFile().apply { mkdirs() }
}

/**
 * Main facade for mapping file paths to programming languages.
 *
 * Linguist provides functionality to identify programming languages based on file extensions
 * and filenames, using language definitions from [GitHub Linguist](https://github.com/github/linguist).
 *
 * The language mappings are loaded from a local YAML file. If no file exists, it will be
 * automatically downloaded from the GitHub Linguist repository.
 *
 * @param linguistFile Optional custom linguist file. If null or invalid, defaults to `~/.linguist/languages.yml`.
 */
class Linguist(linguistFile: File? = null) {
    private val mapper = YAMLMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .defaultPropertyInclusion(JsonInclude.Value.ALL_NON_NULL)
        .build()

    private var linguistFile: File

    private fun checkLinguistFile(it: File): Boolean {
        return it.exists() && (it.name.endsWith(".yml") || it.name.endsWith(".yaml"))
    }

    /**
     * Map of language names to their [Language] definitions.
     *
     * Lazily loaded from the linguist file on first access.
     */
    val languages: Map<String, Language> by lazy { readLocalLanguages() }

    /**
     * Map of file extensions to lists of languages that use that extension.
     *
     * Extensions include the leading dot (e.g., ".kt" -> [Kotlin]).
     * Multiple languages may share the same extension.
     */
    val extensionToLanguagesMap: Map<String, List<Language>> by lazy {
        languages.flatMap {
            it.value.extensions.map { extension ->
                extension to it.value
            }
        }.groupBy({ it.first }, { it.second })
    }

    /**
     * Map of specific filenames to lists of languages that use that filename.
     *
     * Used for files like "Makefile", "Dockerfile", etc. that don't rely on extensions.
     */
    val filenameToLanguagesMap: Map<String, List<Language>> by lazy {
        languages.flatMap {
            it.value.filenames.map { filename ->
                filename to it.value
            }
        }.groupBy({ it.first }, { it.second })
    }

    init {
        this.linguistFile =
            linguistFile?.let { if (checkLinguistFile(it)) it else null } ?: dxLinguistHome.resolve("languages.yml")
        if (!this.linguistFile.exists()) {
            updateLinguistFile()
        }
    }

    private fun readLocalLanguages(): Map<String, Language> {
        try {
            return mapper.readValue<Map<String, LanguageDTO>>(linguistFile).map {
                it.key to Language(
                    it.key,
                    it.value.type,
                    it.value.group,
                    it.value.extensions ?: emptyList(),
                    it.value.filenames ?: emptyList(),
                    it.value.aliases ?: emptyList()
                )
            }.toMap()
        } catch (e: Exception) {
            val defaultLinguistFile = dxLinguistHome.resolve("languages.yml")
            return if (linguistFile.absolutePath != defaultLinguistFile.absolutePath) {
                linguistFile = defaultLinguistFile
                updateLinguistFile()
                readLocalLanguages()
            } else throw e
        }
    }

    /**
     * Retrieves all languages associated with a file path.
     *
     * Looks up languages by file extension first, then by filename if no extension match is found.
     *
     * @param path The file path to analyze (can be absolute or relative).
     * @return List of matching [Language] objects, or empty list if no matches found.
     */
    fun getLanguages(path: String): List<Language> {
        val fileName = Paths.get(path).fileName.toString()
        val extension = ".${fileName.substringAfterLast('.', "")}"
        return extensionToLanguagesMap[extension] ?: filenameToLanguagesMap[fileName] ?: emptyList()
    }

    /**
     * Checks if a file path corresponds to any of the specified languages.
     *
     * Performs case-insensitive matching against language names, aliases, and groups.
     *
     * @param path The file path to check.
     * @param languages The language names to match against (case-insensitive).
     * @return `true` if the file matches any of the specified languages, `false` otherwise.
     */
    fun isOf(path: String, vararg languages: String) = if (languages.isEmpty()) {
        false
    } else {
        val lowerCaseLanguages = languages.map { it.lowercase() }
        getLanguages(path).flatMap { it.aliases + it.name + it.group }.mapNotNull { it?.lowercase() }
            .any(lowerCaseLanguages::contains)
    }

    /**
     * Checks if a language name is registered in the linguist database.
     *
     * Performs case-insensitive matching against both canonical names and aliases.
     *
     * @param language The language name to check.
     * @return `true` if the language is registered, `false` otherwise.
     */
    fun isRegistered(language: String) =
        languages.flatMap { it.value.aliases + it.key }.map { it.lowercase() }.contains(language.lowercase())

    private fun getGithubLinguistFile() = URI(githubLinguistFileUrl).toURL().readText()

    /**
     * Downloads the latest language definitions from GitHub Linguist.
     *
     * Fetches the languages.yml file from the GitHub Linguist repository and
     * saves it to the local linguist file location.
     */
    fun updateLinguistFile() {
        val languages: Map<String, LanguageDTO> = mapper.readValue(getGithubLinguistFile())
        linguistFile.apply { createNewFile() }.also { mapper.writeValue(it, languages) }
    }

    private data class LanguageDTO(
        val type: String,
        val group: String?,
        val extensions: List<String>?,
        val filenames: List<String>?,
        val aliases: List<String>?
    )
}
