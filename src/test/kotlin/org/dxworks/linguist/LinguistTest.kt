package org.dxworks.linguist

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class LinguistTest {

    private val linguist = Linguist()

    private val bundledLinguist =
        Linguist(File(Linguist::class.java.getResource("/org/dxworks/linguist/languages.yml")!!.toURI()))

    @Test
    fun isOf() {
        assertTrue { linguist.isOf("src/Main.java", "Java") }

        assertTrue { linguist.isOf("src/main.hh", "C++") }
        assertTrue { linguist.isOf("src/main.hh", "cpp") }

        assertTrue { linguist.isOf("src/comp/script.tsx", "TSX") }
        assertTrue { linguist.isOf("src/comp/script.tsx", "TypeScript") }

        assertTrue { linguist.isOf("opt/sshconfig", "SSH Config") }
    }

    @Test
    fun bundledFileListsTheUsualLanguageFirst() {
        assertEquals("PHP", bundledLinguist.getLanguages("src/index.php").first().name)
        assertEquals("Rust", bundledLinguist.getLanguages("src/main.rs").first().name)
        assertEquals("Markdown", bundledLinguist.getLanguages("README.md").first().name)
        assertEquals("SQL", bundledLinguist.getLanguages("db/schema.sql").first().name)

        assertTrue { bundledLinguist.isOf("src/index.php", "Hack") }
    }
}
