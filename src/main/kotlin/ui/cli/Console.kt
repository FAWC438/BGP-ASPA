package ui.cli

/**
 * 用于控制台输出信息
 */
class Console {

    fun info(message: String, inline: Boolean = false) {
        print("INFO", message, inline)
    }

    fun warning(message: String, inline: Boolean = false) {
        print("WARNING", message, inline)
    }

    fun error(message: String, inline: Boolean = false) {
        print("ERROR", message, inline)
    }

    fun print(message: String = "") {
        println(message)
    }

    private fun print(level: String, message: String, inline: Boolean) {

        val formattedMessage = String.format("%s: %s", level, message)
        if (inline) kotlin.io.print(formattedMessage) else println(formattedMessage)
    }
}