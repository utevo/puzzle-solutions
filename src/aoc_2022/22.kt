package aoc_2022.d22

import java.io.File
import java.lang.Exception

enum class BoardElement {
    Open,
    Wall,
    Nothing
}

fun Char.toBoardElement(): BoardElement {
    return when (this) {
        '.' -> BoardElement.Open
        '#' -> BoardElement.Wall
        ' ' -> BoardElement.Nothing
        else -> throw IllegalArgumentException()
    }
}

typealias BoardElementRow = List<BoardElement>

data class Position(val y: Int, val x: Int)

enum class Direction {
    Right,
    Down,
    Left,
    Up
}

fun Int.modPositive(other: Int): Int {
    val rawResult = this.mod(other)
    return if (rawResult < 0) rawResult + other else rawResult
}


typealias Portals = Map<State, State>
typealias MutablePortals = MutableMap<State, State>

data class Board(private val elementRows: List<BoardElementRow>, private val portals: Portals) {
    private fun at(position: Position): BoardElement {
        return elementRows.getOrNull(position.y)?.getOrNull(position.x) ?: throw IllegalStateException()
    }

    fun stateAfterStep(currState: State): State {
        val dumpState = dumpStateAfterStep(currState)
        return when (at(dumpState.position)) {
            BoardElement.Open -> dumpState
            BoardElement.Wall -> currState
            else -> throw IllegalStateException()
        }
    }


    private fun dumpStateAfterStep(state: State): State {
        val maybeState = portals[state]
        if (maybeState is State) {
            return maybeState
        }

        return when (state.direction) {
            Direction.Right -> state.copy(position = state.position.copy(x = state.position.x + 1))
            Direction.Left -> state.copy(position = state.position.copy(x = state.position.x - 1))
            Direction.Down -> state.copy(position = state.position.copy(y = state.position.y + 1))
            Direction.Up -> state.copy(position = state.position.copy(y = state.position.y - 1))
        }

    }

    val topLeftOpenTitlePosition: Position by lazy {
        val x = elementRows[0].indexOf(BoardElement.Open)
        Position(y = 0, x = x)
    }
}


enum class TurnKind {
    Left,
    Right,
}

fun Direction.toInt(): Int {
    return when (this) {
        Direction.Up -> 0
        Direction.Right -> 1
        Direction.Down -> 2
        Direction.Left -> 3
    }
}

fun Int.toDirection(): Direction {
    return when (this) {
        0 -> Direction.Up
        1 -> Direction.Right
        2 -> Direction.Down
        3 -> Direction.Left
        else -> throw IllegalArgumentException()
    }
}

fun Direction.afterTurn(kind: TurnKind): Direction {
    return when (kind) {
        TurnKind.Left -> ((this.toInt() - 1).modPositive(Direction.values().size)).toDirection()
        TurnKind.Right -> ((this.toInt() + 1) % Direction.values().size).toDirection()
    }
}


data class State(val position: Position, val direction: Direction) {
    fun afterSteps(numberOfSteps: Int, board: Board): State {
        var newState = this
        repeat(numberOfSteps) {
            newState = board.stateAfterStep(newState)
        }
        return newState
    }

    fun afterTurn(kind: TurnKind): State {
        return copy(direction = direction.afterTurn(kind))
    }
}

data class PathDescription(val operations: List<Operation>) {
    sealed class Operation {
        data class MoveForward(val stepsCount: Int) : Operation()
        data class Turn(val kind: TurnKind) : Operation()
    }
}

fun String.toPathDescription(): PathDescription {
    val thisWithSpaces = this.replace("R", " R ").replace("L", " L ")
    val operations = thisWithSpaces.split(" ").map {
        when (it) {
            "L" -> PathDescription.Operation.Turn(TurnKind.Left)
            "R" -> PathDescription.Operation.Turn(TurnKind.Right)
            else -> PathDescription.Operation.MoveForward(it.toInt())
        }
    }
    return PathDescription(operations)
}

fun State.toPassword(): Int {
    fun Direction.toPasswordInt(): Int {
        return when (this) {
            Direction.Right -> 0
            Direction.Down -> 1
            Direction.Left -> 2
            Direction.Up -> 3
        }
    }

    return 1000 * (this.position.y + 1) + 4 * (this.position.x + 1) + this.direction.toPasswordInt()
}


