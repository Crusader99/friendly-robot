package de.crusader.friendlyrobot.commands

import de.crusader.objects.color.Color
import org.languagetool.JLanguageTool

/**
 * Command implementation for checking spell and grammar of latex source file.
 *
 * Usage:
 * - Create new instance
 * - Call parse(...) method on it
 * - Call execute() function
 */
class CheckCommand : ParseCommand(
        "check",
        "Checks spell and grammar of latex source file"
) {

    /**
     * Optional argument: language for spell and grammar checking
     * Default language is set to english
     */
    private val language by argumentOfString("en")

    /**
     * Execute this command.
     *
     * Note: The parse(...) method for this command need to be called first.
     */
    override fun execute() {
        // Check if language supported and return object instance
        val language = language.toLanguageObject()

        // Load and parse latex source file
        val parsedLatexSource = parseLatexFile()

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

        // Initialize language tool
        val tool = JLanguageTool(language)

        // Check spell and grammar
        val matches = tool.check(annotatedText)

        // Print matches
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