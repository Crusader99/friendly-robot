package de.crusader.friendlyrobot

import de.crusader.friendlyrobot.parser.LatexParser
import de.crusader.objects.color.Color
import org.languagetool.JLanguageTool
import org.languagetool.language.GermanyGerman

fun main() {

    val latex = """
\title{Test}
\subtitle{Test2}
""".trimIndent()

    val parsed = LatexParser(latex)
    parsed.printLatexColored()
    print(Color.DARK_RED.unix("\n\n---\n\n"))
    print(parsed.toPlainText())
    print(Color.DARK_RED.unix("\n\n---\n\n"))

    val annotated = parsed.toAnnotatedText()

    val langTool = JLanguageTool(GermanyGerman())
    val matches = langTool.check(annotated)
    for (match in matches) {
        val sentence = parsed.toPlainText(match.fromPos..match.toPos)
        val from = parsed.indexToLocation(match.fromPos)
        val to = parsed.indexToLocation(match.toPos)
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