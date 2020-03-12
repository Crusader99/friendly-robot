package de.crusader.friendlyrobot.commands

import de.crusader.friendlyrobot.latex.LatexParser
import de.crusader.friendlyrobot.parseLatexFile
import de.crusader.objects.color.Color
import java.io.File

/**
 * Command implementation for parsing latex source file and printing extracted plain text.
 *
 * Usage:
 * - Create new instance
 * - Call parse(...) method on it
 * - Call execute() function
 */
open class ParseCommand(
        name: String = "parse",
        description: String? = "Parses latex source file and print extracted plain text"
) : Command(name, description) {

    /**
     * Required argument: path to the latex source code
     */
    private val latexSourcePath by argumentOfString("input.tex")

    /**
     * Encoding-Option can be set with --encoding=UTF-16
     * Allows to set the charset of the latex source
     */
    private val encoding by optionOfString("Allows to set the charset of the latex source. Set to system default if not defined")

    /**
     * Execute this command.
     *
     * Note: The parse(...) method for this command need to be called first.
     */
    override fun execute() {
        // Load and parse latex source file
        val parsedLatexSource = parseLatexFile()

        // Print parsed source text output to console
        if (details == true) {
            parsedLatexSource.printLatexColored()
            println(Color.DARK_RED.unix("\n\n---\n\n"))
        }

        // Print plain text output to console
        println(parsedLatexSource.toPlainText())
    }

    /**
     * Load and parse latex file from given command line parameter
     *
     * @return LatexParser with parsed latex content
     */
    protected fun parseLatexFile(): LatexParser {
        // Get latex source file
        val latexSourceFile = File(latexSourcePath)

        // Find requested charset
        val charset = encoding.toCharset()

        // Load and parse latex source file
        return parseLatexFile(latexSourceFile, charset)

    }

}