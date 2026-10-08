package de.jagenka.util

import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

fun sendSuccess(s: CommandSourceStack, text: String)
{
    sendSuccess(s, Component.literal(text))
}

fun sendSuccess(s: CommandSourceStack, component: Component)
{
    s.sendSuccess({ component }, false)
}

fun sendFailure(s: CommandSourceStack, text: String)
{
    sendFailure(s, Component.literal(text))
}

fun sendFailure(s: CommandSourceStack, component: Component)
{
    s.sendFailure(component)
}