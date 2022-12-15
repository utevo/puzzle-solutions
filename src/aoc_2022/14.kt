package aoc_2022.d14

import java.io.File

fun bidirectionalRangeTo(some: Int, other: Int): IntRange = if (some <= other) (some..other) else (other..some)

data class Position(val y: Int, val x: Int) {
    fun wentDown(): Position = this.copy(y = this.y + 1)
    fun wentDownLeft(): Position = this.copy(y = this.y + 1, x = this.x - 1)
    fun wentDownRight(): Position = this.copy(y = this.y + 1, x = this.x + 1)
}

data class RockPath(val points: List<Position>) {
    fun positions(): Set<Position> {
        val positions = HashSet<Position>()
        for ((startPoint, endPoint) in this.points.windowed(2)) {
            if (startPoint.y == endPoint.y) {
                bidirectionalRangeTo(startPoint.x, endPoint.x).forEach { positions.add(Position(startPoint.y, it)) }
            } else if (startPoint.x == endPoint.x) {
                bidirectionalRangeTo(startPoint.y, endPoint.y).forEach { positions.add(Position(it, startPoint.x)) }
            } else {
                throw Exception("Impossible state")
            }
        }
        return positions
    }
}

fun String.toRockPath(): RockPath {
    fun String.toPosition(): Position {
        val (x, y) = this.split(",")
        return Position(y.toInt(), x.toInt())
    }

    val rawPoints = this.split(" -> ")
    return RockPath(points = rawPoints.map { it.toPosition() })
}

val SAND_START_POSITION = Position(x = 500, y = 0)

class Cave(rockPaths: List<RockPath>) {
    var objectsPositions = HashSet<Position>()
    val yOfLowestObject: Int by lazy {
        objectsPositions.map { it.y }.max()
    }

    init {
        for (rockPath in rockPaths) {
            objectsPositions.addAll(rockPath.positions())
        }
    }

    enum class AddSandResult {
        SandStop,
        SandFall
    }

    fun addSand(): AddSandResult {
        var currSandPosition = SAND_START_POSITION.copy()
        while (currSandPosition.y < yOfLowestObject) {
            when (sandStep(currSandPosition)) {
                SandStep.Stop -> {
                    objectsPositions.add(currSandPosition)
                    return AddSandResult.SandStop
                }

                SandStep.GoDown -> {
                    currSandPosition = currSandPosition.wentDown()
                }

                SandStep.GoDownLeft -> {
                    currSandPosition = currSandPosition.wentDownLeft()
                }

                SandStep.GoDownRight -> {
                    currSandPosition = currSandPosition.wentDownRight()
                }
            }
        }
        return AddSandResult.SandFall
    }


    sealed class SandStep {
        object GoDown : SandStep()
        object GoDownLeft : SandStep()
        object GoDownRight : SandStep()
        object Stop : SandStep()
    }

    private fun sandStep(sandPosition: Position): SandStep {
        if (sandPosition.wentDown() !in objectsPositions) {
            return SandStep.GoDown
        }
        if (sandPosition.wentDownLeft() !in objectsPositions) {
            return SandStep.GoDownLeft
        }
        if (sandPosition.wentDownRight() !in objectsPositions) {
            return SandStep.GoDownRight
        }

        return SandStep.Stop
    }
}


fun main() {
    val lines = File("src/aoc_2022/inputs/14.txt").readLines()
    val rockPaths = lines.map { it.toRockPath() }
    val cave = Cave(rockPaths = rockPaths)

    var result = 0
    while (cave.addSand() != Cave.AddSandResult.SandFall) {
        result++
    }

    println(result)
}