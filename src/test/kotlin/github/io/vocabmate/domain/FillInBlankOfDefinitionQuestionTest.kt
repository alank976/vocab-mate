package github.io.vocabmate.domain

import github.io.vocabmate.domain.questions.Question
import github.io.vocabmate.domain.words.Vocab
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class FillInBlankOfDefinitionQuestionTest : StringSpec({
    val words = Vocab(
        word = "genius",
        partOfSpeech = Vocab.PartOfSpeech.Noun,
        definition = "very great and rare natural ability or skill, especially in a particular area such as science or art, or a person who has this"
    )
    val question = Question.FillInBlankOfDefinitionQuestion(words, 1)

    "question should be like" {
        question.question() shouldContain "_"
        println("question=${question.question()}")
        question.question().length shouldBeExactly words.definition.length
    }

    "At least one choice should be in original definition" {
        val q = question.question()
        val choices = question.choices()
        choices shouldHaveSize 4

        choices.any { choice ->
            val recomposeDefinition = q.replace(Regex("[_]+"), choice)
            recomposeDefinition == words.definition
        } shouldBe true
    }

    "answer fits with original definition" {
        val blankRange = question.question().indexOf('_')..question.question().lastIndexOf('_')
        val foundAnswerFromDefinition = words.definition.substring(blankRange)
        question.answer(foundAnswerFromDefinition) shouldBe true
    }

})
