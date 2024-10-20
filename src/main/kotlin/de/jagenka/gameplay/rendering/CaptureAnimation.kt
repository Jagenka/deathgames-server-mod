package de.jagenka.gameplay.rendering

import de.jagenka.Util.ifServerLoaded
import de.jagenka.config.Config
import de.jagenka.managers.DGSpawn
import de.jagenka.managers.PlayerManager
import de.jagenka.managers.PlayerManager.getDGTeam
import de.jagenka.managers.SpawnManager
import de.jagenka.rotateAroundVector
import net.minecraft.particle.DustParticleEffect
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.*
import kotlin.random.Random

object CaptureAnimation
{

    val RADIUS
        get() = Config.spawns.platformRadius + 0.8
    const val VISIBILITY_RANGE = 60.0
    private val orbModel: ParticleRenderer.VertexStructure

    init
    {
        CaptureAnimation::class.java.getResourceAsStream("/models/OrbV2.ply").use { stream ->
            orbModel = PlyImporter.parsePlyFromStream(stream!!)
        }
    }

    fun renderSpiral(captureProgress: Map<DGSpawn, Int>)
    {
        if (!Config.spawns.enableCapture)
        {
            return
        }

        ifServerLoaded { server ->
            captureProgress.forEach { (spawn, progress) ->
                // Get teams on spawn for particle colors
                val playersOnSpawn = PlayerManager.getOnlineParticipatingPlayers().filter { spawn.containsPlayer(it) }
                val teamsOnSpawn = playersOnSpawn.map { it.getDGTeam() }.toSet()

                val globalRotation = Gradient.globalGradient(18000) * Math.PI * 2.0

                val angle = Gradient.globalGradient(6000) * Math.PI * 2.0 + globalRotation
                val height = Gradient.globalGradient(2000) * 6.0
                val captureDistance = (1.0 - (Config.spawns.captureTimeNeeded - progress).toFloat() / Config.spawns.captureTimeNeeded.toFloat()) * RADIUS

                val angles = if ((Config.spawns.captureTimeNeeded - progress).absoluteValue > 0.5)
                    listOf(
                        angle,
                        angle + Math.PI * 2.0 * 1.0 / 6.0,
                        angle + Math.PI * 2.0 * 2.0 / 6.0,
                        angle + Math.PI * 2.0 * 1.0 / 6.0,
                        angle + Math.PI * 2.0 * 3.0 / 6.0,
                        angle + Math.PI * 2.0 * 4.0 / 6.0,
                        angle + Math.PI * 2.0 * 5.0 / 6.0
                    )
                else
                    (0 until 360).map { it.toDouble() / 360.0 * Math.PI * 2.0 }

                val particles = angles.map {
                    val offsetX = cos(it) * RADIUS
                    val offsetZ = sin(it) * RADIUS

                    return@map listOf(
                        spawn.coordinates.toVec3d().add(Vec3d(offsetX, height, offsetZ)),
//                        spawn.coordinates.toVec3d().add(Vec3d(offsetX, 0.75 + Math.random() / 2.0, offsetZ)),
                    )
                }.flatten().toMutableList()

                repeat((captureDistance.pow(2.0) * 1.0).toInt()) {
                    val randomAngle = Math.random() * Math.PI * 2.0
                    val offsetX = cos(randomAngle) * Math.random() * captureDistance
                    val offsetZ = sin(randomAngle) * Math.random() * captureDistance
                    particles.add(spawn.coordinates.toVec3d().add(Vec3d(offsetX, 0.1, offsetZ)))
                }

                PlayerManager.getOnlinePlayers().forEach inner@{ player ->
                    // we're using force on the particle, this makes the default range 512 blocks (instead of 32), so let's trim that
                    if (player.pos.subtract(spawn.coordinates.toVec3d()).lengthSquared() > VISIBILITY_RANGE.pow(2.0))
                    {
                        return@inner
                    }

                    val rgbParticle = DustParticleEffect(teamsOnSpawn.find { it != null && it != SpawnManager.getTeam(spawn) }?.getColorVector() ?: Vector3f(0f, 0f, 0f), 4f)
                    ParticleRenderer.drawMultipleParticlesWorld(server, player, rgbParticle, particles)
                }

            }

        }

    }

