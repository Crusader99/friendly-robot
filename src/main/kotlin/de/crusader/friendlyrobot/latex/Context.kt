package de.crusader.friendlyrobot.latex

import de.crusader.objects.color.Color
import de.crusader.objects.color.KnownColor


/**
 * Defines possible contexts in latex source code
 */
enum class Context(val fgColor: KnownColor, val bgColor: KnownColor) {
    TEXT(Color.WHITE, Color.BLACK),
    SPACE(Color.WHITE, Color.GRAY),
    COMMENT(Color.YELLOW, Color.AQUA),
    MARKUP_COMMAND(Color.YELLOW, Color.RED),
    MARKUP_CONTENT(Color.WHITE, Color.MAGENTA),
    QUOTATION_MARKS(Color.ORANGE, Color.BLACK),
    MATH(Color.WHITE, Color.DARK_RED)
}