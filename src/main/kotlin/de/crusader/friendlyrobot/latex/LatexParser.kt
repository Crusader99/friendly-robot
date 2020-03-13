package de.crusader.friendlyrobot.latex

import de.crusader.extensions.plusAssign
import de.crusader.extensions.printErr
import de.crusader.friendlyrobot.latex.packages.LatexPackageRegistry
import de.crusader.friendlyrobot.parser.Parser
import org.languagetool.markup.AnnotatedText
import org.languagetool.markup.AnnotatedTextBuilder


/**
 * The latex parser which takes an input latex text and detects contexts of the input text.
 * The contexts can be converted to plain text, used for syntax highlighting or
 * information extraction.
 */
class LatexParser(input: String) : Parser<Context>(input) {

    /**
     * Count of closing brackets to support inner brackets in commands
     */
    private var closingBracket = '?'

    /**
     * Amount of opening inner brackets in markup content context
     * For example \graphicspath{ {./images/} }
     */
    private var openedInnerBrackets = 0

    /**
     * True when previous plain text contains space at end
     */
    private var spaceInserted = true

    /**
     * Last parsed command name
     */
    private lateinit var commandName: String

    /**
     * Handles latex commands with help from registered packages
     */
    private val packageRegistry = LatexPackageRegistry()

    /**
     * Context for input text on first character
     */
    override val startContext: Context
        get() = Context.TEXT

    /**
     * Extension property for characters.
     * Checks if character is linebreak.
     */
    private val Char.isLinebreak
        get() = this == '\r' || this == '\n'

    /**
     * Extension property for characters.
     * Returns true when it is whitespace or linebreak
     */
    private val Char.isSpace
        get() = isWhitespace() || isLinebreak

    /**
     * A call to the underlying parser for a specific context.
     */
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

    /**
     * Parses latex input text while in normal text context
     */
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

    /**
     * Parses latex input text while in space context.
     * Space context is wherever there are spaces.
     */
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

    /**
     * Parses latex input text while in quotation marks context
     */
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

    /**
     * Parses latex input text while in command context
     */
    private fun parseComment() {
        // Leave comment after line break
        if (char.isLinebreak) {
            currentContext = Context.SPACE
            parseSpace()
        }
    }

    /**
     * Parses latex input text while in markup-command-context
     */
    private fun parseMarkupCommand() {
        if (char == '{' || char == '[') {
            // Save some information for later
            closingBracket = char.opposite
            openedInnerBrackets = 0
            commandName = currentContextString

            // Jump to markup content
            nextContext = Context.MARKUP_CONTENT
        } else if (currentContextLength == 1 && !char.isLetter()) {
            // Handle short commands like '\"'
            currentContextReplacement = packageRegistry.onCommand("\\$char", emptyArray())

            // Leave for things like \\ or \, or things like that
            nextContext = Context.TEXT
        } else if (char.isSpace) {
            // Handle longer commands but without parameters (for example: '\newline')
            currentContextReplacement = packageRegistry.onCommand(currentContextString, emptyArray())

            // Jump back to text for thing like \item ...
            currentContext = Context.SPACE
        }
    }

    /**
     * Parses latex input text while in markup-content-context
     */
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
                // Remove nested latex commands
                val content = LatexParser(currentContextString).toPlainText()

                // Handle longer commands but with parameters (for example: '\item{Test}')
                currentContextReplacement = packageRegistry.onCommand(commandName, arrayOf(content))

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

    /**
     * Parses latex input text while in math context
     */
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

    /**
     * Converts parsed latex to annotated text.
     * @return Annotated text instance to be used in language tool
     */
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

    /**
     * Prints input latex source to console with syntax highlighting.
     * Can be used to debug parse mistakes.
     */
    fun printLatexColored() {
        forEach { switch, part ->
            val formatted = switch.context.fgColor.unix(part)
            print(switch.context.bgColor.unixBackgound(formatted))
        }
        println()
    }

    /**
     * Converts the parsed latex to plain text
     *
     * @param range - Index range of input source to be included in plain text
     * @return Parsed plain text
     */
    @JvmOverloads
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