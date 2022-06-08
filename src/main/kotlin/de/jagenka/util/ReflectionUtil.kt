package de.jagenka.util

import de.jagenka.commands.ConfigPropertyTransformer
import de.jagenka.config.Config
import de.jagenka.config.ConfigEntry
import de.jagenka.config.Section
import java.lang.reflect.Field

fun <T> getDeclaredFields(clazz: Class<T>): List<Field> {
    val fields = mutableListOf<Field>()
    fields.addAll(clazz.declaredFields)

//    if(recursive) {
//        val superClass = clazz.superclass
//        println(superClass)
//        if(superClass != null && superClass.canonicalName != "java.lang.Object") {
//            fields.addAll(getDeclaredFields(superClass, recursive = true))
//        }
//    }

    return fields
}

fun <T> getSectionsFromConfig(configClass: Class<T>): Map<Section, Field> {
    return getDeclaredFields(configClass)
        .filter { f -> f.annotations.any { it is Section } }
        .associateBy { it.annotations.filterIsInstance<Section>().first() }
}

fun getPropertiesFromSection(sectionClass: Class<*>, transformers: Map<Class<out Any>, ConfigPropertyTransformer<out Any>>): List<Field> {
    return getDeclaredFields(sectionClass)
        .filter { prop -> getConfigPropertyTransformer(prop.type, transformers) != null }
}

fun getConfigPropertyTransformer(type: Class<*>, transformers: Map<Class<out Any>, ConfigPropertyTransformer<out Any>>): ConfigPropertyTransformer<out Any>? {
    return transformers.entries.find { type.canonicalName == it.key.canonicalName }?.value
}

fun setPropertyFromString(newValue: String, configEntry: ConfigEntry, sectionField: Field, propertyField: Field,
                          transformers: Map<Class<out Any>, ConfigPropertyTransformer<out Any>>
): Boolean {
    try {
        sectionField.isAccessible = true
        propertyField.isAccessible = true

        val currentSection: Any = sectionField.get(Config.configEntry)
//        val currentValue = propertyField.get(currentSection)

        val transformer = getConfigPropertyTransformer(propertyField.type, transformers) ?: return false
        val newValueObject = transformer.fromString(newValue)

        // make property field non final
//        val modifiersField = Field::class.java.getDeclaredField("modifiers")   // hidden from java 12 on
//        val getDeclaredFields0: Method = Class::class.java.getDeclaredMethod("getDeclaredFields0", Boolean::class.java)
//        getDeclaredFields0.isAccessible = true
//        val fieldFields = (getDeclaredFields0.invoke(Field::class.java, false) as Array<Field>).toList()
//
//        val modifiersField = fieldFields.find { it.name == "modifiers" } ?: return false
//
//        modifiersField.isAccessible = true
//        modifiersField.setInt(propertyField, propertyField.modifiers.and(Modifier.FINAL.inv()))

        propertyField.set(currentSection, newValueObject)

        return true

    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

fun getStringifiedValueFromProperty(configEntry: ConfigEntry, sectionField: Field, propertyField: Field,
    transformers: Map<Class<out Any>, ConfigPropertyTransformer<out Any>>
): String {
    try {
        sectionField.isAccessible = true
        propertyField.isAccessible = true

        val currentSection: Any = sectionField.get(Config.configEntry)
        val currentValue = propertyField.get(currentSection)

        val transformer = getConfigPropertyTransformer(propertyField.type, transformers) ?: return "!! No valid transformer"

        val currentValueString = transformer.toString(currentValue)

        return currentValueString

    } catch (e: Exception) {
        e.printStackTrace()
        return "!! Internal error occured during config access"
    }
}