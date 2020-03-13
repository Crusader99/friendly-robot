package de.crusader.friendlyrobot.packages.standard

import de.crusader.friendlyrobot.packages.LatexPackage

/**
 * Contains only commands defined in standard latex package
 */
class LatexStandardPackage : LatexPackage {

    /**
     * Called when command parsed from latex source.
     *
     * @param commandName -     Name of the parsed command, including \ as first char
     * @param parameters -      Parameters of command, makes not difference between bracket types
     * @return                  possible plain-text replacement of this command.
     *                          For example: This could be the first parameter for '\title'.
     *                          Commands that return null will be ignored and removed from
     *                          plain text.
     */
    override fun onCommand(commandName: String, parameters: Array<String>): String? {
        if (commandName.length == 2 && commandName[0] == '\\') {
            val char = commandName[1]
            return if (char == ',') {
                // Replace protected space to protected space
                "\u202F"
            } else {
                // Keep other characters
                char.toString()
            }
        }

        // Support custom line breaks
        if (commandName == "\\newline") {
            return "\n"
        }

        // Ignore other commands and commands for other packages
        return null

    }

}