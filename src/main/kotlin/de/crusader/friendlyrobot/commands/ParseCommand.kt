package de.crusader.friendlyrobot.commands

import de.crusader.friendlyrobot.latex.LatexParser
import de.crusader.friendlyrobot.parseLatexFile
import de.crusader.objects.color.Color
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
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
    private val latexSourcePath by argumentOfString()

    /**
     * Encoding-Option can be set with --encoding=UTF-16
     * Allows to set the charset of the latex source
     */
    private val encoding by optionOfString("Allows to set the charset of the latex source. Set to system default if not defined")

    /**
     * Option to enable print of latex markup highlighting
     * Enable this option with: -m=true or --print-latex-markup=true
     */
    protected val printLatexMarkup by optionOfBoolean("Prints syntax highlighting for input latex file", 'm')

    /*
    TODO: add option for enabling/disabling warnings

        protected val printWarnings by optionOfUnit("Prints warnings and hints occurred while parsing", 'w')
     */

    /**
     * Execute this command.
     *
     * Note: The parse(...) method for this command need to be called first.
     */
    override fun execute() {
        // Load and parse latex source file
        val parsedLatexSource = parseLatexFile()

        // Print parsed source text output to console
        if (printLatexMarkup == true) {
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
        val latexSourcePath = latexSourcePath

        // Allow getting text from clipboard instead
        if (latexSourcePath == "clipboard") {
            val toolkit = Toolkit.getDefaultToolkit()
            val clipboard = toolkit.systemClipboard
            val inputSource = clipboard.getData(DataFlavor.stringFlavor).toString()
            return LatexParser(inputSource)
        }

        // Get latex source file
        val latexSourceFile = File(latexSourcePath)

        // Find requested charset
        val charset = encoding.toCharset()

        // Load and parse latex source file
        return parseLatexFile(latexSourceFile, charset)

    }

}