package aoc_2022.d23

import java.io.File


data class Position(val y: Int, val x: Int)

typealias ElfPosition = Position

data class Elves(val positions: MutableSet<ElfPosition>) {
    fun minY() = positions.minOf { it.y }
    fun maxY() = positions.maxOf { it.y }
    fun minX() = positions.minOf { it.x }
    fun maxX() = positions.maxOf { it.x }

    fun afterMovements(movements: CorrectMovements): Elves {
        val newPosition = positions.toMutableSet()

        for (movement in movements.movements) {
            newPosition.remove(movement.movement.from)
            newPosition.add(movement.movement.to)
        }

        return Elves(newPosition)
    }

    override fun toString(): String {
        var asString = ""
        for (y in minY()..maxY()) {
            for (x in minX()..maxX()) {
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

data class CorrectMovements(val movements: Set<CorrectMovement>) {
    fun noElfMoved(): Boolean {
        return movements.all { it.movement.from == it.movement.to }
    }
}

data class MoveStrategy(
    val name: String,
    val `if`: (elfPosition: ElfPosition, otherElves: Elves) -> Boolean,
    val move: (currPosition: ElfPosition) -> ElfPosition
)

val stayStrategy = MoveStrategy(
    name = "stay",
    `if` = { elfPosition, otherElves ->
        elfPosition.neighborPositions().all { it !in otherElves.positions }
    },
    move = { it }
)

val goNorthStrategy = MoveStrategy(
    name = "north",
    `if` = { elfPosition, otherElves ->
        setOf(
            elfPosition.n(),
            elfPosition.ne(),
            elfPosition.nw(),
        ).all { it !in otherElves.positions }
    },
    move = { it.n() }
)

val goSouthStrategy = MoveStrategy(
    name = "south",
    `if` = { elfPosition, otherElves ->
        setOf(
            elfPosition.s(),
            elfPosition.se(),
            elfPosition.sw(),
        ).all { it !in otherElves.positions }
    },
    move = { it.s() }
)

val goWestStrategy = MoveStrategy(
    name = "west",
    `if` = { elfPosition, otherElves ->
        setOf(
            elfPosition.w(),
            elfPosition.nw(),
            elfPosition.sw(),
        ).all { it !in otherElves.positions }
    },
    move = { it.w() }
)

val goEastStrategy = MoveStrategy(
    name = "east",
    `if` = { elfPosition, otherElves ->
        setOf(
            elfPosition.e(),
            elfPosition.ne(),
            elfPosition.se(),
        ).all { it !in otherElves.positions }
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


fun main() {
    val lines = File("src/aoc_2022/inputs/23.txt").readLines()
    var elves = lines.toElves()

    var currMoveStrategies = initMoveStrategies.copy()

    var i = 1
    do {
        val plannedMovements: PlanedMovements =
            elves.positions.map { elfPosition ->
                currMoveStrategies.apply(
                    elfPosition = elfPosition,
                    otherElves = elves
                )
            }.toSet()
                .let { positions -> PlanedMovements(positions) }

        val correctMovements: CorrectMovements = plannedMovements.toCorrectMovements()
        elves = elves.afterMovements(correctMovements)
        currMoveStrategies = currMoveStrategies.next()
        i++
    } while (!correctMovements.noElfMoved())

    println("result: $i")
}