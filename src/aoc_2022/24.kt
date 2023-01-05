package aoc_2022.d24

import java.io.File


enum class Direction {
    N, E, S, W;

    override fun toString(): String {
        return when (this) {
            N -> "^"
            E -> ">"
            S -> "v"
            W -> "<"
        }
    }

    companion object {
        fun fromChar(char: Char): Direction {
            return when (char) {
                '^' -> N
                '>' -> E
                'v' -> S
                '<' -> W
                else -> throw IllegalArgumentException()
            }
        }
    }
}

data class Position2D(val y: Int, val x: Int)
data class Storm(val dir: Direction)

sealed class ValleyItem {
    data class Storms(val value: List<Storm>) : ValleyItem()
    object Wall : ValleyItem()
}

data class Valley(
    val itemByPosition: Map<Position2D, ValleyItem>,
    val y: Int,
    val x: Int
) {
    val startPosition by lazy {
        val startX = (0 until x).first { itemByPosition[Position2D(y = 0, x = it)] == null }
        Position2D(y = 0, x = startX)
    }

    val endPosition by lazy {
        val endX = (0 until x).first { itemByPosition[Position2D(y = y - 1, x = it)] == null }
        Position2D(y = y - 1, x = endX)
    }

    fun next(): Valley {
        val newItemByPosition = mutableMapOf<Position2D, ValleyItem>()

        for ((position, item) in itemByPosition) {
            when (item) {
                ValleyItem.Wall -> newItemByPosition[position] = item
                is ValleyItem.Storms -> {
                    for (storm in item.value) {
                        val newPosition = calcStormNextPosition(storm = storm, position = position)
                        val currStorms =
                            newItemByPosition.getOrDefault(
                                newPosition,
                                ValleyItem.Storms(listOf())
                            ) as ValleyItem.Storms
                        newItemByPosition[newPosition] =
                            ValleyItem.Storms(value = currStorms.value + storm)
                    }
                }
            }
        }
        return copy(itemByPosition = newItemByPosition)
    }

    override fun toString(): String {
        var asString = ""
        for (yIdx in 0 until y) {
            for (xIdx in 0 until x) {
                asString += when (val item = itemByPosition[Position2D(yIdx, xIdx)]) {
                    ValleyItem.Wall -> '#'
                    null -> '.'
                    is ValleyItem.Storms -> {
                        if (item.value.size > 1) {
                            item.value.size.toString()
                        } else {
                            item.value[0].dir.toString()
                        }
                    }
                }
            }
            asString += '\n'
        }
        return asString
    }

    private fun calcStormNextPosition(storm: Storm, position: Position2D): Position2D {
        val naiveNewPosition = when (storm.dir) {
            Direction.N -> position.copy(y = position.y - 1)
            Direction.E -> position.copy(x = position.x + 1)
            Direction.S -> position.copy(y = position.y + 1)
            Direction.W -> position.copy(x = position.x - 1)
        }

        fun fixedNaivePosition(naivePosition: Position2D): Position2D {
            fun fixedY(naivePosition: Position2D): Int {
                if (itemByPosition[naivePosition] == ValleyItem.Wall) {
                    if (naivePosition.y == 0) {
                        return y - 2
                    }
                    if (naivePosition.y == y - 1) {
                        return 1
                    }
                }
                return naivePosition.y
            }

            fun fixedX(naivePosition: Position2D): Int {
                if (itemByPosition[naivePosition] == ValleyItem.Wall) {
                    if (naivePosition.x == 0) {
                        return x - 2
                    }
                    if (naivePosition.x == x - 1) {
                        return 1
                    }
                }
                return naivePosition.x
            }


            return Position2D(y = fixedY(naivePosition), x = fixedX(naivePosition))
        }

        return fixedNaivePosition(naiveNewPosition)
    }

    fun positionIsOkForPerson(position: Position2D): Boolean {
        if (position.x < 0 || position.x >= x || position.y < 0 || position.y >= y) {
            return false
        }
        return itemByPosition[position] == null
    }
}

fun List<String>.toValley(): Valley {
    val x = this[0].length
    val y = this.size

    val itemByPosition = mutableMapOf<Position2D, ValleyItem>()

    for ((yIdx, row) in this.withIndex()) {
        for ((xIdx, itemAsChar) in row.withIndex()) {
            val position = Position2D(yIdx, xIdx)
            when (itemAsChar) {
                '.' -> Unit
                '#' -> itemByPosition[position] = ValleyItem.Wall
                else -> itemByPosition[position] = ValleyItem.Storms(listOf(Storm(Direction.fromChar(itemAsChar))))
            }
        }
    }
    return Valley(itemByPosition = itemByPosition, y = y, x = x)
}

fun Position2D.neighbors(): Set<Position2D> {
    val dirs: List<Pair<Int, Int>> = listOf(1 to 0, 0 to 1, -1 to 0, 0 to -1)
    return dirs.map { copy(y = y + it.first, x = x + it.second) }.toSet()
}

fun goodNextPositions(position: Position2D, valley: Valley): Set<Position2D> {
    return (position.neighbors() + position).filter { valley.positionIsOkForPerson(it) }.toSet()
}

fun main() {
    val lines = File("src/aoc_2022/inputs/24.txt").readLines()
    var valley = lines.toValley()

    val accessiblePositionsByTime: MutableList<Set<Position2D>> = mutableListOf(setOf(valley.startPosition))

    while (true) {
        valley = valley.next()
        val prevAccessiblePositions = accessiblePositionsByTime.last()
        val currAccessiblePositions = prevAccessiblePositions.flatMap { goodNextPositions(it, valley) }.toSet()
        if (currAccessiblePositions.any { it == valley.endPosition }) {
            println("result: ${accessiblePositionsByTime.size}")
            return
        }
        accessiblePositionsByTime.add(currAccessiblePositions)
    }
}