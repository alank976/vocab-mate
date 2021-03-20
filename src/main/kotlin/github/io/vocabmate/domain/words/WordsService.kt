package github.io.vocabmate.domain.words

interface WordsService {
    fun getWords(value: String): List<Word>
}