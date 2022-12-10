package aoc_2022

import java.io.File

data class Registers(val x: Int = 1)

sealed interface Instruction
object NoopInstruction : Instruction
class AddxInstruction(val value: Int) : Instruction


data class RegistersHistory(val registersAfterCycleIdx: List<Registers> = listOf(Registers())) {
    companion object {
        fun fromInstructions(instructions: List<Instruction>): RegistersHistory {
            val registersAfterCycleIdx = mutableListOf(Registers())

            for (instruction in instructions) {
                when (instruction) {
                    NoopInstruction -> {
                        registersAfterCycleIdx.add(registersAfterCycleIdx.last().copy())
                    }

                    is AddxInstruction -> {
                        registersAfterCycleIdx.add(registersAfterCycleIdx.last().copy())
                        registersAfterCycleIdx.add(
                            registersAfterCycleIdx.last().let { it.copy(x = it.x + instruction.value) })
                    }
                }
            }
            return RegistersHistory(registersAfterCycleIdx)
        }
    }
}

fun String.toInstruction(): Instruction? {
    val splited = this.split(" ")
    val instructionName = splited.first()
    val rest = splited.drop(1)

    return when (instructionName) {
        "noop" -> NoopInstruction
        "addx" -> rest.firstOrNull()?.toIntOrNull()?.let { AddxInstruction(it) }
        else -> null
    }
}

fun main() {
    val rawInstruction = File("src/aoc_2022/inputs/10.txt").readLines()

    val instructions = rawInstruction.mapNotNull { it.toInstruction() }
    val registersHistory = RegistersHistory.fromInstructions(instructions)

    var result = 0

    for (cycleIdx in 20 until registersHistory.registersAfterCycleIdx.size step 40) {
        val xValue = registersHistory.registersAfterCycleIdx[cycleIdx - 1].x
        result += cycleIdx * xValue
    }
    println(result)
}