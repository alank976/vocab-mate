package github.io.vocabmate.interfaces

import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.domain.vocabs.VocabService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.reactivex.rxjava3.core.Flowable

@Controller("/vocabs")
class VocabController(private val vocabService: VocabService) {
    @Get("/{vocab}")
    fun getVocabs(@PathVariable("vocab") vocab: String): Flowable<Vocab> {
        return vocabService.getVocab(vocab)
    }
}