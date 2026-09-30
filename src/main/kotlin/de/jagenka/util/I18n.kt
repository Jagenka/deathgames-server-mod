package de.jagenka.util

import com.charleskorn.kaml.*
import de.jagenka.config.Config
import org.slf4j.helpers.Reporter.error
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
        I18n::class.java.getResourceAsStream(messagesFilePath.format(locale))!!.use { stream ->
            try
            {
                return Yaml.default.parseToYamlNode(stream).flatten()

            } catch (ex: Exception)
            {
                error("error reading locale $locale", ex)
            }
        }

        return emptyMap() // this should be unreachable
    }

    fun YamlNode.flatten(prefix: String = "", out: MutableMap<String, String> = linkedMapOf()): Map<String, String>
    {
        fun join(key: String) = if (prefix.isEmpty()) key else "$prefix.$key"
        when (this)
        {
            is YamlScalar -> out[prefix] = content
            is YamlMap -> entries.forEach { (k, v) -> v.flatten(join(k.content), out) }
            is YamlList -> items.forEachIndexed { i, v -> v.flatten(join(i.toString()), out) }
            is YamlTaggedNode -> innerNode.flatten(prefix, out)
            is YamlNull -> Unit
        }
        return out
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