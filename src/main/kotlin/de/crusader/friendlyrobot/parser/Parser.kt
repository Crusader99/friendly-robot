package de.crusader.friendlyrobot.parser

/**
 * Abstract parser class which takes an input text and detects contexts of the input text.
 * The contexts can be converted to plain text, used for syntax highlighting or
 * information extraction.
 */
abstract class Parser<CONTEXT>(val input: String) {

    /**
     * Context change for each index
     */
    private val contextHistory by BeforeFirstAccessHandler(ContextHistory(startContext)) {
        // Starts parsing before first access to this variable
        // Should prevent missing or double parse() call
        // May not be compatible with multi threading
        parse()
    }

    /**
     * Current index in latex text
     */
    protected var index = 0
        private set

    /**
     * Current analyzed char
     */
    protected val char: Char
        get() = input[index]

    /**
     * The char at the next index position
     */
    protected val nextChar: Char?
        get() = getCharAt(index + 1)

    /**
     * The char at the previous index position
     */
    protected val previousChar: Char?
        get() = getCharAt(index - 1)

    /**
     * Current context for current index
     */
    protected var currentContext: CONTEXT
        get() = getContextAtIndex(index)
        set(context) {
            contextHistory[index] = context
        }

    /**
     * Context for following indices
     */
    protected var nextContext: CONTEXT
        get() = getContextAtIndex(index + 1)
        set(context) {
            contextHistory[index + 1] = context
        }

    /**
     * Text of current context, has length of 1 at first char
     */
    protected val currentContextString: String
        get() = input.substring(getContextStart(index), index)

    /**
     *  Text length of current context, will be 1 at first char
     */
    protected val currentContextLength: Int
        get() = index - getContextStart(index)

    /**
     * First char of current context
     */
    protected val currentContextStart: Char
        get() = input[getContextStart(index)]

    /**
     * Set or get plain text replacement for the current context
     */
    protected var currentContextReplacement: String?
        get() = contextHistory.getContextAtIndex(index).replacement
        set(value) {
            contextHistory.getContextAtIndex(index).replacement = value
        }

    /**
     * Default start context for parsing
     * Should be overwritten by sub class
     */
    protected abstract val startContext: CONTEXT

    /**
     * Returns the opposite bracket, useful for most parsers
     */
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

    /**
     * Get context starting at specific index (including that index)
     *
     * @param index - The index of the requested context based on the index text
     * @return Context at the given index position
     */
    protected fun getContextAtIndex(index: Int) =
            contextHistory.getContextAtIndex(index).context

    /**
     * Get context starting at specific index (including that index)
     *
     * @param index - The index of the requested context-start based on the index text
     * @return Index position of context start
     */
    protected fun getContextStart(index: Int) =
            contextHistory.getContextAtIndex(index).startIndex

    /**
     * Get chat at specific position from input text
     *
     * @return chat at index or null when index out of range
     */
    protected fun getCharAt(index: Int): Char? =
            if (index in input.indices) {
                input[index]
            } else {
                null
            }

    /**
     * Starts parsing the input text and creates a context history.
     * The function should be called only once. Usually called on init block.
     */
    private fun parse() {
        try {
            for (index in input.indices) {
                this.index = index
                callContext(currentContext)
            }
        } catch (ex: Exception) {
            throw IllegalStateException("Latex parsing failed in " + index.loc, ex)
        }
    }

    /**
     * A call to the underlying parser for a specific context.
     * Should be implemented by sub class
     */
    protected abstract fun callContext(context: CONTEXT)

    /**
     * Converts a specific index of the input text to line and column position as text
     *
     * @param indexPosition - Index in input text
     * @return Line and column position as text
     */
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

    /**
     * Simplifies the indexToLocation(...) call. Converts a specific index
     * of the input text to line and column position as text

     * @return Line and column position as text
     */
    protected val Int.loc
        get() = indexToLocation(this)

    /**
     * Calls callback for each part of context history.
     *
     * @param callback - Callback for ContextObject and the raw input string for that part
     */
    fun forEach(callback: (ContextObject<CONTEXT>, String) -> Unit) {
        lateinit var lastSwitch: ContextObject<CONTEXT>

        fun append(endIndex: Int) {
            val lastPart = input.substring(lastSwitch.startIndex, endIndex)
            callback(lastSwitch, lastPart)
        }

        for ((index, newSwitch) in contextHistory.asSortedSequence().withIndex()) {
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