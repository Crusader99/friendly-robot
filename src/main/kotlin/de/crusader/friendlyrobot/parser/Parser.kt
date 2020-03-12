package de.crusader.friendlyrobot.parser

import de.crusader.friendlyrobot.context.ContextHistory
import de.crusader.friendlyrobot.context.ContextObject

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