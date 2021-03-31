package github.io.vocabmate.domain.vocabs

import github.io.vocabmate.domain.words.Vocab
import io.reactivex.rxjava3.core.Flowable

interface VocabRepository {
    fun findAll(): Flowable<Vocab>
    fun create(vocab: Vocab): Vocab
}