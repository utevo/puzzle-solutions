package aoc_2022.d17

import java.io.File

data class Position(val y: Int, val x: Int) {
    operator fun plus(other: Position): Position = Position(y + other.y, x + other.x)
}

enum class JetMove {
    Left,
    Right
}

fun Position.afterJetMove(jetMove: JetMove): Position = when (jetMove) {
    JetMove.Left -> copy(x = x - 1)
    JetMove.Right -> copy(x = x + 1)
}

data class JetPattern(val moves: List<JetMove>)

typealias RockPiecePosition = Position
typealias RockPieces = Set<RockPiecePosition>
typealias MutableRockPieces = MutableSet<RockPiecePosition>

// shape is defined from bottom to up
data class FallingRockShape(val rockPieces: RockPieces) {
    companion object {
        private val t1 =
            FallingRockShape(
                setOf(
                    Position(0, 0),
                    Position(0, 1),
                    Position(0, 2),
                    Position(0, 3)
                )
            )
        private val t2 = FallingRockShape(
            setOf(
                Position(0, 0),
                Position(1, -1),
                Position(1, 0),
                Position(1, 1),
                Position(2, 0)
            )
        )
        private val t3 = FallingRockShape(
            setOf(
                Position(0, 0),
                Position(0, 1),
                Position(0, 2),
                Position(1, 2),
                Position(2, 2)
            )
        )
        private val t4 = FallingRockShape(
            setOf(
                Position(0, 0),
                Position(1, 0),
                Position(2, 0),
                Position(3, 0)
            )
        )
        private val t5 = FallingRockShape(
            setOf(
                Position(0, 0),
                Position(0, 1),
                Position(1, 0),
                Position(1, 1)
            )
        )
        val all = listOf(t1, t2, t3, t4, t5)
    }
}


typealias BasePosition = Position

data class FallingRock(val shape: FallingRockShape, val basePosition: BasePosition) {
    fun canFallDown(tunnel: Tunnel): Boolean {
        val afterFallDown = makeFallDown()
        return tunnel.isOkPlaced(afterFallDown)
    }

    fun tryMakeJetMove(jetMove: JetMove, tunnel: Tunnel): FallingRock {
        val afterJetMove = makeJetMove(jetMove)
        return if (tunnel.isOkPlaced(afterJetMove)) afterJetMove else this
    }

    fun makeFallDown(): FallingRock {
        return copy(basePosition = basePosition.copy(y = basePosition.y - 1))
    }

    val piecesPositions: Set<RockPiecePosition> by lazy {
        shape.rockPieces.map { it + basePosition }.toSet()
    }

    private fun makeJetMove(jetMove: JetMove): FallingRock {
        return copy(basePosition = basePosition.afterJetMove(jetMove))
    }
}

data class Tunnel(val rockPieces: MutableRockPieces, val width: Int = 7) {
    fun maxRockPieceY(): Int {
        return rockPieces.maxOfOrNull { it.y } ?: -1
    }

    fun isOkPlaced(fallingRock: FallingRock): Boolean {
        return isNotCollides(fallingRock) && isInTunnel(fallingRock)
    }

    private fun isNotCollides(fallingRock: FallingRock): Boolean {
        return fallingRock.piecesPositions.all(::isNotCollides)
    }

    private fun isNotCollides(rockPiecePosition: Position): Boolean {
        return rockPiecePosition !in rockPieces
    }

    private fun isInTunnel(fallingRock: FallingRock): Boolean {
        return fallingRock.piecesPositions.all(::isInTunnel)
    }

    private fun isInTunnel(rockPiecePosition: Position): Boolean {
        val xIsOk = rockPiecePosition.x in (0 until width)
        val yIsOk = rockPiecePosition.y >= 0
        return xIsOk && yIsOk
    }

    fun add(fallingRock: FallingRock) {
        rockPieces += fallingRock.piecesPositions
    }
}

fun String.toJetPattern(): JetPattern {
    val moves = this.map {
        when (it) {
            '<' -> JetMove.Left
            '>' -> JetMove.Right
            else -> throw IllegalArgumentException()
        }
    }
    return JetPattern(moves = moves)
}

