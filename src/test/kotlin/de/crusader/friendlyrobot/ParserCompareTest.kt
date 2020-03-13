package de.crusader.friendlyrobot

import de.crusader.friendlyrobot.latex.LatexParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests related to latex parser functionality
 */
class ParserCompareTest {

    /**
     * Compares parsed latex file with plain text file
     */
    @Test
    fun compare() {
        val latexFile = File("/compare/input-source.tex")
        val plainFile = File("/compare/output-expected.txt")

        println("Parsing latex source...")
        val latexText = Resources.getText(latexFile)
        val plainTextActual = LatexParser(latexText).toPlainText()

        println("Comparing actual result with expected result...")
        val plainTextExpected = Resources.getText(plainFile)
        assertEquals(plainTextActual, plainTextExpected)
    }

}