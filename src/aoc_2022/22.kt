package aoc_2022.d22


import java.io.File


enum class BoardElement {
    Open,
    Wall,
    Nothing,
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

data class BoardPosition(val y: Int, val x: Int)

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


data class Board(private val elementRows: List<BoardElementRow>) {
    private fun at(position: BoardPosition): BoardElement {
        return elementRows.getOrNull(position.y)?.getOrNull(position.x) ?: BoardElement.Nothing
    }

    fun positionAfterStep(currPosition: BoardPosition, direction: Direction): BoardPosition {
        val positionInGivenDirection = positionInDirection(currPosition, direction)
        return when (at(positionInGivenDirection)) {
            BoardElement.Open -> positionInGivenDirection
            BoardElement.Wall -> currPosition
            else -> throw IllegalStateException("This state should be impossible")
        }
    }

    private fun positionInDirection(currPosition: BoardPosition, direction: Direction): BoardPosition {
        var newPosition = rawPositionInDirection(currPosition, direction)
        while (at(newPosition) == BoardElement.Nothing) {
            newPosition = rawPositionInDirection(newPosition, direction)
        }

        return newPosition
    }

    private fun rawPositionInDirection(currPosition: BoardPosition, direction: Direction): BoardPosition {
        return when (direction) {
            Direction.Right -> currPosition.copy(x = (currPosition.x + 1) % (elementRows[currPosition.y].size))
            Direction.Left -> currPosition.copy(x = (currPosition.x - 1).modPositive(elementRows[currPosition.y].size))
            Direction.Down -> currPosition.copy(y = (currPosition.y + 1) % (elementRows.size))
            Direction.Up -> currPosition.copy(y = (currPosition.y - 1).modPositive(elementRows.size))
        }
    }

    val topLeftOpenTitlePosition: BoardPosition by lazy {
        val x = elementRows[0].indexOf(BoardElement.Open)
        BoardPosition(y = 0, x = x)
    }
}

fun List<String>.toBoard(): Board {
    val elementRows = this.map { list -> list.map { it.toBoardElement() } }
    return Board(elementRows)
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


data class State(val position: BoardPosition, val direction: Direction) {
    fun afterSteps(numberOfSteps: Int, board: Board): State {
        var newPosition = position
        repeat(numberOfSteps) {
            newPosition = board.positionAfterStep(newPosition, direction)
        }
        return copy(position = newPosition)
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

