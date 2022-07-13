package de.jagenka.gameplay.rendering

import de.jagenka.BlockPos
import kotlin.math.max
import kotlin.math.sqrt

class AStar(val start: BlockPos, val goal: BlockPos)
{
    var path = emptyList<BlockPos>()
        private set

    private val openSet = mutableSetOf(start)
    private val cameFrom = mutableMapOf<BlockPos, BlockPos>()
    private val gScore = mutableMapOf<BlockPos, Double>().withDefault { Double.MAX_VALUE }
    private val fScore = mutableMapOf<BlockPos, Double>().withDefault { Double.MAX_VALUE }

    init
    {

        gScore[start] = 0.0
        fScore[start] = heuristic(start)
        mainLoop@while (openSet.isNotEmpty())
        {
            val current = openSet.minByOrNull { heuristic(it) }!!
            if (current == goal)
            {
                this.path = reconstructPath()
                break@mainLoop
            }

            openSet.remove(current)
            current.getPossibleWalkDestinations().forEach {
                val tentativeGScore = gScore.getValue(current) + cost(current, it)
                if (tentativeGScore < gScore.getValue(it))
                {
                    cameFrom[it] = current
                    gScore[it] = tentativeGScore
                    fScore[it] = tentativeGScore + heuristic(it)
                    if (it !in openSet) openSet.add(it)
                }
            }
        }
    }

    fun heuristic(pos: BlockPos): Double
    {
        return pos.distanceTo(goal.toVec3d())
    }

    fun cost(from: BlockPos, to: BlockPos): Double
    {
        val (dx, dy, dz) = to - from
        return if (dy == 0)
        {
            sqrt((dx * dx + dz * dz).toDouble())
        } else if (dy > 0)
        {
            from.manhattanDistanceTo(to).toDouble()
        } else
        {
            (from.manhattanDistanceTo(to) + max(0, dy - 3)).toDouble()
        }
    }

    fun reconstructPath(): List<BlockPos>
    {
        val path = mutableListOf<BlockPos>()
        var current: BlockPos? = goal
        while (current != null)
        {
            path.add(0, current)
            current = cameFrom[current]
        }
        return path
    }
}