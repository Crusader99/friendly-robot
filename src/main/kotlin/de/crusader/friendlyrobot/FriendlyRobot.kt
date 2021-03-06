package de.crusader.friendlyrobot

import de.crusader.friendlyrobot.commands.Command
import de.crusader.friendlyrobot.latex.LatexParser
import de.crusader.objects.color.Color
import java.io.File
import java.nio.charset.Charset


/**
 * The main class of the application, will be called on startup
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        // Start an interactive mode where user can type commands to input stream
        val info = "No parameters provided. Starting in interactive mode... Type 'help' for help."
        println(Color.YELLOW.unix(info))
        for (commandLine in generateSequence { readLine() }) {
            Command.execute(commandLine)
        }
    } else {
        // Execute by passed arguments
        val commandLine = args.joinToString(" ")
        Command.execute(commandLine)
    }
}


/**
 * Loads and parses a latex file with a specific encoding.
 *
 * @param latexSourceFile - File of the latex source code
 * @param charset - The charset encoding of the latex file
 * @return LatexParser with parsed latex content
 */
fun parseLatexFile(latexSourceFile: File, charset: Charset): LatexParser {
    // Check file exists
    if (!latexSourceFile.exists()) {
        throw IllegalArgumentException("Latex source file does not exist at path: " + latexSourceFile.path)
    }

    // Check file is no directory
    if (latexSourceFile.isDirectory) {
        throw UnsupportedOperationException("Path to latex source file seems to be a directory. Parse of multiple latex files is currently not supported")
    }

    // Load latex source file
    val latexSource = latexSourceFile.readText(charset)

    // Create parser from latex source
    return LatexParser(latexSource)
}
