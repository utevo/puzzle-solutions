package aoc_2022.d12

import java.io.File
import kotlin.math.min

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

    data class Entry(val position: Position, val height: Height)

    fun entries(): Iterator<Entry> {
        return object : Iterator<Entry> {
            var nextPosition = Position(0, 0)

            override fun hasNext(): Boolean {
                return nextPosition.y < m
            }

            override fun next(): Entry {
                val currPosition = nextPosition

                nextPosition =
                    Position(
                        y = nextPosition.y + if ((nextPosition.x + 1) % n == 0) 1 else 0,
                        x = (nextPosition.x + 1) % n
                    )
                return Entry(currPosition, get(currPosition))
            }
        }
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


class BfsResult(private val parentsOf: Map<Position, Position?>, private val explored: Set<Position>) {
    fun distanceTo(pos: Position): Int {
        if (pos !in explored) return Int.MAX_VALUE
        val parent = parentsOf[pos] ?: return 0

        return this.distanceTo(pos = parent) + 1
    }

    companion object {
        fun fromArea(area: Area, start: Position = area.start): BfsResult {
            val parents = mutableMapOf<Position, Position?>().withDefault { null }
            val explored = mutableSetOf<Position>()
            val queue = ArrayDeque<Position>()
            queue.add(start)
            explored.add(start)

            while (!queue.isEmpty()) {
                val currPos = queue.removeFirst()

                for (neighbor in area.reachableNeighborsOf(currPos)) {
                    if (neighbor in explored) {
                        continue
                    }
                    parents[neighbor] = currPos
                    explored.add(neighbor)
                    queue.add(neighbor)
                }
            }

            return BfsResult(parents, explored)
        }
    }
}

fun main() {
    val lines = File("src/aoc_2022/inputs/12.txt").readLines()
    val rawHeightmapRows: List<RawHeightmapRow> = lines.map { it.toList() }

    val area = rawHeightmapRows.toArea()

    var minDistance = Int.MAX_VALUE
    for ((position, height) in area.entries()) {
        if (height == 0.toShort()) {
            val bfsResult = BfsResult.fromArea(area, position)
            val distance = bfsResult.distanceTo(area.end)

            minDistance = min(minDistance, distance)
        }
    }
    println(minDistance)
}