package github.io.vocabmate.domain.words

import io.reactivex.Flowable

interface WordsService {
    fun getWords(value: String): Flowable<Word>
}