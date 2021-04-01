package org.dxworks.linguist

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.net.URL
import java.nio.file.Paths


private const val githubLinguistFileUrl =
    "https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml"
val dxLinguistHome by lazy {
    Paths.get(System.getProperty("user.home")).resolve(".linguist").toFile().apply { mkdirs() }
}

class Linguist(linguistFile: File? = null) {
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        .apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

    private var linguistFile: File

    private fun checkLinguistFile(it: File): Boolean {
        return it.exists() && (it.name.endsWith(".yml") || it.name.endsWith(".yaml"))
    }

    val languages: Map<String, Language> by lazy { readLocalLanguages() }
    val extensionToLanguagesMap: Map<String, List<Language>> by lazy {
        languages.flatMap {
            it.value.extensions.map { extension ->
                extension to it.value
            }
        }.groupBy({ it.first }, { it.second })
    }
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

    fun getLanguages(path: String): List<Language> {
        val fileName = Paths.get(path).fileName.toString()
        val extension = ".${fileName.substringAfterLast('.', "")}"
        return extensionToLanguagesMap[extension] ?: filenameToLanguagesMap[fileName] ?: emptyList()
    }

    fun isOf(path: String, vararg languages: String) = if (languages.isEmpty()) {
        false
    } else {
        val lowerCaseLanguages = languages.map { it.toLowerCase() }
        getLanguages(path).flatMap { it.aliases + it.name + it.group }.mapNotNull { it?.toLowerCase() }
            .any(lowerCaseLanguages::contains)
    }

    fun isRegistered(language: String) =
        languages.flatMap { it.value.aliases + it.key }.map { it.toLowerCase() }.contains(language.toLowerCase())

    private fun getGithubLinguistFile() = URL(githubLinguistFileUrl).readText()

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