fun List<String>.toBoard(): Board {
    val elementRows = this.map { list -> list.map { it.toBoardElement() } }

    // expected specific shape of map
    val portals: MutablePortals = mutableMapOf()

    run {
        val fromY = 0
        for (fromX in EDGE_LENGTH until (2 * EDGE_LENGTH)) {
            val toX = 0
            val toY = 3 * EDGE_LENGTH + fromX - EDGE_LENGTH
            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Up)] =
                State(position = toPosition, direction = Direction.Right)
            portals[State(position = toPosition, direction = Direction.Left)] =
                State(position = fromPosition, direction = Direction.Down)
        }
    }
    if (portals[State(
            position = Position(y = 0, x = EDGE_LENGTH),
            direction = Direction.Up
        )] != State(position = Position(y = 3 * EDGE_LENGTH, x = 0), direction = Direction.Right)
    ) {
        throw Exception()
    }

    run {
        val fromY = 0
        for (fromX in (2 * EDGE_LENGTH) until (3 * EDGE_LENGTH)) {
            val toY = 4 * EDGE_LENGTH - 1
            val toX = fromX - 2 * EDGE_LENGTH

            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Up)] =
                State(position = toPosition, direction = Direction.Up)
            portals[State(position = toPosition, direction = Direction.Down)] =
                State(position = fromPosition, direction = Direction.Down)
        }
    }
    if (portals[State(
            position = Position(y = 0, x = 2 * EDGE_LENGTH),
            direction = Direction.Up
        )] != State(position = Position(y = 4 * EDGE_LENGTH - 1, x = 0), direction = Direction.Up)
    ) {
        throw Exception()
    }

    run {
        val fromX = 3 * EDGE_LENGTH - 1
        for (fromY in 0 until EDGE_LENGTH) {
            val toX = 2 * EDGE_LENGTH - 1
            val toY = 3 * EDGE_LENGTH - 1 - fromY

            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Right)] =
                State(position = toPosition, direction = Direction.Left)
            portals[State(position = toPosition, direction = Direction.Right)] =
                State(position = fromPosition, direction = Direction.Left)
        }
    }
    if (portals[State(
            position = Position(y = 0, x = 3 * EDGE_LENGTH - 1),
            direction = Direction.Right
        )] != State(position = Position(y = 3 * EDGE_LENGTH - 1, x = 2 * EDGE_LENGTH - 1), direction = Direction.Left)
    ) {
        throw Exception()
    }

    run {
        val fromY = 1 * EDGE_LENGTH - 1
        for (fromX in (2 * EDGE_LENGTH) until (3 * EDGE_LENGTH)) {
            val toX = 2 * EDGE_LENGTH - 1
            val toY = EDGE_LENGTH + fromX - (2 * EDGE_LENGTH)

            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Down)] =
                State(position = toPosition, direction = Direction.Left)
            portals[State(position = toPosition, direction = Direction.Right)] =
                State(position = fromPosition, direction = Direction.Up)
        }
    }
    if (portals[State(
            position = Position(y = EDGE_LENGTH - 1, x = 2 * EDGE_LENGTH),
            direction = Direction.Down
        )] != State(position = Position(y = EDGE_LENGTH, x = 2 * EDGE_LENGTH - 1), direction = Direction.Left)
    ) {
        throw Exception()
    }

    run {
        val fromY = 3 * EDGE_LENGTH - 1
        for (fromX in EDGE_LENGTH until (2 * EDGE_LENGTH)) {
            val toX = EDGE_LENGTH - 1
            val toY = 3 * EDGE_LENGTH + fromX - EDGE_LENGTH

            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Down)] =
                State(position = toPosition, direction = Direction.Left)
            portals[State(position = toPosition, direction = Direction.Right)] =
                State(position = fromPosition, direction = Direction.Up)
        }
    }
    if (portals[State(
            position = Position(y = 3 * EDGE_LENGTH - 1, x = EDGE_LENGTH),
            direction = Direction.Down
        )] != State(position = Position(y = 3 * EDGE_LENGTH, x = EDGE_LENGTH - 1), direction = Direction.Left)
    ) {
        throw Exception()
    }

    run {
        val fromX = 0
        for (fromY in (2 * EDGE_LENGTH) until (3 * EDGE_LENGTH)) {
            val toX = EDGE_LENGTH
            val toY = EDGE_LENGTH - 1 - (fromY - (2 * EDGE_LENGTH))

            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Left)] =
                State(position = toPosition, direction = Direction.Right)
            portals[State(position = toPosition, direction = Direction.Left)] =
                State(position = fromPosition, direction = Direction.Right)
        }
    }
    if (portals[State(
            position = Position(y = 0, x = EDGE_LENGTH),
            direction = Direction.Left
        )] != State(position = Position(y = 3 * EDGE_LENGTH - 1, x = 0), direction = Direction.Right)
    ) {
        throw Exception()
    }

    run {
        val fromX = EDGE_LENGTH
        for (fromY in EDGE_LENGTH until (2 * EDGE_LENGTH)) {
            val toY = (2 * EDGE_LENGTH)
            val toX = fromY - EDGE_LENGTH

            val fromPosition = Position(y = fromY, x = fromX)
            val toPosition = Position(y = toY, x = toX)
            portals[State(position = fromPosition, direction = Direction.Left)] =
                State(position = toPosition, direction = Direction.Down)
            portals[State(position = toPosition, direction = Direction.Up)] =
                State(position = fromPosition, direction = Direction.Right)
        }
    }
    if (portals[State(
            position = Position(y = EDGE_LENGTH, x = EDGE_LENGTH),
            direction = Direction.Left
        )] != State(position = Position(y = (2 * EDGE_LENGTH), x = 0), direction = Direction.Down)
    ) {
        throw Exception()
    }

    return Board(elementRows, portals)
}

const val EDGE_LENGTH = 50

fun main() {
    val lines = File("src/aoc_2022/inputs/22.txt").readLines()

    val idxOfEmptyLine = lines.indexOf("")
    val rawBoard = lines.subList(0, idxOfEmptyLine)
    val rawPathDescription = lines[idxOfEmptyLine + 1]
    val board = rawBoard.toBoard()
    val pathDescription = rawPathDescription.toPathDescription()

    var state = State(position = board.topLeftOpenTitlePosition, direction = Direction.Right)

    for (operation in pathDescription.operations) {
        state = when (operation) {
            is PathDescription.Operation.MoveForward -> {
                state.afterSteps(operation.stepsCount, board)
            }

            is PathDescription.Operation.Turn -> {
                state.afterTurn(operation.kind)
            }
        }
    }

    println("state: $state")
    println("password: ${state.toPassword()}")
}

