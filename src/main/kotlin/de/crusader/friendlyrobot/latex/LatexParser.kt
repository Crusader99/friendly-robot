package de.crusader.friendlyrobot.latex

import de.crusader.extensions.plusAssign
import de.crusader.extensions.printErr
import de.crusader.friendlyrobot.parser.Parser
import org.languagetool.markup.AnnotatedText
import org.languagetool.markup.AnnotatedTextBuilder


/**
 * The latex parser which takes an input latex text and detects contexts of the input text.
 * The contexts can be converted to plain text, used for syntax highlighting or
 * information extraction.
 */
class LatexParser(input: String) : Parser<Context>(input) {

    private var closingBracket = '?'

    // Amount of opening inner brackets in markup content context
    // For example \graphicspath{ {./images/} }
    private var openedInnerBrackets = 0

    private var spaceInserted = true
    private lateinit var commandName: String

    override val defaultContext: Context
        get() = Context.TEXT

    private val Char.isLinebreak
        get() = this == '\r' || this == '\n'

    private val Char.isSpace
        get() = isWhitespace() || isLinebreak

    override fun callContext(context: Context) {
        when (context) {
            Context.TEXT -> parseText()
            Context.SPACE -> parseSpace()
            Context.COMMENT -> parseComment()
            Context.MARKUP_COMMAND -> parseMarkupCommand()
            Context.MARKUP_CONTENT -> parseMarkupContent()
            Context.QUOTATION_MARKS -> parseQuotationMarks()
            Context.MATH -> parseMaths()
        }
    }

    private fun parseText() {
        if (char.isSpace) {
            // Join space area
            currentContext = Context.SPACE
        } else if (char == '%') {
            // Join comment
            currentContext = Context.COMMENT
        } else if (char == '\\') {
            // Join markup
            currentContext = Context.MARKUP_COMMAND
        } else if (char == '"') {
            // Join quotation-marks
            currentContext = Context.QUOTATION_MARKS
        } else if (char == '{') {
            // Support multiple sub brackets
            openedInnerBrackets = 0

            // Join math context
            currentContext = Context.MATH
        } else {
            // Allow chars to be inserted
            spaceInserted = false

            // Print warning
            if (char == '.' && previousChar?.isLetter() == true && nextChar?.isLetter() == true) {
                printErr("Missing space after dot: ${index.loc} ($previousChar$char$nextChar)")
            }
        }
    }

    private fun parseSpace() {
        if (!char.isSpace) {
            if (!spaceInserted) {
                // Replace multiple chars to one char
                currentContextReplacement = " "

                // Prevent multiple space chars
                spaceInserted = true
            }

            // Leave space area
            currentContext = Context.TEXT

            // Handle text parser
            parseText()
        }
    }

    private fun parseQuotationMarks() {
        // Set default replacement
        currentContextReplacement = "\""

        if (char == '`') {
            currentContextReplacement = "„"
            nextContext = Context.TEXT
        } else if (char == '\'') {
            currentContextReplacement = "“"
            nextContext = Context.TEXT
        } else {
            // Print format warning
            if (char.isLetter()) {
                printErr("Unescaped quotation marks found at ${index.loc}")
            } else {
                printErr("Unknown quotation mark escape found at ${index.loc}: $currentContextString")
            }

            // Switch context to text
            currentContext = Context.TEXT

            // Handle text parser
            parseText()
        }
    }

    private fun parseComment() {
        // Leave comment after line break
        if (char.isLinebreak) {
            currentContext = Context.SPACE
            parseSpace()
        }
    }

    private fun parseMarkupCommand() {
        if (char == '{' || char == '[') {
            // Save some information for later
            closingBracket = char.opposite
            openedInnerBrackets = 0
            commandName = currentContextString

            // Jump to markup content
            nextContext = Context.MARKUP_CONTENT
        } else if (currentContextLength == 1 && !char.isLetter()) {
            currentContextReplacement = if (char == ',') {
                // Replace protected space to protected space
                "\u202F"
            } else {
                // Keep other character
                char.toString()
            }

            // Leave for things like \\ or \, or things like that
            nextContext = Context.TEXT
        } else if (char.isSpace) {
            // Support custom line breaks
            if (currentContextString == "\\newline") {
                currentContextReplacement = "\n"
            }

            // Jump back to text for thing like \item ...
            currentContext = Context.SPACE
        }
    }

    private fun parseMarkupContent() {
        if (char == closingBracket) {
            if (openedInnerBrackets > 0) {
                // Found end of inner bracket
                // For example \graphicspath{ {./images/} }
                openedInnerBrackets--
            } else if (nextChar == '{' || nextChar == '[') {
                // Jump back to markup command when multiple brackets
                currentContext = Context.MARKUP_COMMAND
            } else {
                if (commandName.removeSuffix("*").endsWith("section") || commandName.endsWith("title")) {
                    // Set replacement for title section
                    currentContextReplacement = "\n\n$currentContextString.\n"
                } else if (commandName == "\\underline") {
                    // Ignore underline and print normal text
                    currentContextReplacement = currentContextString
                } else if (commandName == "\\item") {
                    // Convert text parameter to plain text replacement
                    currentContextReplacement = LatexParser(currentContextString).toPlainText()
                } else if (commandName == "\\ref") {
                    // Default replacement for reference
                    currentContextReplacement = "1.0.0"
                } else if (commandName == "\\ac") {
                    // Default replacement for definition text
                    currentContextReplacement = "Text"
                }

                // Mark enclosing brackets as command
                currentContext = Context.MARKUP_COMMAND

                // Jump back to text on closing bracket
                nextContext = Context.TEXT
            }
        } else if (char == closingBracket.opposite) {
            // Found opening inner bracket
            // For example \graphicspath{ {./images/} }
            openedInnerBrackets++
        }
    }

    private fun parseMaths() {
        // Leave after bracket
        if (char == '}') {
            if (openedInnerBrackets > 0) {
                // Found end of inner bracket
                openedInnerBrackets--
            } else {
                // Calculate full math context
                val mathLatex = currentContextString
                        .removePrefix("{")
                        .removeSuffix("}")
                currentContextReplacement = LatexParser(mathLatex).toPlainText()
                nextContext = Context.TEXT
            }
        } else if (char == '{') {
            openedInnerBrackets++
        }
    }

    fun toAnnotatedText(): AnnotatedText {
        val builder = AnnotatedTextBuilder()
        forEach { switch, part ->
            if (switch.context == Context.TEXT) {
                builder.addText(part)
            } else if (switch.replacement != null) {
                builder.addMarkup(part, switch.replacement)
            } else {
                builder.addMarkup(part)
            }
        }
        return builder.build()
    }

    fun printLatexColored() {
        forEach { switch, part ->
            val formatted = switch.context.fgColor.unix(part)
            print(switch.context.bgColor.unixBackgound(formatted))
        }
        println()
    }

    fun toPlainText(range: IntRange? = null): String {
        val builder = StringBuilder()
        forEach { switch, part ->
            val context = switch.context
            val replacement = switch.replacement
            val append = if (context == Context.TEXT) {
                part
            } else replacement ?: return@forEach

            if (range != null && switch.startIndex !in range && switch.startIndex + append.length !in range) {
                return@forEach
            }
            builder += append
        }
        return builder.toString().trim()
    }

}