package de.crusader.friendlyrobot.parser

/**
 * Linked data structure for fast access to context objects
 */
class ContextObject<CONTEXT>(
        val startIndex: Int,
        context: CONTEXT
) {

    var context: CONTEXT = context
        private set

    var replacement: String? = null
        internal set(value) {
            field = if (value.isNullOrEmpty()) {
                null
            } else {
                value
            }
        }

    private var higherIndex: ContextObject<CONTEXT>? = null
    private var lowerIndex: ContextObject<CONTEXT>? = null

    internal fun higherIndices() = generateSequence(this) { obj ->
        obj.higherIndex
    }

    internal fun lowerIndices() = generateSequence(this) { obj ->
        obj.lowerIndex
    }

    internal operator fun get(searchedIndex: Int): ContextObject<CONTEXT>? {
        try {
            val higherIndex = higherIndex
            val lowerIndex = lowerIndex
            return when {
                // Searched for this object
                searchedIndex == this.startIndex -> this

                // Search object is in higher position
                higherIndex != null && searchedIndex >= higherIndex.startIndex -> higherIndex[startIndex]

                // Search object is in lower position
                lowerIndex != null && searchedIndex <= lowerIndex.startIndex -> lowerIndex[startIndex]

                // No object for this index available
                else -> null
            }
        } catch (ex: Exception) {
            throw IllegalStateException("Context search failed for index $searchedIndex", ex)
        }
    }

    internal operator fun plusAssign(new: ContextObject<CONTEXT>) {
        val insertIndex = new.startIndex
        val lower = findLessOrEqual(insertIndex)
        if (lower.startIndex == insertIndex) {
            lower.context = new.context
            lower.replacement = null
            return
        }

        val higher = lower.higherIndex
        lower.higherIndex = new

        // Update new lower element to higher element
        higher?.lowerIndex = new
    }

    private fun findLessOrEqual(searchedIndex: Int): ContextObject<CONTEXT> {
        return if (this.startIndex <= searchedIndex) {
            this
        } else {
            lowerIndex?.findLessOrEqual(searchedIndex)
                    ?: throw IllegalStateException("No context switch less or equal to $searchedIndex")
        }
    }

}