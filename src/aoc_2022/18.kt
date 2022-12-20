package aoc_2022.d18

import java.io.File

data class CubePosition(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: CubePosition): CubePosition {
        return CubePosition(x = x + other.x, y = y + other.y, z = z + other.z)
    }

    val neighbors by lazy {
        val diffs = listOf(
            CubePosition(1, 0, 0),
            CubePosition(-1, 0, 0),
            CubePosition(0, 1, 0),
            CubePosition(0, -1, 0),
            CubePosition(0, 0, 1),
            CubePosition(0, 0, -1)
        )

        diffs.map { it + this }
    }
}

fun CubePosition.exposedSidesCount(cubePositions: CubePositions): Int {
    return neighbors.count { it !in cubePositions }
}

fun String.toCubePosition(): CubePosition {
    val (rawX, rawY, rawZ) = this.split(",")
    return CubePosition(x = rawX.toInt(), y = rawY.toInt(), z = rawZ.toInt())
}

typealias CubePositions = Set<CubePosition>

fun main() {
    val lines = File("src/aoc_2022/inputs/18.txt").readLines()
    val cubePositions: CubePositions = lines.map { it.toCubePosition() }.toSet()

    val minCubePosition = CubePosition(
        x = cubePositions.minOf { it.x } - 1,
        y = cubePositions.minOf { it.y } - 1,
        z = cubePositions.minOf { it.z } - 1,
    )
    val maxCubePosition = CubePosition(
        x = cubePositions.maxOf { it.x } + 1,
        y = cubePositions.maxOf { it.y } + 1,
        z = cubePositions.maxOf { it.z } + 1,
    )

    var result = 0
    val explored = mutableSetOf<CubePosition>()
    fun bfs(cubePosition: CubePosition) {
        if (cubePosition in explored) return
        if (cubePosition.x < minCubePosition.x || cubePosition.y < minCubePosition.y || cubePosition.z < minCubePosition.z) return
        if (cubePosition.x > maxCubePosition.x || cubePosition.y > maxCubePosition.y || cubePosition.z > maxCubePosition.z) return
        explored.add(cubePosition)

        result += 6 - cubePosition.exposedSidesCount(cubePositions)
        for (neighbor in cubePosition.neighbors - cubePositions) {
            bfs(neighbor)
        }
    }
    bfs(minCubePosition)

    println(result)
}