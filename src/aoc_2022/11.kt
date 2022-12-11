package aoc_2022

import java.io.File

typealias Item = Long

sealed class MonkeyOperationExpression
data class IntMOV(val value: Item) : MonkeyOperationExpression()
object OldMOV : MonkeyOperationExpression()


enum class MonkeyOperationOperator {
    ADD, MULTIPLY
}

fun MonkeyOperationExpression.value(old: Item): Item = when (this) {
    OldMOV -> old
    is IntMOV -> this.value
}


data class MonkeyOperation(
    val left: MonkeyOperationExpression, val operator: MonkeyOperationOperator, val right: MonkeyOperationExpression
) {
    fun calc(old: Item): Item {
        val leftValue = left.value(old)
        val rightValue = right.value(old)

        return when (operator) {
            MonkeyOperationOperator.ADD -> leftValue + rightValue
            MonkeyOperationOperator.MULTIPLY -> leftValue * rightValue
        }
    }
}

data class MonkeyTest(val divisibleBy: Int)

data class Monkey(
    val items: List<Item>,
    val operation: MonkeyOperation,
    val test: MonkeyTest,
    val ifTrueTrowTo: Int,
    val ifFalseTrowTo: Int
)

data class MonkeyThrow(val item: Item, val toMonkeyId: Int)


fun Monkey.toMonkeyId(item: Item, newValModulo: Int): Int =
    if ((toNewValue(item, newValModulo) % test.divisibleBy.toLong()) == 0L) ifTrueTrowTo else ifFalseTrowTo

fun Monkey.toNewValue(item: Item, newValModulo: Int): Item = operation.calc(item) % newValModulo

fun Monkey.turnThrows(newValModulo: Int): List<MonkeyThrow> =
    items.map { MonkeyThrow(toNewValue(it, newValModulo), toMonkeyId(it, newValModulo)) }

fun List<String>.toMonkey(): Monkey {
    fun String.toItems(): List<Item> = this.split(", ").map { it.toLong() }

    fun String.toOperation(): MonkeyOperation {
        fun String.toExpression(): MonkeyOperationExpression = when (this) {
            "old" -> OldMOV
            else -> IntMOV(this.toLong())
        }

        fun String.toOperator(): MonkeyOperationOperator = when (this) {
            "+" -> MonkeyOperationOperator.ADD
            "*" -> MonkeyOperationOperator.MULTIPLY
            else -> throw Exception()
        }

        val (left, operator, right) = this.split(" ")
        return MonkeyOperation(
            left = left.toExpression(), operator = operator.toOperator(), right = right.toExpression()
        )
    }

    val (rawItems, rawOperation, rawTest, rawIfTrueTrowTo, rawIfFalseTrowTo) = this
    return Monkey(
        items = rawItems.toItems(),
        operation = rawOperation.toOperation(),
        test = MonkeyTest(rawTest.toInt()),
        ifTrueTrowTo = rawIfTrueTrowTo.toInt(),
        ifFalseTrowTo = rawIfFalseTrowTo.toInt()
    )
}

fun monkeyBusiness(inspectionsCountByMonkeyId: List<Int>): Long =
    inspectionsCountByMonkeyId.sorted().takeLast(2).map { it.toLong() }.reduce { acc, i -> acc * i }

fun main() {
    val lines = File("src/aoc_2022/inputs/11.txt").readLines()
    val rawMonkeys = lines.chunked(6)
    val monkeys = rawMonkeys.map { it.toMonkey() }.toMutableList()

    val inspectionsCountByMonkeyId = MutableList(monkeys.size) { 0 }

    val newValModulo = monkeys.map { it.test.divisibleBy }.reduce { acc, i -> acc * i }

    for (roundIdx in 1..10000) {
        for (tourIdx in 0 until monkeys.size) {
            val turnThrows = monkeys[tourIdx].turnThrows(newValModulo)
            inspectionsCountByMonkeyId[tourIdx] += turnThrows.size

            for ((item, toMonkeyIdx) in turnThrows) {
                monkeys[toMonkeyIdx] = monkeys[toMonkeyIdx].copy(items = monkeys[toMonkeyIdx].items.plus(item))
            }
            monkeys[tourIdx] = monkeys[tourIdx].copy(items = emptyList())
        }
    }

    println(
        "monkeyBusiness: ${
            monkeyBusiness(inspectionsCountByMonkeyId)
        }"
    )
}