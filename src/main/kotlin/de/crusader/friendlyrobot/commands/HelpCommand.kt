package de.crusader.friendlyrobot.commands

import de.crusader.extensions.println

/**
 * Help command implementation.
 *
 * Usage:
 * - Create new instance
 * - Call parse(...) method on it
 * - Call execute() function
 */
open class HelpCommand : Command("help", "Print help to available commands") {

    /**
     * Execute this command.
     *
     * Note: The parse(...) method for this command need to be called first.
     */
    override fun execute() {
        registeredCommands.joinToString("\n---\n") { it.getManual() }.println()
    }

}