package de.jagenka.util

import de.jagenka.config.Config
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

object I18n
{

    const val messagesFilePath = "/i18n/messages-%s.yaml"

    var locale: String = "en"
        private set
    var messages = mapOf<String, String>()
        private set
    var defaultLang = mapOf<String, String>()
        private set

    init
    {
        loadI18n()
    }

    fun loadI18n()
    {
        var locale = Config.general.locale

        I18n::class.java.getResourceAsStream(messagesFilePath.format(locale)).use { stream ->
            if (locale.isBlank() || stream == null)
            {
                locale = "en"
            }
        }

        I18n.locale = locale

        if (locale != "en") defaultLang = readLocale("en")
        messages = readLocale(locale)
    }

    fun readLocale(locale: String): Map<String, String>
    {
        val result = mutableMapOf<String, String>()

        // Read messages file
        I18n::class.java.getResourceAsStream(messagesFilePath.format(locale))!!.use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { inputReader ->
                BufferedReader(inputReader).use { bufferedReader ->
                    val lines = bufferedReader.lines()

                    lines.forEach {
                        if (":" !in it)
                        {
                            throw RuntimeException("Malformed messages.yaml")
                        }

                        val key = it.substring(0, it.indexOfFirst { it == ':' }).trim()
                        val value = it.substring(it.indexOfFirst { it == ':' } + 1, it.length).trim()
                        result[key] = value
                    }
                }
            }
        }

        return result.toMap()
    }

    fun get(key: String, args: Map<String, Any> = emptyMap()): String
    {
        var message = messages[key] ?: defaultLang[key] ?: "missing value for $key"

        args.entries.forEach { (key, value) ->
            message = message.replace("{${key}}", value.toString())

            repeat(100) {
                val matcher = Pattern.compile("\\{$key\\?([\\w']+)\\}").matcher(message)

                if (!matcher.find())
                {
                    return@repeat
                }

                message = message.replace(matcher.group(0), if (value.toString().toLong() == 1L) "" else matcher.group(1))
            }

            repeat(100) {
                val matcher = Pattern.compile("\\{$key\\?([\\w']+),([\\w']+)\\}").matcher(message)

                if (!matcher.find())
                {
                    return@repeat
                }

                message = message.replace(matcher.group(0), if (value.toString().toLong() == 1L) matcher.group(1) else matcher.group(2))
            }
        }

        return message
    }
}