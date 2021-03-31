package github.io.vocabmate.domain.questions

import github.io.vocabmate.domain.words.Vocab

sealed class Question(val vocab: Vocab, val creditPoint: Int) {
    abstract fun question(): String
    abstract fun choices(): List<String>
    abstract fun answer(answer: String): Boolean
    val trivialWords = setOf(
        "is", "am", "are",
        "a", "the",
        "i", "you", "he", "she", "it", "they", "we",
        "me", "him", "her", "them", "us",
        "what", "which", "why", "when", "where", "how", "whom", "who",
        "to", "in", "of", "on", "into", "for",
        "just", "and", "or", "also", "very",
        "this", "that", "these", "those",
        "as",
        "have", "has", "will", "shall", "would", "could", "not"
    )

    class FillInBlankOfDefinitionQuestion(vocab: Vocab, creditPoint: Int) : Question(vocab, creditPoint) {
        private val replacedWordIndex: Int
        private val definitionWords = vocab.definition.split(" ")
        private val answer: String

        init {
            val replaceableWords = definitionWords
                .mapIndexed { i, s -> i to s }
                .filter { (_, word) -> !word.ignoreLastNonLetterCharIfAny().isTrivialWord() }
                .associate { it }
            if (replaceableWords.isEmpty()) throw DefinitionTooSimpleException(vocab.definition)
            replacedWordIndex = replaceableWords.keys.random()
            answer = definitionWords[replacedWordIndex].ignoreLastNonLetterCharIfAny()
        }

        override fun question(): String {
            val convertedDefinitionWords = definitionWords.toMutableList()
            convertedDefinitionWords[replacedWordIndex] = maskToBlankLine(definitionWords[replacedWordIndex])
            return convertedDefinitionWords.joinToString(separator = " ")
        }


        override fun choices(): List<String> {
            val choices = mutableSetOf(answer)
            Array(10) {
                trivialWords.random()
            }
                .asSequence()
                .takeWhile { choices.size < 4 }
                .forEach { choices.add(it) }
            return choices.shuffled()
        }

        override fun answer(answer: String) = answer == this.answer

        class DefinitionTooSimpleException(definition: String) :
            Exception("Definition: $definition is too simple to be used as question")

        private fun String.ignoreLastNonLetterCharIfAny() =
            last()
                .takeUnless { it.isLetter() }
                ?.let { substring(0, length - 1) }
                ?: this

        private fun maskToBlankLine(input: String): String = input.last()
            .takeIf { !it.isLetter() }
            ?.let { "_".repeat(input.length - 1) + it }
            ?: "_".repeat(input.length)

        // TODO: Configurable instead of hardcode
        private fun String.isTrivialWord(): Boolean {
            return toLowerCase() in trivialWords
        }
    }

}