package de.jagenka.util

import de.jagenka.DeathGames
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class DebugOverride<T>(
    private val source: () -> T,
    private val debugValue: T,
) : ReadOnlyProperty<Any?, T>
{
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        if (DeathGames.isDebug) debugValue else source()
}