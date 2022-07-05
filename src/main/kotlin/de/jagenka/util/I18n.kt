package de.jagenka.util

import de.jagenka.config.Config
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

object I18n {

    const val messagesFilePath = "/i18n/messages-%s.yaml"

    val locale: String
    val messages = HashMap<String, String>()

    init {
        var locale = Config.configEntry.general.locale

        I18n::class.java.getResourceAsStream(messagesFilePath.format(locale)).use { stream ->
            if(locale.isBlank() || stream == null) {
                locale = "en"
            }
        }

        I18n.locale = locale

        // Read messages file
        I18n::class.java.getResourceAsStream(messagesFilePath.format(locale))!!.use { stream ->
            InputStreamReader(stream).use { inputReader ->
                BufferedReader(inputReader).use { bufferedReader ->
                    val lines = bufferedReader.lines()

                    lines.forEach {
                        if(":" !in it) {
                            throw RuntimeException("Malformed messages.yaml")
                        }

                        val key = it.substring(0, it.indexOfFirst { it == ':' }).trim()
                        val value = it.substring(it.indexOfFirst { it == ':' } + 1, it.length).trim()
                        messages[key] = value
                    }
                }
            }
        }
    }

    fun get(key: String, args: Map<String, Any> = emptyMap()): String {
        var message = messages[key] ?: return ""

        args.entries.forEach { (key, value) ->
            message = message.replace("{${key}}", value.toString())

            repeat(100) {
                val matcher = Pattern.compile("\\{$key\\?([\\w']+)\\}").matcher(message)

                if(! matcher.find()) {
                    return@repeat
                }

                message = message.replace(matcher.group(0), if(value.toString().toLong() == 1L) "" else matcher.group(1))
            }

            repeat(100) {
                val matcher = Pattern.compile("\\{$key\\?([\\w']+),([\\w']+)\\}").matcher(message)

                if(! matcher.find()) {
                    return@repeat
                }

                message = message.replace(matcher.group(0), if(value.toString().toLong() == 1L) matcher.group(1) else matcher.group(2))
            }
        }

        return message
    }
}