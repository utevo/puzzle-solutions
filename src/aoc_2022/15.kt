package aoc_2022.d15

import java.io.File
import kotlin.math.abs

data class Position(val y: Int, val x: Int) {
    fun manhattanDistanceTo(other: Position): Int = abs(y - other.y) + abs(x - other.x)
}

data class SensorClosedBacon(val sensorPosition: Position, val closedBaconPosition: Position) {
    val distance: Int by lazy { sensorPosition.manhattanDistanceTo(closedBaconPosition) }
}

fun String.toSensorClosedBacon(): SensorClosedBacon {
    fun String.toPosition(): Position {
        val (x, y) = this.split(",")
        return Position(y = y.toInt(), x = x.toInt())
    }

    val (rawSensorPosition, rawClosedBaconPosition) = this.split(" ")
    return SensorClosedBacon(
        sensorPosition = rawSensorPosition.toPosition(),
        closedBaconPosition = rawClosedBaconPosition.toPosition()
    )
}

fun List<SensorClosedBacon>.canBaconExist(baconPosition: Position): Boolean {
    for (sensorClosedBacon in this) {
        if (sensorClosedBacon.closedBaconPosition == baconPosition) {
            return true
        }
        if (sensorClosedBacon.distance >= sensorClosedBacon.sensorPosition.manhattanDistanceTo(baconPosition)) {
            return false
        }
    }

    return true
}

const val ASKED_Y = 2000000

fun main() {
    val lines = File("src/aoc_2022/inputs/15.txt").readLines()
    val sensorClosedBaconList = lines.map { it.toSensorClosedBacon() }

    val minX = sensorClosedBaconList.minOf { it.sensorPosition.x - it.distance }
    val maxX = sensorClosedBaconList.maxOf { it.sensorPosition.x + it.distance }
    println("minX: $minX, maxX: $maxX")

    val possibleBaconPositions =
        (minX..maxX).map { Position(y = ASKED_Y, x = it) }.filter { !sensorClosedBaconList.canBaconExist(it) }

    println(possibleBaconPositions.size)
}


