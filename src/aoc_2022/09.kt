package aoc_2022

import java.io.File
import kotlin.math.*

enum class MoveDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

data class Position(val y: Int, val x: Int) {
    fun moved(moveDirection: MoveDirection): Position =
        when (moveDirection) {
            MoveDirection.UP -> copy(y = this.y + 1)
            MoveDirection.DOWN -> copy(y = this.y - 1)
            MoveDirection.LEFT -> copy(x = this.x - 1)
            MoveDirection.RIGHT -> copy(x = this.x + 1)
        }

    fun attractedBy(somePosition: Position): Position =
        when (PositionsDistance.between(this, somePosition)) {
            PositionsDistance.ZERO, PositionsDistance.ONE -> this
            PositionsDistance.TWO_OR_MORE -> {
                val yDiff = somePosition.y - this.y
                val xDiff = somePosition.x - this.x
                val newY = this.y + yDiff.coerceIn(-1, 1)
                val newX = this.x + xDiff.coerceIn(-1, 1)

                Position(y = newY, x = newX)
            }
        }
}

enum class PositionsDistance {
    ZERO,
    ONE,
    TWO_OR_MORE;

    companion object {
        fun between(position: Position, otherPosition: Position): PositionsDistance {
            val yDistance = abs(position.y - otherPosition.y)
            val xDistance = abs(position.x - otherPosition.x)

            if (yDistance == 1 && xDistance == 1) {
                return ONE
            }
            if (xDistance + yDistance == 0) {
                return ZERO
            }
            if (xDistance + yDistance == 1) {
                return ONE
            }

            return TWO_OR_MORE
        }
    }
}

data class State(val headPosition: Position, val tailPositions: List<Position>) {
    fun afterHeadMove(headMoveDirection: MoveDirection): State {
        val newHeadPosition = this.headPosition.moved(headMoveDirection)
        val newTailPositions = this.tailPositions.toMutableList()
        newTailPositions[0] = this.tailPositions[0].attractedBy(newHeadPosition)

        for (i in 1 until this.tailPositions.size) {
            newTailPositions[i] = this.tailPositions[i].attractedBy(
                newTailPositions[i - 1],
            )
        }

        return State(
            headPosition = newHeadPosition,
            tailPositions = newTailPositions,
        )
    }
}

data class Command(val headMove: MoveDirection, val quantity: Int)

typealias PrevTailPositions = MutableSet<Position>

fun String.toCommand(): Command {
    val (rawMoveDirection, rawQuantity) = this.split(" ")
    return Command(headMove = rawMoveDirection.toMoveDirection(), quantity = rawQuantity.toInt())
}

fun String.toMoveDirection(): MoveDirection =
    when (this) {
        "U" -> MoveDirection.UP
        "D" -> MoveDirection.DOWN
        "L" -> MoveDirection.LEFT
        "R" -> MoveDirection.RIGHT
        else -> throw Exception()
    }

const val numberOfTailPositions = 9

fun main() {
    val rawCommands = File("src/aoc_2022/inputs/9.txt").readLines()
    val commands = rawCommands.map { it.toCommand() }

    var state = State(headPosition = Position(y = 0, x = 0), tailPositions = (1..numberOfTailPositions).map { Position(y = 0, x = 0) })

    val prevTailPositions: PrevTailPositions = mutableSetOf()

    for (command in commands) {
        for (i in 1 .. command.quantity) {
            state = state.afterHeadMove(command.headMove)
            prevTailPositions.add(state.tailPositions.last())
        }
    }

    println(prevTailPositions.size)
}
