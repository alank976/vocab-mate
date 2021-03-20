package github.io.vocabmate.interfaces

import github.io.vocabmate.domain.words.WordsService
import github.io.vocabmate.domain.words.Word
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Named
import javax.ws.rs.QueryParam

@Controller("/words")
class WordsController(@Named("words-api") private val wordsService: WordsService) {
    @Get
    fun words(@QueryParam("word") word: String): List<Word> {
        return wordsService.getWords(word)
    }
}