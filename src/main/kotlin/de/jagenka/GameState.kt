package de.jagenka

import de.jagenka.config.Config

enum class GameState
{
    /**
     * mod is disabled and should do basically nothing
     */
    DISABLED,

    /**
     * players are selecting teams
     */
    PREPARING,

    /**
     * after players selected to start game, and before players can damage each other after being tp-ed to their spawns
     */
    STARTING,

    /**
     * normal game operations, where more than one team is participating
     */
    RUNNING,

    /**
     * less than two teams are participating and game transitions to PREPARING
     */
    ENDING;

    companion object
    {
        /**
         * the (exactly) one state the mod is currently in
         */
        var current: GameState = DISABLED

        val modEnabled: Boolean
            get() = current != DISABLED && Config.general.enabled

        val modDisabled: Boolean
            get() = !modEnabled
    }
}