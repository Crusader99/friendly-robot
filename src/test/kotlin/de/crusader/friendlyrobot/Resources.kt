package de.crusader.friendlyrobot

import de.crusader.extensions.readText
import java.io.File
import java.io.IOException

/**
 * Contains helper functions for reading string from resources
 */
object Resources {

    /**
     * Loads project resource by given name and returns the text
     *
     * @param resourceName the path of the resource
     * @return the loaded string
     */
    fun getText(resourceName: File): String {
        try {
            return Resources::class.java.getResourceAsStream(resourceName.path).buffered().readText()
        } catch (ex: java.lang.Exception) {
            throw IOException("Unable to load ressource: $resourceName", ex)
        }
    }

}