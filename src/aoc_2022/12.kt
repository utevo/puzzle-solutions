package aoc_2022.d12

import java.io.File

typealias Height = Short

fun Char.toHeight(): Height {
    return (this.code - 'a'.code).toShort()
}

data class Position(val y: Int, val x: Int)

data class Area(val heights: List<List<Height>>, val start: Position, val end: Position) {
    val m by lazy {
        heights.size
    }
    val n by lazy {
        heights[0].size
    }

    operator fun get(pos: Position): Height {
        return heights[pos.y][pos.x]
    }
}

typealias RawHeightmapRow = List<Char>

fun List<RawHeightmapRow>.toArea(): Area {
    val heights: MutableList<List<Height>> = mutableListOf()
    var start: Position? = null
    var end: Position? = null

    for ((yIdx, rawHeightmapRow) in this.withIndex()) {
        val heightsRow = mutableListOf<Height>()
        for ((xIdx, rawHeightmapCell) in rawHeightmapRow.withIndex()) {
            when (rawHeightmapCell) {
                'S' -> {
                    start = Position(y = yIdx, x = xIdx)
                    heightsRow.add('a'.toHeight())
                }

                'E' -> {
                    end = Position(y = yIdx, x = xIdx)
                    heightsRow.add('z'.toHeight())
                }

                else -> {
                    heightsRow.add(rawHeightmapCell.toHeight())
                }
            }
        }
        heights.add(heightsRow)
    }

    return Area(heights, start ?: throw Exception(), end ?: throw Exception())
}

fun Area.isPositionOk(pos: Position): Boolean =
    pos.y >= 0 && pos.y < this.m && pos.x >= 0 && pos.x < this.n

fun Area.neighborsOf(pos: Position): List<Position> =
    listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)).map { (yd, xd) ->
        Position(
            y = pos.y + yd,
            x = pos.x + xd
        )
    }.filter { this.isPositionOk(it) }

fun Area.reachableNeighborsOf(pos: Position): List<Position> =
    this.neighborsOf(pos).filter { this[it] <= (this[pos] + 1) }


typealias BfsParents = Map<Position, Position?>

fun BfsParents.distanceTo(pos: Position): Int {
    val parent = this[pos] ?: return 0

    return this.distanceTo(pos = parent) + 1
}

fun Area.getBfsParents(): BfsParents {
    val parents = mutableMapOf<Position, Position?>().withDefault { null }
    val explored = mutableSetOf<Position>()
    val queue = ArrayDeque<Position>()
    queue.add(this.start)
    explored.add(this.start)

    while (!queue.isEmpty()) {
        val currPos = queue.removeFirst()

        for (neighbor in this.reachableNeighborsOf(currPos)) {
            if (neighbor in explored) {
                continue
            }
            parents[neighbor] = currPos
            explored.add(neighbor)
            queue.add(neighbor)
        }
    }

    return parents
}


fun main() {
    val lines = File("src/aoc_2022/inputs/12.txt").readLines()
    val rawHeightmapRows: List<RawHeightmapRow> = lines.map { it.toList() }

    val area = rawHeightmapRows.toArea()
    val bfsParents = area.getBfsParents()
    println(bfsParents.distanceTo(area.end))
}