    fun renderOrb(captureProgress: Map<DGSpawn, Int>)
    {
        if (!Config.spawns.enableCapture)
        {
            return
        }

        ifServerLoaded { server ->
            captureProgress.forEach { (spawn, progress) ->
                // Get teams on spawn for particle colors
                val playersOnSpawn = PlayerManager.getOnlineParticipatingPlayers().filter { spawn.containsPlayer(it) }
                val teamsOnSpawn = playersOnSpawn.map { it.getDGTeam() }.toSet()

                val orb = spawn.coordinates.toVec3d().add(Vec3d(0.0, 6.0, 0.0))
                val orbM = orbModel.clone()
                // This is just volume for a sphere solved for radius, using max radius 5.0 (max Volume is 268.1)
                val orbSize = ((progress.toDouble() / Config.spawns.captureTimeNeeded.toDouble()) * 268.1 * (3.0 / 4.0) / PI).pow(1.0 / 3.0)
                orbM.translate(orb)
                orbM.scale(orbSize / 1.5, orb)
                orbM.rotate(Vector3f(0f, 1f, 0f), Gradient.globalGradient(9000) * 360, orb)

                val beamOrigin = spawn.coordinates.toVec3d().add(Vec3d(1.0, 0.0, 0.0).multiply(Config.spawns.platformRadius.toDouble()))
                val beamLine = ParticleRenderer.generateLine(beamOrigin, orb, 0.2)

                val particles = mutableSetOf<Vec3d>()
                particles.addAll(orbM.getVertices())

                (0 until 8).forEach random@{ vertex ->
                    if (Random.nextDouble() < 0.5) return@random
                    particles.add(beamLine.map { point ->
                        point.subtract(orb).rotateAroundVector(Vector3f(0f, 1f, 0f), 45f * vertex).add(orb)
                    }[round(Gradient.globalGradient(2000) * beamLine.lastIndex).toInt()])
                }

                PlayerManager.getOnlinePlayers().forEach inner@{ player ->
                    // we're using force on the particle, this makes the default range 512 blocks (instead of 32), so let's trim that
                    if (player.pos.subtract(spawn.coordinates.toVec3d()).lengthSquared() > VISIBILITY_RANGE.pow(2.0))
                    {
                        return@inner
                    }

                    val orbParticle = DustParticleEffect(teamsOnSpawn.find { it != null && it != SpawnManager.getTeam(spawn) }?.getColorVector() ?: Vector3f(0f, 0f, 0f), 1f)
                    ParticleRenderer.drawMultipleParticlesWorld(server, player, orbParticle, particles)
                }
            }
        }
    }

    class Gradient
    {
        companion object
        {
            val data: Map<Vec3d, Int> = mutableMapOf()

            /**
             * Calculates the percentage of passed rotation based on given rotation time. This synchronizes all animations using this function.
             * @return Percentage of the gradient.
             */
            fun globalGradient(rotationTimeInMillis: Int): Double
            {
                return (System.currentTimeMillis() % rotationTimeInMillis).toDouble() / rotationTimeInMillis.toDouble()
            }

            /**
             * Calculates the percentage of passed rotation based on given rotation time and an offset, which enables you to have asynchronous animations.
             * @return Percentage of the gradient.
             */
//            fun gradient(startTime: Long, rotationTimeInMillis: Int): Double
//            {
//                val diff = System.currentTimeMillis() % rotationTimeInMillis
//                val returnVal = (currentTime - lastTime % rotationTimeInMillis).toDouble() / rotationTimeInMillis.toDouble()
//                return returnVal
//            }
        }
    }
}