package aoc_2022.d20

import java.io.File

class Move(val value: Int)

fun MutableList<Move>.cyclicMove(currIdx: Int) {
    val move = this.removeAt(currIdx)
    val prevIdx = (currIdx - 1).modPositive(this.size)
    val newPrevIdx = (prevIdx + move.value).modPositive(this.size)
    val newIdx = (newPrevIdx + 1) % this.size
    this.add(newIdx, move)
}

fun Int.modPositive(other: Int): Int {
    val rawResult = this.mod(other)
    return if (rawResult < 0) rawResult + other else rawResult
}


fun main() {
    val lines = File("src/aoc_2022/inputs/20.txt").readLines()
    val moves = lines.map { Move(it.toInt()) }
    val encrypted = moves.toMutableList()

    for (move in moves) {
        val currIdx = encrypted.indexOf(move)
        encrypted.cyclicMove(currIdx)
    }

    val idxOf0 = encrypted.withIndex().find { it.value.value == 0 }!!.index
    val numbers = listOf(1000, 2000, 3000).map { encrypted[(idxOf0 + it) % encrypted.size] }
    val result = numbers.sumOf { it.value }
    println(numbers.map { it.value })
    println("result: $result")
}