fun <T> List<T>.asCyclicSequence(): Sequence<T> {
    val idxSequence = generateSequence(0) { (it + 1) % this.size }
    return idxSequence.map { this[it] }
}


fun makeBasePosition(shape: FallingRockShape, maxRockPieceY: Int): BasePosition {
    val shapeMinY = shape.rockPieces.minOf { it.y }
    val shapeMinX = shape.rockPieces.minOf { it.x }

    return BasePosition(y = maxRockPieceY + 4 - shapeMinY, x = 2 - shapeMinX)
}

typealias Last200Rows = Set<RockPiecePosition>


const val NUMBER_OF_ROWS = 200
fun Tunnel.last200Rows(): Last200Rows {
    val maxRockPieceY = maxRockPieceY()
    val last20Rows: MutableRockPieces = mutableSetOf()

    for (y in (maxRockPieceY - NUMBER_OF_ROWS + 1..maxRockPieceY)) {
        for (x in (0 until width)) {
            if (Position(y, x) in this.rockPieces) {
                last20Rows.add(Position(y - maxRockPieceY + NUMBER_OF_ROWS - 1, x))
            }
        }
    }
    return last20Rows
}

data class State(val last200Rows: Last200Rows, val lastRockShapeId: Long, val lastJetMoveId: Long)
data class PrevStateMetadata(val rockShapeIdx: Long, val maxRockPieceY: Int)

const val HOW_TALL_AFTER = 1000000000000L

fun main() {
    val lines = File("src/aoc_2022/inputs/17.txt").readLines()
    val jetPattern = lines[0].toJetPattern()
    val tunnel = Tunnel(rockPieces = mutableSetOf())

    val jetPatternIterator =
        jetPattern.moves.asCyclicSequence().iterator()

    val prevStates = mutableSetOf<State>()
    val metadataByPrevState = mutableMapOf<State, PrevStateMetadata>()
    var patternSkipHeight = 0L

    var rockShapeIdx = 0L
    var jetMoveIdx = -1L
    for (rockShape in FallingRockShape.all.asCyclicSequence()) {
        var fallingRock =
            FallingRock(
                shape = rockShape,
                basePosition = makeBasePosition(shape = rockShape, maxRockPieceY = tunnel.maxRockPieceY())
            )

        while (true) {
            val jetMove = jetPatternIterator.next()
            jetMoveIdx++
            fallingRock = fallingRock.tryMakeJetMove(jetMove = jetMove, tunnel = tunnel)
            if (!fallingRock.canFallDown(tunnel)) break
            fallingRock = fallingRock.makeFallDown()
        }

        tunnel.add(fallingRock)
        if (rockShapeIdx == HOW_TALL_AFTER) {
            break
        }

        if (patternSkipHeight == 0L) {
            val state = State(
                last200Rows = tunnel.last200Rows(),
                lastRockShapeId = rockShapeIdx % FallingRockShape.all.size,
                lastJetMoveId = jetMoveIdx % jetPattern.moves.size
            )
            if (state in prevStates) {
                // There's probably some off-by-one error in this scope.
                val prevStateMetadata = metadataByPrevState.getValue(state)
                val howMuchHeightIsMissing = HOW_TALL_AFTER - tunnel.maxRockPieceY()
                val numberOfRockShapesInPattern = rockShapeIdx - prevStateMetadata.rockShapeIdx
                val patternHeight =
                    tunnel.maxRockPieceY() - (metadataByPrevState[state]?.maxRockPieceY
                        ?: throw IllegalArgumentException())
                val numberOfPattern = howMuchHeightIsMissing.floorDiv(numberOfRockShapesInPattern)
                rockShapeIdx += numberOfPattern * numberOfRockShapesInPattern
                patternSkipHeight = numberOfPattern * patternHeight
            }

            prevStates.add(state)
            metadataByPrevState[state] =
                PrevStateMetadata(rockShapeIdx = rockShapeIdx, maxRockPieceY = tunnel.maxRockPieceY())
        }
        rockShapeIdx++
    }

    val result = tunnel.maxRockPieceY() + patternSkipHeight
    println(result)
}
