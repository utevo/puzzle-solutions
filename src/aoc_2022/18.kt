package aoc_2022.d18

import java.io.File

data class CubePosition(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: CubePosition): CubePosition {
        return CubePosition(x = x + other.x, y = y + other.y, z = z + other.z)
    }
}

fun CubePosition.exposedSidesCount(cubePositions: CubePositions): Int {
    val DIR = listOf(
        CubePosition(1, 0, 0),
        CubePosition(-1, 0, 0),
        CubePosition(0, 1, 0),
        CubePosition(0, -1, 0),
        CubePosition(0, 0, 1),
        CubePosition(0, 0, -1)
    )

    return DIR.map { it + this }.filter { it !in cubePositions }.count()
}

fun String.toCubePosition(): CubePosition {
    val (rawX, rawY, rawZ) = this.split(",")
    return CubePosition(x = rawX.toInt(), y = rawY.toInt(), z = rawZ.toInt())
}

typealias CubePositions = Set<CubePosition>

fun main() {
    val lines = File("src/aoc_2022/inputs/18.txt").readLines()
    val cubePositions: CubePositions = lines.map { it.toCubePosition() }.toSet()

    val result = cubePositions.map { it.exposedSidesCount(cubePositions - it) }.sum()
    println(result)
}