package de.crusader.friendlyrobot

abstract class Parser<CONTEXT>(val input: String) {

    // Context change for each index
    private val contextSwitches = ContextHistory(defaultContext)

    // Current index in latex text
    protected var index = 0
        private set

    // Current analyzed char
    protected val char: Char
        get() = input[index]

    protected val nextChar: Char?
        get() = getCharAt(index + 1)

    protected val previousChar: Char?
        get() = getCharAt(index - 1)

    // Current context for current index
    protected var currentContext: CONTEXT
        get() = getContextAtIndex(index)
        set(context) {
            contextSwitches[index] = context
        }

    // Context for following indices
    protected var nextContext: CONTEXT
        get() = getContextAtIndex(index + 1)
        set(context) {
            contextSwitches[index + 1] = context
        }

    // Text of current context, has length of 1 at first char
    protected val currentContextString: String
        get() = input.substring(getContextStart(index), index)

    // Text length of current context, will be 1 at first char
    protected val currentContextLength: Int
        get() = index - getContextStart(index)

    // First char of current context
    protected val currentContextStart: Char
        get() = input[getContextStart(index)]

    protected var currentContextReplacement: String?
        get() = contextSwitches.getContextAtIndex(index).replacement
        set(value) {
            contextSwitches.getContextAtIndex(index).replacement = value
        }

    protected abstract val defaultContext: CONTEXT

    protected val Char.opposite: Char
        get() = when (this) {
            '{' -> '}'
            '}' -> '{'
            '[' -> ']'
            ']' -> '['
            '<' -> '>'
            '>' -> '<'
            else -> throw UnsupportedOperationException("No opposite for $this")
        }

    init {
        parse()
    }

    // Get context starting at specific index (including that index)
    protected fun getContextAtIndex(index: Int) =
            contextSwitches.getContextAtIndex(index).context

    // Get context starting at specific index (including that index)
    protected fun getContextStart(index: Int) =
            contextSwitches.getContextAtIndex(index).startIndex

    protected fun getCharAt(index: Int): Char? =
            if (index in input.indices) {
                input[index]
            } else {
                null
            }

    private fun parse() {
        for (index in input.indices) {
            this.index = index
            callContext(currentContext)
        }
    }

    protected abstract fun callContext(context: CONTEXT)

    fun indexToLocation(indexPosition: Int): String {
        var totalCount = 0
        var lineCount = 0
        for (line in input.lineSequence()) {
            val lineRange = totalCount..(totalCount + line.length)
            totalCount += line.length
            lineCount++
            if (indexPosition in lineRange) {
                val columnCount = indexPosition - lineRange.first + 1
                return "$lineCount:$columnCount ($indexPosition)"
            }
        }
        return "$lineCount:? ($indexPosition)"
    }

    protected val Int.loc
        get() = indexToLocation(this)

    fun forEach(callback: (ContextObject<CONTEXT>, String) -> Unit) {
        lateinit var lastSwitch: ContextObject<CONTEXT>

        fun append(endIndex: Int) {
            val lastPart = input.substring(lastSwitch.startIndex, endIndex)
            callback(lastSwitch, lastPart)
        }

        for ((index, newSwitch) in contextSwitches.asSortedSequence().withIndex()) {
            if (index == 0) {
                lastSwitch = newSwitch
                continue
            }
            if (lastSwitch.context == newSwitch.context) {
                throw IllegalStateException("Switch to same context? Context: ${newSwitch.context}")
            }
            append(newSwitch.startIndex)
            lastSwitch = newSwitch
        }
        append(input.length)
    }

}

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
                    .maxBy { it.startIndex }
            ?: throw IllegalStateException("No context found at index $index")

}

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