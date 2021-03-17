package github.io.vocabmate.interfaces

import github.io.vocabmate.infrastructure.UrbanDictClient
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.ws.rs.QueryParam

@Controller("/foo")
class MyController(private val urbanDictClient: UrbanDictClient) {
    @Get
    fun foo(@QueryParam("expr") expression: String): MutableIterable<Any>? {
        return urbanDictClient.checkDict(expression)
    }
}