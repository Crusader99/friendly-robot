package de.crusader.friendlyrobot.latex.packages

/**
 * Interface for latex packages. The latex packages are able
 * to accept commands and replace them with other text.
 */
interface LatexPackage {

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
    fun onCommand(commandName: String, parameters: Array<String>): String?

}