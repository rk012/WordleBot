package io.github.rk012.wordlebot

import kotlin.system.exitProcess

private const val USE_SOLUTION_LIST = true
private const val ENGINE_MOVES = 1 // number of moves to look ahead - slows down significantly
private const val INCREASE_MOVES = false // If true, move depth increases each turn

private fun main() {
    val wordList = getResource("/wordle_list.txt")!!.readText().split("\r\n")
    val solutionList = if (USE_SOLUTION_LIST) getResource("/solution_list.txt")!!.readText().split("\r\n") else wordList

    val engine = Engine(wordList, solutionList, ENGINE_MOVES, INCREASE_MOVES)
    var word = engine.nextWord()

    println("""
            How to enter results:
            
            = GREEN
            + YELLOW
            . BLACK
            
            Example: if the result was BLACK-YELLOW-GREEN-YELLOW-YELLOW, enter .+=++
        """.trimIndent()
    )

    while (true) {
        println()
        val wordCount = engine.getCount()

        if (wordCount == 1) {
            println("Final word: $word")
            print("Press Enter to quit.")
            readln()
            exitProcess(0)
        }

        println("Possible words: ${if (wordCount > 5) wordCount else engine.currentList.toString()}")
        println("Use word: $word")

        print("Enter result: ")

        try {
            word = engine.nextWord(toResultList(readln()))
        } catch (e: NoSuchWordException) {
            println("Filter provided is impossible.")
        }
    }
}

private fun toResultList(input: String): List<Results> {
    val out = mutableListOf<Results>()
    input.forEach {
        out.add(when(it) {
            '+' -> Results.EXISTS
            '=' -> Results.MATCHES
            else -> Results.NONE
        })
    }
    return out
}

private fun getResource(path: String) = object {}.javaClass.getResource(path)