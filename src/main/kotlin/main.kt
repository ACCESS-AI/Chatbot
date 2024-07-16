import chatbot.model.Chatbot
import chatbot.model.Message
import java.security.MessageDigest

suspend fun main() {
        val courseSlug: String = "mockkkkkkk"
        val courseSlugHash: String = //"cbdcfbbfbbbfbfefbcbf"
                MessageDigest.getInstance("SHA-256")
                .digest(courseSlug.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .filter(Char::isLetter)
                .take(20)
        var chatbot = Chatbot( "supervisor@uzh.ch", courseSlug, courseSlugHash, "classes", "carpark-multiple-inheritance")

        val output = chatbot.run("bla bla task instructions", listOf(),
                "how do i create a subclass of a car? I think i need to use inheritance")
        println(output.toString())

//        val history: List<Message> = chatbot.getHistory()
//
//        history.forEach { h: Message -> println(h) }
}