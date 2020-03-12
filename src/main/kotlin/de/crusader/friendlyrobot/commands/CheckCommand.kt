package de.crusader.friendlyrobot.commands

import de.crusader.friendlyrobot.parseLatexFile
import de.crusader.objects.color.Color
import org.languagetool.JLanguageTool
import java.io.File


class CheckCommand : Command("check", "Checks spell and grammar of latex source file") {

    /**
     * Required argument: path to the latex source code
     */
    val latexSourcePath by argumentOfString("input.tex")

    /**
     * Optional argument: language for spell and grammar checking
     * Default language is set to english
     */
    val language by argumentOfString("en")

    /**
     * Encoding-Option can be set with --encoding=UTF-16
     * Allows to set the charset of the latex source
     */
    val encoding by optionOfString("Allows to set the charset of the latex source. Set to system default if not defined")

    override fun execute() {
        // Get latex source file
        val latexSourceFile = File(latexSourcePath)

        // Check if language supported and return object instance
        val language = language.toLanguageObject()

        // Find requested charset
        val charset = encoding.toCharset()

        // Load and parse latex source file
        val parsedLatexSource = parseLatexFile(latexSourceFile, charset)

        // Print parsed source text output to console
        if (details == true) {
            parsedLatexSource.printLatexColored()
            println(Color.DARK_RED.unix("\n\n---\n\n"))
        }

        // Print plain text output to console
        if (details == true || details == null) {
            println(parsedLatexSource.toPlainText())
            print(Color.DARK_RED.unix("\n\n---\n\n"))
        }

        // Convert latex source to annotated text
        val annotatedText = parsedLatexSource.toAnnotatedText()

        val tool = JLanguageTool(language)

        val matches = tool.check(annotatedText)
        for (match in matches) {
            val sentence = parsedLatexSource.toPlainText(match.fromPos..match.toPos)
            val from = parsedLatexSource.indexToLocation(match.fromPos)
            val to = parsedLatexSource.indexToLocation(match.toPos)
            val errorMessage = match.message.trim()
            println("> $sentence")
            print(Color.RED.unix("Potential error at characters "))
            print(Color.DARK_RED.unixBackgound(from))
            print(Color.RED.unix(" - "))
            print(Color.DARK_RED.unixBackgound(to))
            print(Color.RED.unix(": "))
            println(errorMessage)
            println(Color.GREEN.unix("Suggested correction(s): ") + Color.DARK_GREEN.unix(match.suggestedReplacements.toString()))
            println()
            println()
        }
    }

}