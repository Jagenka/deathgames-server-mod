package de.jagenka

import net.fabricmc.api.DedicatedServerModInitializer

object DeathGames : DedicatedServerModInitializer
{
    override fun onInitializeServer()
    {
        println("DeathGames Mod initialized!")
    }
}