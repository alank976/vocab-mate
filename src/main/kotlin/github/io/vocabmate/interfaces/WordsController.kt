package github.io.vocabmate.interfaces

import github.io.vocabmate.domain.words.WordsService
import github.io.vocabmate.domain.words.Word
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Flowable
import javax.inject.Named
import javax.ws.rs.QueryParam

@Controller("/words")
class WordsController(@Named("words-api") private val wordsService: WordsService) {
    @Get
    fun words(@QueryParam("word") word: String): Flowable<Word> {
        return wordsService.getWords(word)
    }
}