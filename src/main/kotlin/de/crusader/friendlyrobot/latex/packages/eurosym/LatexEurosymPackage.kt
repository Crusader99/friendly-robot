package de.crusader.friendlyrobot.latex.packages.eurosym

import de.crusader.friendlyrobot.latex.packages.LatexPackage

/**
 * Contains only commands defined in eurosym latex package
 */
class LatexEurosymPackage : LatexPackage {

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
        if (commandName == "\\euro") {
            return "\t€"
        } else if (commandName == "\\EUR" && parameters.size == 1) {
            val content = parameters[0]
            return "$content €"
        }

        // Ignore other commands and commands for other packages
        return null
    }

}