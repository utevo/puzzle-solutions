package aoc_2022.d15

import java.io.File
import kotlin.math.abs

data class Position(val y: Int, val x: Int) {
    fun manhattanDistanceTo(other: Position): Int = abs(y - other.y) + abs(x - other.x)
}

fun Position.manhattanPointsWithDistance(distance: Int): List<Position> {
    val position = mutableListOf<Position>()

    val top = this.copy(y = this.y - distance)
    position.add(top)
    var curr = top
    for (i in 1 until distance) {
        curr = curr.copy(y = curr.y + 1, x = curr.x + 1)
        position.add(curr)
    }

    val right = this.copy(x = this.x + distance)
    position.add(right)
    curr = right
    for (i in 1 until distance) {
        curr = curr.copy(y = curr.y + 1, x = curr.x - 1)
        position.add(curr)
    }

    val bottom = this.copy(y = this.y + distance)
    position.add(bottom)
    curr = bottom
    for (i in 1 until distance) {
        curr = curr.copy(y = curr.y - 1, x = curr.x - 1)
        position.add(curr)
    }

    val left = this.copy(x = this.x - distance)
    position.add(left)
    curr = left
    for (i in 1 until distance) {
        curr = curr.copy(y = curr.y - 1, x = curr.x + 1)
        position.add(curr)
    }

    return position
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
        sensorPosition = rawSensorPosition.toPosition(), closedBaconPosition = rawClosedBaconPosition.toPosition()
    )
}

fun List<SensorClosedBacon>.canBaconExist(baconPosition: Position): Boolean {
    for (sensorClosedBacon in this) {
        if (sensorClosedBacon.closedBaconPosition == baconPosition) {
            return false
        }
        if (sensorClosedBacon.sensorPosition == baconPosition) {
            return false
        }
        if (sensorClosedBacon.distance >= sensorClosedBacon.sensorPosition.manhattanDistanceTo(baconPosition)) {
            return false
        }
    }

    return true
}

const val MAX_DISTANCE = 4000000

fun Position.tuningFrequency(): Long {
    return 4000000L * this.x + this.y
}

fun main() {
    val lines = File("src/aoc_2022/inputs/15.txt").readLines()
    val sensorClosedBaconList = lines.map { it.toSensorClosedBacon() }

    for (sensorClosedBacon in sensorClosedBaconList) {
        val positionsToCheck =
            sensorClosedBacon.sensorPosition.manhattanPointsWithDistance(sensorClosedBacon.distance + 1)
        for (positionToCheck in positionsToCheck) {
            if (positionToCheck.x in (0..MAX_DISTANCE) && positionToCheck.y in (0..MAX_DISTANCE)) {
                if (sensorClosedBaconList.canBaconExist(positionToCheck)) {
                    println(positionToCheck.tuningFrequency())
                    return
                }
            }
        }
    }
}


