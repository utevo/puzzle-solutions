package aoc_2022.d20

import java.io.File

class Move(val value: Long)

fun MutableList<Move>.cyclicMove(currIdx: Int) {
    val move = this.removeAt(currIdx)
    val prevIdx = (currIdx - 1).toLong().modPositive(this.size.toLong())
    val newPrevIdx = (prevIdx + move.value).modPositive(this.size.toLong())
    val newIdx = (newPrevIdx + 1) % this.size
    this.add(newIdx.toInt(), move)
}

fun Long.modPositive(other: Long): Long {
    val rawResult = this.mod(other)
    return if (rawResult < 0) rawResult + other else rawResult
}

const val DECRYPTION_KEY = 811589153

fun main() {
    val lines = File("src/aoc_2022/inputs/20.txt").readLines()
    val moves = lines.map { Move(it.toLong() * DECRYPTION_KEY) }
    val encrypted = moves.toMutableList()

    for (a in 0 until 10) {
        for (move in moves) {
            val currIdx = encrypted.indexOf(move)
            encrypted.cyclicMove(currIdx)
        }
    }

    val idxOf0 = encrypted.withIndex().find { it.value.value == 0L }!!.index
    val numbers = listOf(1000, 2000, 3000).map { encrypted[(idxOf0 + it) % encrypted.size] }
    val result = numbers.sumOf { it.value }
    println(numbers.map { it.value })
    println("result: $result")
}