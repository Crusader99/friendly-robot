package de.crusader.friendlyrobot

import de.crusader.extensions.printErr
import de.crusader.extensions.toFullString
import de.crusader.extensions.toOptimizedLine
import de.crusader.friendlyrobot.commands.CheckCommand
import de.crusader.friendlyrobot.latex.LatexParser
import java.io.File
import java.nio.charset.Charset

/**
 * The main class of the application, will be called on startup
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("No parameters provided. Starting in interactive mode... Type 'check --help' for help.")
        for (commandLine in generateSequence { readLine() }) {
            executeCommand(commandLine)
        }
    } else {
        val commandLine = args.joinToString(" ")
        executeCommand(commandLine)
    }
}

/**
 * Handle command by given commandline
 */
private fun executeCommand(commandLine: String) {
    val cmd = CheckCommand()
    try {
        cmd.parse(commandLine)
    } catch (ex: Exception) {
        printErr(ex.toOptimizedLine(false))
        println(cmd.getSynopsis())
        return
    }
    try {
        if (cmd.help == null) {
            // Execute command
            cmd.execute()
        } else {
            // Print help page
            println(cmd.getManual())
        }
    } catch (ex: Exception) {
        val error: String = if (cmd.details == true) {
            ex.toFullString()
        } else {
            ex.toOptimizedLine(false)
        }
        printErr(error)
    }
}

/**
 * Loads and parses a latex file with a specific encoding.
 *
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
