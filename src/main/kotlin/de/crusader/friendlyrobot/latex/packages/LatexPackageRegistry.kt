package de.crusader.friendlyrobot.latex.packages

import de.crusader.friendlyrobot.latex.packages.eurosym.LatexEurosymPackage
import de.crusader.friendlyrobot.latex.packages.standard.LatexStandardPackage
import de.crusader.friendlyrobot.latex.packages.unknown.LatexUnknownPackages

/**
 * All used or available packages are registered and loaded in this class
 */
class LatexPackageRegistry : LatexPackage {

    /**
     * All registered packages wich are able to handle latex commands
     */
    private val registered = mutableListOf<LatexPackage>()

    init {
        // Register all available packages
        registered += LatexStandardPackage()
        registered += LatexEurosymPackage()
        registered += LatexUnknownPackages()
    }

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
        for (cmd in registered) {
            val replacement = cmd.onCommand(commandName, parameters)
            if (replacement != null) {
                return replacement
            }
        }

        // No registered latex package was able to handle this command so ignore this command
        return null
    }

}