package org.dxworks.linguist

data class Language(
    val name: String,
    val type: String,
    val group: String?,
    val extensions: List<String>,
    val filenames: List<String>,
    val aliases: List<String>
)
