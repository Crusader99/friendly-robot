package de.crusader.friendlyrobot.packages.unknown

import de.crusader.friendlyrobot.packages.LatexPackage

/**
 * Contains commands where is not clear from which package the come from
 */
class LatexUnknownPackages : LatexPackage {

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
        if (commandName.removeSuffix("*").endsWith("section") || commandName.endsWith("title")) {
            if (parameters.isEmpty()) {
                // Section / title command without parameter?!?
                return null
            }

            // Set replacement for title section
            val title = parameters[0]
            return "\n\n$title.\n"

        } else if (commandName == "\\underline" && parameters.isNotEmpty()) {
            // Ignore underline and print normal text
            return parameters[0]
        } else if (commandName == "\\item" && parameters.isNotEmpty()) {
            // Convert text parameter to plain text replacement
            return parameters[0]
        } else if (commandName == "\\ref") {
            // Default replacement for reference
            return "1.0.0"
        } else if (commandName == "\\ac") {
            // Default replacement for definition text
            return "Text"
        } else if (commandName == "\\url" && parameters.isNotEmpty()) {
            // Return url
            return parameters[0]
        }

        // Ignore other commands and commands for other packages
        return null

    }

}