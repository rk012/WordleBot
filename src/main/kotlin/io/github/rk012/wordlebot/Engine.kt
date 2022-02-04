package io.github.rk012.wordlebot

class Engine internal constructor(private val wordList: List<String>, solutionList: List<String>, private var moves: Int = 1, private val increase_moves: Boolean = false) {
    var currentList = solutionList
        private set

    private var word = "raise" // Calculated first word is always same

    private fun<T> Map<T, Int>.getDefault(key: T): Int = get(key) ?: 0

    private fun matches(word: String, input: String, filter: List<Results>): Boolean {
        val letterCount = mutableMapOf<Char, Int>()
        word.forEach {
            letterCount[it] = letterCount[it]?.plus(1) ?: 1
        }

        filter.forEachIndexed { index, results ->
            if (results == Results.MATCHES) {
                if (input[index] != word[index]) return false
                else letterCount[word[index]] = letterCount.getDefault(word[index]) - 1
            }
            if (results == Results.EXISTS && input[index] == word[index]) return false
        }

        filter.forEachIndexed { index, results ->
            val c = input[index]

            when (results) {
                Results.EXISTS -> if (letterCount.getDefault(c) == 0) return false else letterCount[c] = letterCount.getDefault(c) - 1
                Results.NONE -> if (letterCount.getDefault(c) != 0) return false
                Results.MATCHES -> Unit
            }
        }

        return true
    }

    private fun getFilter(word: String, input: String): List<Results> {
        val filter = MutableList(5) { Results.NONE }

        val letterCount = mutableMapOf<Char, Int>()
        word.forEach {
            letterCount[it] = letterCount.getDefault(it) + 1
        }

        word.forEachIndexed { index, c ->
            if (c == input[index]) {
                filter[index] = Results.MATCHES
                letterCount[c] = letterCount[c]!! - 1
            }
        }

        word.forEachIndexed { index, _ ->
            if (filter[index] != Results.MATCHES && letterCount.getDefault(input[index]) > 0) {
                filter[index] = Results.EXISTS
                //letterCount[c] = letterCount.getDefault(c) - 1
                letterCount[input[index]] = letterCount.getDefault(input[index]) - 1
            }
        }

        return filter
    }

    private fun applyFilter(currentList: List<String>, word: String, filter: List<Results>): List<String> = currentList.filter { matches(it, word, filter) }

    private fun getWord() = getWordlistScores(currentList, moves).minByOrNull { it.value }!!.key

    private fun getWordScore(wordList: List<String>, word: String, depth: Int): Int {
        val filterScores = getWordFilterScores(wordList, word)
        return if (depth == 1) {
            filterScores.maxByOrNull { it.value }!!.value
        } else {
            val nextFilterScores = mutableMapOf<List<Results>, Int>()

            filterScores.forEach { (filter, _) ->
                val filteredWordList = applyFilter(wordList, word, filter)
                val filteredWordScores = getWordlistScores(filteredWordList, depth-1)

                nextFilterScores[filter] = filteredWordScores.minByOrNull { it.value }!!.value
            }

            nextFilterScores.maxByOrNull { it.value }!!.value
        }
    }

    private fun getWordFilterScores(wordList: List<String>, word: String): Map<List<Results>, Int> {
        val filterResults = mutableMapOf<List<Results>, Int>()

        wordList.forEach {
            val filter = getFilter(it, word)
            filterResults[filter] = filterResults.getDefault(filter) + 1
        }

        return filterResults
    }

    private fun getWordlistScores(currentList: List<String>, depth: Int): Map<String, Int> {
        if (currentList.size == 1) return mapOf(currentList[0] to 1)

        val wordScores = mutableMapOf<String, Int>()

        wordList.forEach {
            wordScores[it] = getWordScore(currentList, it, depth)
        }

        return wordScores
    }

    fun nextWord(filter: List<Results>): String {
        val nextList = applyFilter(currentList, word, filter)
        if (nextList.isEmpty()) throw NoSuchWordException()

        currentList = nextList

        word = getWord()
        if (increase_moves) {
            moves += 1
        }
        return word
    }

    fun nextWord() = word

    fun getCount() = currentList.size
}