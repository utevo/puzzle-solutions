package aoc_2022.d21

import java.io.File


typealias MonkeyName = String

data class Monkey(val name: String, val job: MonkeyJob)

fun String.toMonkey(): Monkey {
    val (name, rawJob) = this.split(": ")

    return Monkey(name, rawJob.toMonkeyJob())
}

sealed class MonkeyJob {
    data class YellNumber(val number: Long) : MonkeyJob()
    data class YellOperation(
        val firstMonkeyName: MonkeyName,
        val operator: YellOperator,
        val secondMonkeyName: MonkeyName
    ) : MonkeyJob()
}

enum class YellOperator {
    Add,
    Subtract,
    Multiple,
    Divide;

    fun calc(some: Long, other: Long): Long {
        return when (this) {
            Add -> some + other
            Subtract -> some - other
            Multiple -> some * other
            Divide -> some / other
        }
    }
}

fun String.toYellOperator(): YellOperator {
    return when (this) {
        "+" -> YellOperator.Add
        "-" -> YellOperator.Subtract
        "*" -> YellOperator.Multiple
        "/" -> YellOperator.Divide
        else -> throw IllegalArgumentException()
    }
}

class Monkeys(monkeyList: List<Monkey>) {
    val monkeyByName = monkeyList.associateBy { it.name }.toMutableMap()

    fun eval(monkeyName: MonkeyName): Long? {
        val monkeyJob = monkeyByName[monkeyName]?.job ?: return null
        return when (monkeyJob) {
            is MonkeyJob.YellNumber -> monkeyJob.number
            is MonkeyJob.YellOperation -> monkeyJob.operator.calc(
                eval(monkeyJob.firstMonkeyName) ?: return null,
                eval(monkeyJob.secondMonkeyName) ?: return null
            )
        }
    }
}

fun String.toMonkeyJob(): MonkeyJob {
    val number = this.toLongOrNull()
    if (number != null) {
        return MonkeyJob.YellNumber(number)
    }
    val (firstMonkeyName, rawOperator, secondMonkeyName) = this.split(' ')
    return MonkeyJob.YellOperation(firstMonkeyName, rawOperator.toYellOperator(), secondMonkeyName)
}

// Found by manual binary search 👅
const val FROM = 3582317900000L
const val HOW_MANY = 1000000L
fun main() {
    val lines = File("src/aoc_2022/inputs/21.txt").readLines()
    val monkeyList = lines.map { it.toMonkey() }

    val monkeys = Monkeys(monkeyList)

    val root = monkeys.monkeyByName.getValue("root")
    val rootOperation = root.job as MonkeyJob.YellOperation

    for (humnNumber in FROM..FROM + HOW_MANY) {
        monkeys.monkeyByName["humn"] = Monkey("humn", MonkeyJob.YellNumber(humnNumber))

        val firstValue = monkeys.eval(rootOperation.firstMonkeyName) as Long
        val secondValue = monkeys.eval(rootOperation.secondMonkeyName) as Long
        if (humnNumber % 10000L == 0L) {
            println("$firstValue vs $secondValue")
            println(firstValue - secondValue)
        }

        if (firstValue == secondValue) {
            println("humnNumber: $humnNumber")
            return
        }
    }

    println("Not found :(")
}