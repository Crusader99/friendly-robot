package de.crusader.friendlyrobot.parser

/**
 * Contains the specific context for each index in input text.
 * Implemented in own data structure to make access faster
 */
class ContextHistory<CONTEXT>(defaultContext: CONTEXT) {
    private val firstSwitch = ContextObject(0, defaultContext)
    private var lastSwitch = firstSwitch

    operator fun set(index: Int, context: CONTEXT) {
        val new = ContextObject(index, context)
        lastSwitch += new
        if (new.startIndex > lastSwitch.startIndex) {
            lastSwitch = new
        }
    }

    operator fun get(index: Int): CONTEXT? = lastSwitch[index]?.context

    fun asSortedSequence() = firstSwitch.higherIndices()

    fun getContextAtIndex(index: Int): ContextObject<CONTEXT> =
            lastSwitch[index] ?: lastSwitch.lowerIndices()
                    .filter { it.startIndex <= index }
                    .maxByOrNull { it.startIndex }
            ?: throw IllegalStateException("No context found at index $index")

}