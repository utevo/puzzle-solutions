package aoc_2022.d23

import java.io.File


data class Position(val y: Int, val x: Int)

typealias ElfPosition = Position

data class Elves(val positions: Set<ElfPosition>) {
    val minY by lazy { positions.minOf { it.y } }
    val maxY by lazy { positions.maxOf { it.y } }
    val minX by lazy { positions.minOf { it.x } }
    val maxX by lazy { positions.maxOf { it.x } }

    fun afterMovements(movements: CorrectMovements): Elves {
        val newPosition = positions.toMutableSet()

        for (movement in movements.movements) {
            newPosition.remove(movement.movement.from)
            newPosition.add(movement.movement.to)
        }

        return Elves(newPosition)
    }

    operator fun minus(position: ElfPosition): Elves {
        return copy(positions = positions - position)
    }

    override fun toString(): String {
        var asString = ""
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                asString += if (Position(y, x) in positions) '#' else '.'
            }
            asString += '\n'
        }

        return asString
    }
}

fun List<String>.toElves(): Elves {
    val elfPositions = mutableSetOf<ElfPosition>()
    for ((yIdx, list) in this.withIndex()) {
        for ((xIdx, char) in list.withIndex()) {
            if (char == '#') elfPositions.add(Position(y = yIdx, x = xIdx))
        }
    }

    return Elves(positions = elfPositions)
}

fun Position.neighborPositions(): Set<Position> {
    return setOf(n(), e(), s(), w(), ne(), nw(), se(), sw())
}

fun Position.n() = copy(y = y - 1)
fun Position.e() = copy(x = x + 1)
fun Position.s() = copy(y = y + 1)
fun Position.w() = copy(x = x - 1)
fun Position.ne() = this.n().e()
fun Position.nw() = this.n().w()
fun Position.se() = this.s().e()
fun Position.sw() = this.s().w()

data class ElfMovement(val from: ElfPosition, val to: ElfPosition) {
    fun toPlannedMovement(): PlannedMovement {
        return PlannedMovement(this)
    }

    companion object {
        fun stayed(elfPosition: ElfPosition): ElfMovement {
            return ElfMovement(from = elfPosition, to = elfPosition)
        }
    }
}

data class PlannedMovement(val movement: ElfMovement)
data class PlanedMovements(val movements: Set<PlannedMovement>) {
    fun toCorrectMovements(): CorrectMovements {
        val countByTo = movements.groupingBy { it.movement.to }.eachCount().withDefault { 0 }

        val rawCorrectMovement = movements.filter { countByTo.getValue(it.movement.to) == 1 }
        return CorrectMovements(rawCorrectMovement.map { CorrectMovement(it.movement) }.toSet())
    }
}

data class CorrectMovement(val movement: ElfMovement)

data class CorrectMovements(val movements: Set<CorrectMovement>)

data class MoveStrategy(
    val name: String,
    val `if`: (elfPosition: ElfPosition, otherElves: Elves) -> Boolean,
    val move: (currPosition: ElfPosition) -> ElfPosition
)

val stayStrategy = MoveStrategy(
    name = "stay",
    `if` = { elfPosition, otherElves ->
        otherElves.positions.intersect(
            elfPosition.neighborPositions()
        ).isEmpty()
    },
    move = { it }
)

val goNorthStrategy = MoveStrategy(
    name = "north",
    `if` = { elfPosition, otherElves ->
        otherElves.positions.intersect(
            setOf(
                elfPosition.n(),
                elfPosition.ne(),
                elfPosition.nw(),
            )
        ).isEmpty()
    },
    move = { it.n() }
)

val goSouthStrategy = MoveStrategy(
    name = "south",
    `if` = { elfPosition, otherElves ->
        otherElves.positions.intersect(
            setOf(
                elfPosition.s(),
                elfPosition.se(),
                elfPosition.sw(),
            )
        ).isEmpty()
    },
    move = { it.s() }
)

val goWestStrategy = MoveStrategy(
    name = "west",
    `if` = { elfPosition, otherElves ->
        otherElves.positions.intersect(
            setOf(
                elfPosition.w(),
                elfPosition.nw(),
                elfPosition.sw(),
            )
        ).isEmpty()
    },
    move = { it.w() }
)

val goEastStrategy = MoveStrategy(
    name = "east",
    `if` = { elfPosition, otherElves ->
        otherElves.positions.intersect(
            setOf(
                elfPosition.e(),
                elfPosition.ne(),
                elfPosition.se(),
            )
        ).isEmpty()
    },
    move = { it.e() }
)

data class MoveStrategies(val strategies: List<MoveStrategy>) {
    fun next(): MoveStrategies {
        val nextStrategies = strategies.toMutableList()
        nextStrategies += nextStrategies.removeAt(1)
        return MoveStrategies(strategies = nextStrategies)
    }

    fun apply(elfPosition: ElfPosition, otherElves: Elves): PlannedMovement {
        val firstGoodStrategy = strategies.firstOrNull { it.`if`(elfPosition, otherElves) }
            ?: return ElfMovement.stayed(elfPosition).toPlannedMovement()
        return ElfMovement(from = elfPosition, to = firstGoodStrategy.move(elfPosition)).toPlannedMovement()
    }
}

val initMoveStrategies =
    MoveStrategies(listOf(stayStrategy, goNorthStrategy, goSouthStrategy, goWestStrategy, goEastStrategy))


fun Elves.toResult(): Int {
    val squareSize = (maxY - minY + 1) * (maxX - minX + 1)

    return squareSize - positions.size
}

const val NUMBER_OF_ROUNDS = 10

fun main() {
    val lines = File("src/aoc_2022/inputs/23.txt").readLines()
    var elves = lines.toElves()

    var currMoveStrategies = initMoveStrategies.copy()

    repeat(NUMBER_OF_ROUNDS) {
        val plannedMovements: PlanedMovements =
            elves.positions.map { currMoveStrategies.apply(elfPosition = it, otherElves = elves - it) }.toSet()
                .let { PlanedMovements(it) }

        val correctMovements: CorrectMovements = plannedMovements.toCorrectMovements()
        elves = elves.afterMovements(correctMovements)
        currMoveStrategies = currMoveStrategies.next()
    }

    println("result: ${elves.toResult()}")
}