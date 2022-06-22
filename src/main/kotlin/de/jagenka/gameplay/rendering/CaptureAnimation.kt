package de.jagenka.gameplay.rendering

import de.jagenka.Util.ifServerLoaded
import de.jagenka.config.Config
import de.jagenka.managers.DGSpawn
import de.jagenka.managers.PlayerManager
import de.jagenka.team.DGTeam
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d
import kotlin.math.absoluteValue
import kotlin.math.pow

object CaptureAnimation {

    const val RADIUS = 4.8
    const val VISIBILITY_RANGE = 60.0

    fun render(captureProgress: MutableMap<Pair<DGSpawn, DGTeam>, Int>) {
        if(! Config.captureEnabled) {
            return
        }

        ifServerLoaded { server ->

            captureProgress.forEach { pair, progress ->
                val (spawn, team) = pair

                val angle = (System.currentTimeMillis() % 6000).toDouble() / 6000.0 * Math.PI * 2.0
                val captureDistance = (1.0 - (Config.captureTimeNeeded - progress).toFloat() / Config.captureTimeNeeded.toFloat()) * RADIUS

                val angles = if((Config.captureTimeNeeded - progress).absoluteValue > 0.5)
                    listOf(angle, angle + Math.PI * 2.0 / 3.0, angle + Math.PI * 2.0 / 3.0 * 2.0)
                else
                    (0 until 360).map { it.toDouble() / 360.0 * Math.PI * 2.0 }

                val particles = angles.map {
                    val offsetX = Math.cos(it) * RADIUS
                    val offsetY = Math.sin(it) * RADIUS
                    val pos = spawn.coordinates.toVec3d().add(Vec3d(offsetX, 2.0, offsetY))

                    return@map listOf(
                        spawn.coordinates.toVec3d().add(Vec3d(offsetX, 0.25 + Math.random() / 2.0, offsetY)),
                        spawn.coordinates.toVec3d().add(Vec3d(offsetX, 0.75 + Math.random() / 2.0, offsetY)),
                    )
                }.flatten().toMutableList()

                repeat((captureDistance.pow(2.0) * 2.5).toInt()) {
                    val randomAngle = Math.random() * Math.PI * 2.0
                    val offsetX = Math.cos(randomAngle) * Math.random() * captureDistance
                    val offsetY = Math.sin(randomAngle) * Math.random() * captureDistance
                    particles.add(spawn.coordinates.toVec3d().add(Vec3d(offsetX, 0.1, offsetY)))
                }

                PlayerManager.getOnlinePlayers().forEach { player ->
                    // we're using force on the particle, this makes the default range 512 blocks (instead of 32), so let's trim that
                    if(player.pos.subtract(spawn.coordinates.toVec3d()).lengthSquared() > VISIBILITY_RANGE.pow(2.0)) {
                        return@forEach
                    }

                    ParticleRenderer.drawMultipleParticlesWorld(server, player, ParticleTypes.WAX_OFF, particles)
                }

            }

        }

    }


}