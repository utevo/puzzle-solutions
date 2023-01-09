package aoc_2022.d25

import java.io.File
import kotlin.math.pow

const val POWER = 5

enum class SnafuDigit {
    DoubleMinus,
    Minus,
    Zero,
    One,
    Two;

    override fun toString(): String {
        return when (this) {
            DoubleMinus -> "="
            Minus -> "-"
            Zero -> "0"
            One -> "1"
            Two -> "2"
        }
    }

    fun toLong(): Long {
        return when (this) {
            DoubleMinus -> -2
            Minus -> -1
            Zero -> 0
            One -> 1
            Two -> 2
        }
    }
}

fun Char.toSnafuDigit(): SnafuDigit {
    return when (this) {
        '=' -> SnafuDigit.DoubleMinus
        '-' -> SnafuDigit.Minus
        '0' -> SnafuDigit.Zero
        '1' -> SnafuDigit.One
        '2' -> SnafuDigit.Two
        else -> throw IllegalArgumentException()
    }
}

data class SnafuNumber(val digits: List<SnafuDigit>) {
    fun toLong(): Long {
        return digits.reversed()
            .foldIndexed(0) { index, sum, snafuDigit ->
                sum + (snafuDigit.toLong() * POWER.toDouble().pow(index)).toLong()
            }
    }

    override fun toString(): String {
        return digits.joinToString("") { it.toString() }
    }
}

fun String.toSnafuNumber(): SnafuNumber {
    return SnafuNumber(this.map { it.toSnafuDigit() })
}

fun Long.toSnafuNumber(): SnafuNumber {
    val reversedDigits = mutableListOf<SnafuDigit>()

    var value = this
    while (value > 0) {
        val newSnafuDigit =
            when (value.mod(POWER)) {
                0 -> SnafuDigit.Zero
                1 -> SnafuDigit.One
                2 -> SnafuDigit.Two
                3 -> SnafuDigit.DoubleMinus
                4 -> SnafuDigit.Minus
                else -> throw IllegalStateException()
            }
        reversedDigits.add(newSnafuDigit)
        value = (value - newSnafuDigit.toLong()).floorDiv(POWER)
    }

    return SnafuNumber(reversedDigits.reversed())
}

fun main() {
    val lines = File("src/aoc_2022/inputs/25.txt").readLines()
    val snafuNumbers = lines.map { it.toSnafuNumber() }
    val sum = snafuNumbers.sumOf { it.toLong() }

    println(sum.toSnafuNumber())
}