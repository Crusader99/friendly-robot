package de.crusader.friendlyrobot.latex.packages.unknown

import de.crusader.friendlyrobot.latex.packages.LatexPackage

/**
 * Contains commands where is not clear from which package the come from
 */
class LatexUnknownPackages : LatexPackage {

    /**
     * Save shortcuts from \acro command
     */
    private val acRegistry = mutableMapOf<String, String>()

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
        } else if (commandName == "\\textit" && parameters.size == 1) {
            // Print italic as text with quotation marks
            return "„" + parameters[0] + "“"
        } else if (commandName == "\\item") {
            // Convert text parameter to plain text replacement
            return "\n- " + parameters.joinToString(" ")
        } else if (commandName == "\\end") {
            // Convert end of tabular or item-size to newline
            return "\n"
        } else if (commandName == "\\ref") {
            // Default replacement for reference
            return "1.0.0"
        } else if (commandName == "\\ac") {
            // "Text" is the default replacement for definition text
            return acRegistry[parameters.single()] ?: "Text"
        } else if (commandName == "\\acro") {
            // Safe in registry for usage in \ac command
            acRegistry[parameters.first()] = parameters.last()
        } else if (commandName == "\\url" && parameters.isNotEmpty()) {
            // Return url
            return parameters[0]
        } else if (commandName == "\\caption" && parameters.size == 1) {
            // Return caption / image description
            return parameters.single() + "."
        }

        // Ignore other commands and commands for other packages
        return null

    }

}
