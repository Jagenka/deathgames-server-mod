package de.jagenka.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.jagenka.*
import de.jagenka.commands.DeathGamesCommand.isOp
import de.jagenka.config.Config
import de.jagenka.config.ConfigEntry
import de.jagenka.managers.Platform
import de.jagenka.timer.DGUnit
import de.jagenka.util.getPropertiesFromSection
import de.jagenka.util.getSectionsFromConfig
import de.jagenka.util.getStringifiedValueFromProperty
import de.jagenka.util.setPropertyFromString
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.regex.Matcher
import java.util.regex.Pattern

object DeathGamesConfigCommand {

    val pickedCoordinates: MutableList<Coordinates> = ArrayList()

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

                    val result = setPropertyFromString(newValue, Config.configEntry, sectionField, property, configPropertyTransformers, it.source)
                    val currentValueString = getStringifiedValueFromProperty(Config.configEntry, sectionField, property, configPropertyTransformers)
                    if(result) {
                        it.source.sendFeedback(Text.of("config.${section.name}.$propertyName set to $currentValueString"), true)
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

        // Argument / coordinate picker

        val pickLiteral = literal("pick")
        val clearLiteral = literal("clear")

        pickLiteral.executes {

            val playerEntity = it.source.entity as? ServerPlayerEntity

            if (playerEntity != null) {
                val coord = playerEntity.toDGCoordinates()
                pickedCoordinates.add(coord)
                val coordListString = "Args: [" + pickedCoordinates.joinToString(", ") { it.toString() } + "]"
                it.source.sendFeedback(Text.of(coordListString), false)
            } else {
                it.source.sendFeedback(Text.of("No player entity found to obtain coordinates from."), false)
            }

            return@executes 0
        }

        clearLiteral.executes {
            pickedCoordinates.clear()
            it.source.sendFeedback(Text.of("Coordinate list cleared"), false)
            return@executes 0
        }

        val argLiteral = literal("args").then(pickLiteral).then(clearLiteral)
        argLiteral.requires { it.isOp() }

        return lab.then(configLiteral).then(argLiteral)
    }

}

val NUMBER_TRANSFORM_MATCHERS = mapOf(
    Pattern.compile("(-?\\d+)") to 1,
    Pattern.compile("(\\d+)\\s*(t|tick|ticks)") to DGUnit.TICKS.factor,
    Pattern.compile("(\\d+)\\s*(s|sec|secs|seconds)") to DGUnit.SECONDS.factor,
    Pattern.compile("(\\d+)\\s*(m|min|mins|minutes)") to DGUnit.MINUTES.factor,
    Pattern.compile("(\\d+)\\s*(h|hour|hours)") to DGUnit.HOURS.factor
)

@Throws(java.lang.NumberFormatException::class)
fun <T: Number> transformNumber(str: String, numeralParser: (String) -> T): T? {

    val match: Pair<Matcher, Int> = NUMBER_TRANSFORM_MATCHERS.map {
        it.key.matcher(str) to it.value
    }.find { it.first.matches() } ?: return null

    val valueString = match.first.group(1)
    val numeralValue: T = numeralParser.invoke(valueString)

    val scaledNumeral = numeralParser.invoke((numeralValue.toLong() * match.second).toString())

    return scaledNumeral
}


interface ConfigPropertyTransformer<T> {
    fun toString(value: Any): String
    fun fromString(str: String, source: ServerCommandSource): T?
}

