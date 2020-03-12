package de.crusader.friendlyrobot.commands

import de.crusader.args.DeclaredCommand
import org.languagetool.Language
import org.languagetool.Languages
import java.nio.charset.Charset

abstract class Command(name: String, description: String?) : DeclaredCommand(name, description) {

    /**
     * Details option can be enabled with --details
     * Will print more details to exceptions and parsed latex file
     */
    val details by optionOfBoolean("Show more details to parsed latex and exceptions in console")

    /**
     * Print help page to this command
     * Can be enabled with --help
     */
    val help by optionOfUnit("Print help page to this command")

    /**
     * Converts name of charset to charset instance.
     * May throw an exception if charset not supported.
     *
     * @return Matching charset instance
     */
    fun String?.toCharset(): Charset {
        return if (this == null) {
            try {
                Charset.defaultCharset()
            } catch (ex: Exception) {
                // Log exception
                ex.printStackTrace()
                Charsets.UTF_8
            }
        } else {
            try {
                Charset.forName(this) ?: throw NullPointerException("Returned charset is null")
            } catch (ex: Exception) {
                val supported = Charset.availableCharsets().keys.joinToString()
                throw UnsupportedOperationException("Encoding $this seems not to be supported. Supported: $supported", ex)
            }
        }
    }

    /**
     * Convert string to supported language object.
     * Will throw an exception if language not supported.
     *
     * @return Langage object for the requested language
     */
    fun String.toLanguageObject(): Language {
        val languages = Languages.get()
        return languages.find { it.name.equals(this, true) }
                ?: languages.find { it.shortCodeWithCountryAndVariant.equals(this, true) }
                ?: languages.find { it.shortCode.equals(this, true) }
                ?: languages.find { it.localeWithCountryAndVariant.displayName.equals(this, true) }
                ?: languages.findLast { it.locale.displayName.equals(this, true) }
                ?: throw UnsupportedOperationException("Language with name '$this' not supported! Supported: " + languages.map { it.shortCodeWithCountryAndVariant })
    }

    abstract fun execute()

}