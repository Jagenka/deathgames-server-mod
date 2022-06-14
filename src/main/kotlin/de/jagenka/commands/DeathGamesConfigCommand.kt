package de.jagenka.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.jagenka.commands.DeathGamesCommand.isOp
import de.jagenka.config.Config
import de.jagenka.config.ConfigEntry
import de.jagenka.util.getPropertiesFromSection
import de.jagenka.util.getSectionsFromConfig
import de.jagenka.util.getStringifiedValueFromProperty
import de.jagenka.util.setPropertyFromString
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object DeathGamesConfigCommand {

    fun generateConfigCommand(lab: LiteralArgumentBuilder<ServerCommandSource>): LiteralArgumentBuilder<ServerCommandSource>? {
        val transformableTypes = configPropertyTransformers.keys.toList()

        val sections = getSectionsFromConfig(ConfigEntry::class.java)

        val configLiteral = literal("config")
        configLiteral.requires { it.isOp() }

        for((section, sectionField) in sections) {
            val properties = getPropertiesFromSection(sectionField.type, configPropertyTransformers)

            val sectionLiteral = literal(section.name)

            // print section config
            sectionLiteral.executes {
                for(property in properties) {
                    val currentValueString = getStringifiedValueFromProperty(Config.configEntry, sectionField, property, configPropertyTransformers)
                    it.source.sendFeedback(Text.of("config.${section.name}.${property.name}: $currentValueString"), false)
                }

                return@executes 0
            }

            // add sub command for each property in this section
            for(property in properties) {
                val propertyName = property.name

                val propertyLiteral = literal(propertyName)

                // print current property value
                propertyLiteral.executes {
                    val currentValueString = getStringifiedValueFromProperty(Config.configEntry, sectionField, property, configPropertyTransformers)

                    it.source.sendFeedback(Text.of("config.${section.name}.$propertyName: $currentValueString"), false)
                    return@executes 0
                }

                // set property value
                val argumentLiteral = argument("newValue", StringArgumentType.greedyString())
                argumentLiteral.executes {
                    val newValue = it.getArgument("newValue", String::class.java)

                    val result = setPropertyFromString(newValue, Config.configEntry, sectionField, property, configPropertyTransformers)
                    if(result) {
                        it.source.sendFeedback(Text.of("config.${section.name}.$propertyName set to $newValue"), true)
                        Config.store()
                    } else {
                        it.source.sendFeedback(Text.of("Could not set value on config.${section.name}.$propertyName"), false)
                    }

                    return@executes 0
                }


                propertyLiteral.then(argumentLiteral)

                sectionLiteral.then(propertyLiteral)
            }

            configLiteral.then(sectionLiteral)
        }

        return lab.then(configLiteral)
    }

}

interface ConfigPropertyTransformer<T> {
    fun toString(value: Any): String
    fun fromString(str: String): T?
}

val configPropertyTransformers = mapOf<Class<out Any>, ConfigPropertyTransformer<out Any>>(
    String::class.java to object : ConfigPropertyTransformer<String> {
        override fun toString(value: Any): String = (value as? String)!!
        override fun fromString(str: String): String? = str
    },
    Boolean::class.java to object : ConfigPropertyTransformer<Boolean> {
        override fun toString(value: Any): String = (value as? Boolean)!!.toString()
        override fun fromString(str: String): Boolean? {
            try {
                return str.toBooleanStrict()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Int::class.java to object : ConfigPropertyTransformer<Int> {
        override fun toString(value: Any): String = (value as? Int)!!.toString()
        override fun fromString(str: String): Int? {
            try {
                return str.toInt()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Long::class.java to object : ConfigPropertyTransformer<Long> {
        override fun toString(value: Any): String = (value as? Long)!!.toString()
        override fun fromString(str: String): Long? {
            try {
                return str.toLong()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Short::class.java to object : ConfigPropertyTransformer<Short> {
        override fun toString(value: Any): String = (value as? Short)!!.toString()
        override fun fromString(str: String): Short? {
            try {
                return str.toShort()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Byte::class.java to object : ConfigPropertyTransformer<Byte> {
        override fun toString(value: Any): String = (value as? Byte)!!.toString()
        override fun fromString(str: String): Byte? {
            try {
                return str.toByte()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Float::class.java to object : ConfigPropertyTransformer<Float> {
        override fun toString(value: Any): String = (value as? Float)!!.toString()
        override fun fromString(str: String): Float? {
            try {
                return str.toFloat()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Double::class.java to object : ConfigPropertyTransformer<Double> {
        override fun toString(value: Any): String = (value as? Double)!!.toString()
        override fun fromString(str: String): Double? {
            try {
                return str.toDouble()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    }
)