package org.dxworks.linguist

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class LinguistTest {

    private val linguist = Linguist()

    @Test
    fun isOf() {
        assertTrue { linguist.isOf("src/Main.java", "Java") }

        assertTrue { linguist.isOf("src/main.hh", "C++") }
        assertTrue { linguist.isOf("src/main.hh", "cpp") }

        assertTrue { linguist.isOf("src/comp/script.tsx", "TSX") }
        assertTrue { linguist.isOf("src/comp/script.tsx", "TypeScript") }

        assertTrue { linguist.isOf("opt/sshconfig", "SSH Config") }
    }
}
