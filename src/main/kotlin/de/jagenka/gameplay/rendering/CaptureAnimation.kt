package de.jagenka.gameplay.rendering

import de.jagenka.Util.ifServerLoaded
import de.jagenka.config.Config
import de.jagenka.managers.DGSpawn
import de.jagenka.managers.PlayerManager
import de.jagenka.team.DGTeam
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d

object CaptureAnimation {

    const val RADIUS = 7.0

    fun render(captureProgress: MutableMap<Pair<DGSpawn, DGTeam>, Int>) {
        if(! Config.captureEnabled) {
            return
        }

        ifServerLoaded { server ->

            captureProgress.forEach { pair, progress ->
                val (spawn, team) = pair

                val angle = (System.currentTimeMillis() % 4000).toDouble() / 4000.0 * Math.PI * 2.0
                val distance = (Config.captureTimeNeeded - progress).toFloat() / Config.captureTimeNeeded.toFloat() * RADIUS

                val angles = listOf(angle, angle + Math.PI * 2.0 / 3.0, angle + Math.PI * 2.0 / 3.0 * 2.0)

                val particles = angles.map {
                    val offsetX = Math.cos(it) * distance
                    val offsetY = Math.sin(it) * distance
                    val pos = spawn.coordinates.toVec3d().add(Vec3d(offsetX, 2.0, offsetY))

                    return@map listOf(
                        spawn.coordinates.toVec3d().add(Vec3d(offsetX, 0.5 + Math.random() / 6.0, offsetY)),
                        spawn.coordinates.toVec3d().add(Vec3d(offsetX, 1.0 + Math.random() / 6.0, offsetY)),
                    )
                }.flatten()

                PlayerManager.getOnlinePlayers().forEach { player ->
                    //TODO: check distance
                    ParticleRenderer.drawMultipleParticlesWorld(server, player, ParticleTypes.WAX_OFF, particles)
                }

            }

        }

    }


}