val configPropertyTransformers = mapOf<Class<out Any>, ConfigPropertyTransformer<out Any>>(
    Coordinates::class.java to object : ConfigPropertyTransformer<Coordinates> {
        override fun toString(value: Any): String = "Coord" + (value as? Coordinates)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Coordinates? {

            val parsedCoordinate = Util.getCoordinateFromString(str)

            if(parsedCoordinate != null) {
                return parsedCoordinate
            } else if(str == "first") {
                return DeathGamesConfigCommand.pickedCoordinates.firstOrNull()
            } else if(str == "last") {
                return DeathGamesConfigCommand.pickedCoordinates.lastOrNull()
            } else if(str == "pick") {
                return (source.entity as? ServerPlayerEntity)?.toDGCoordinates()
            } else {
                // This is terribly engineered, but better to have some feedback than none
                source.sendFeedback(Text.of("Value has to be one of [first, last, pick], to obtain the first or last argument from the args list or to pick the current position."), false)
                return null
            }
        }
    },
    Platform::class.java to object : ConfigPropertyTransformer<Platform> {
        override fun toString(value: Any): String = "Platform" + (value as? Platform)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Platform? {
            if(str.isBlank()) {
                source.sendFeedback(Text.of("You have to provide a name"), false)
                return null
            }

            return (source.entity as? ServerPlayerEntity)?.toDGCoordinates()?.let { Platform(str, it, false) }
        }
    },
    CoordinateList::class.java to object : ConfigPropertyTransformer<CoordinateList> {
        override fun toString(value: Any): String = (value as? CoordinateList)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): CoordinateList? {
            Util.getCoordinateListFromString(str)?.let { return@fromString CoordinateList(it) }
            return CoordinateList(ArrayList(DeathGamesConfigCommand.pickedCoordinates))
        }
    },
    PlatformList::class.java to object : ConfigPropertyTransformer<PlatformList> {
        override fun toString(value: Any): String = (value as? PlatformList)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): PlatformList? {
            val names = str.split(",").map { it.trim() }

            if(names.size != DeathGamesConfigCommand.pickedCoordinates.size) {
                source.sendFeedback(Text.of("You need to provide the same number of names and coordinates. Names: ${names.size}, Coordinates: ${DeathGamesConfigCommand.pickedCoordinates.size}"), false)
            }

            val platforms = (0 until names.size).map { Platform(names[it], DeathGamesConfigCommand.pickedCoordinates[it], false) }

            return PlatformList(platforms)
        }
    },
    BlockCuboid::class.java to object : ConfigPropertyTransformer<BlockCuboid> {
        override fun toString(value: Any): String = (value as? BlockCuboid)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): BlockCuboid? {
            Util.getCoordinateListFromString(str)?.let {
                if(it.size != 2) {
                    source.sendFeedback(Text.of("You need to specify exactly two coordinates for a BlockCuboid."), false)
                    return@fromString null
                }
                return@fromString BlockCuboid(it[0], it[1])
            }

            if(DeathGamesConfigCommand.pickedCoordinates.size != 2) {
                // This is terribly engineered, but better to have some feedback than none
                source.sendFeedback(Text.of("You need to pick exactly two coordinates for a BlockCuboid."), false)
                return null
            } else {
                return BlockCuboid(DeathGamesConfigCommand.pickedCoordinates[0], DeathGamesConfigCommand.pickedCoordinates[1])
            }
        }
    },
    String::class.java to object : ConfigPropertyTransformer<String> {
        override fun toString(value: Any): String = (value as? String)!!
        override fun fromString(str: String, source: ServerCommandSource): String? = str
    },
    Boolean::class.java to object : ConfigPropertyTransformer<Boolean> {
        override fun toString(value: Any): String = (value as? Boolean)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Boolean? {
            try {
                return str.toBooleanStrict()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Int::class.java to object : ConfigPropertyTransformer<Int> {
        override fun toString(value: Any): String = (value as? Int)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Int? {
            try {
                return transformNumber(str) { it.toInt() }
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Long::class.java to object : ConfigPropertyTransformer<Long> {
        override fun toString(value: Any): String = (value as? Long)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Long? {
            try {
                return transformNumber(str) { it.toLong() }
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Short::class.java to object : ConfigPropertyTransformer<Short> {
        override fun toString(value: Any): String = (value as? Short)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Short? {
            try {
                return transformNumber(str) { it.toShort() }
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Byte::class.java to object : ConfigPropertyTransformer<Byte> {
        override fun toString(value: Any): String = (value as? Byte)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Byte? {
            try {
                return transformNumber(str) { it.toByte() }
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Float::class.java to object : ConfigPropertyTransformer<Float> {
        override fun toString(value: Any): String = (value as? Float)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Float? {
            try {
                return str.toFloat()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    },
    Double::class.java to object : ConfigPropertyTransformer<Double> {
        override fun toString(value: Any): String = (value as? Double)!!.toString()
        override fun fromString(str: String, source: ServerCommandSource): Double? {
            try {
                return str.toDouble()
            } catch (e: NumberFormatException) {
                return null
            }
        }
    }
)