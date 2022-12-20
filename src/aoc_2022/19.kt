package aoc_2022.d19

import java.io.File
import kotlin.math.max

data class Resources(val ore: Int = 0, val clay: Int = 0, val obsidian: Int = 0, val geode: Int = 0) {
    operator fun plus(other: Resources): Resources {
        return copy(
            ore = ore + other.ore,
            clay = clay + other.clay,
            obsidian = obsidian + other.obsidian,
            geode = geode + other.geode
        )
    }

    operator fun minus(other: Resources): Resources {
        return copy(
            ore = ore - other.ore,
            clay = clay - other.clay,
            obsidian = obsidian - other.obsidian,
            geode = geode - other.geode
        )
    }
}

enum class RobotKind {
    Geode,
    Obsidian,
    Ore,
    Clay,
}

data class Blueprint(
    val oreRobotCost: Resources,
    val clayRobotCost: Resources,
    val obsidianRobotCost: Resources,
    val geodeRobotCost: Resources
) {
    fun cost(robotKind: RobotKind): Resources {
        return when (robotKind) {
            RobotKind.Ore -> oreRobotCost
            RobotKind.Clay -> clayRobotCost
            RobotKind.Obsidian -> obsidianRobotCost
            RobotKind.Geode -> geodeRobotCost
        }
    }
}


fun String.toBlueprint(): Blueprint {
    fun String.toOreRobotCost(): Resources {
        val (rawOreCount) = this.split(' ')
        return Resources(ore = rawOreCount.toInt())
    }

    fun String.toClayRobotCost(): Resources {
        val (rawOreCount) = this.split(' ')
        return Resources(ore = rawOreCount.toInt())
    }

    fun String.toObsidianRobotCost(): Resources {
        val (rawOreCount, _, _, rawClayCount) = this.split(' ')
        return Resources(ore = rawOreCount.toInt(), clay = rawClayCount.toInt())
    }

    fun String.toGeodeRobotCost(): Resources {
        val (rawOreCount, _, _, rawObsidianCount) = this.split(' ')
        return Resources(ore = rawOreCount.toInt(), obsidian = rawObsidianCount.toInt())
    }

    val (rawOreRobotCost, rawClayRobotCost, rawObsidianRobotCost, rawGeodeRobotCost) = this.split(',')
    return Blueprint(
        rawOreRobotCost.toOreRobotCost(),
        rawClayRobotCost.toClayRobotCost(),
        rawObsidianRobotCost.toObsidianRobotCost(),
        rawGeodeRobotCost.toGeodeRobotCost()
    )
}

data class RobotsState(val ore: Int = 0, val clay: Int = 0, val obsidian: Int = 0, val geode: Int = 0) {
    operator fun plus(robotKind: RobotKind): RobotsState {
        return when (robotKind) {
            RobotKind.Ore -> copy(ore = ore + 1)
            RobotKind.Clay -> copy(clay = clay + 1)
            RobotKind.Obsidian -> copy(obsidian = obsidian + 1)
            RobotKind.Geode -> copy(geode = geode + 1)
        }
    }

    operator fun minus(robotKind: RobotKind): RobotsState {
        return when (robotKind) {
            RobotKind.Ore -> copy(ore = ore - 1)
            RobotKind.Clay -> copy(clay = clay - 1)
            RobotKind.Obsidian -> copy(obsidian = obsidian - 1)
            RobotKind.Geode -> copy(geode = geode - 1)
        }
    }

    fun createdResourcesAfterMinute(): Resources {
        return Resources(ore = ore, clay = clay, obsidian = obsidian, geode = geode)
    }
}


data class State(
    val resources: Resources = Resources(),
    val robots: RobotsState = RobotsState(ore = 1),
    val leftMinutes: Int = 24
) {
    fun afterWaitingMinute(): State {
        return copy(
            resources = resources + robots.createdResourcesAfterMinute(),
            leftMinutes = leftMinutes - 1
        )
    }

    fun afterBuildingRobot(robotKind: RobotKind, blueprint: Blueprint): State {
        return afterIncurringCosts(blueprint.cost(robotKind)).afterWaitingMinute().copy(robots = robots + robotKind)
    }

    fun canBuildRobot(robotKind: RobotKind, blueprint: Blueprint): Boolean {
        val cost = blueprint.cost(robotKind)
        return canIncurCost(cost)
    }

    private fun afterIncurringCosts(cost: Resources): State {
        return copy(resources = resources - cost)
    }

    private fun canIncurCost(cost: Resources): Boolean {
        return cost.ore <= resources.ore && cost.clay <= resources.clay && cost.obsidian <= resources.obsidian && cost.geode <= resources.geode
    }

    fun geodeUpperLimit(): Int {
        val sureGeodeCount = resources.geode + leftMinutes * robots.geode
        val theoreticalGeodeGainCount = (leftMinutes - 1) * (leftMinutes) / 2
        return sureGeodeCount + theoreticalGeodeGainCount
    }
}

// need few minutes to calculate result
fun main() {
    val lines = File("src/aoc_2022/inputs/19.txt").readLines()
    val blueprints = lines.map { it.toBlueprint() }

    fun maxGeodeCount(blueprint: Blueprint): Int {
        var maxGeodeCount = 0

        fun backtrack(state: State) {
            maxGeodeCount = max(maxGeodeCount, state.resources.geode)
            if (state.leftMinutes == 0) {
                return
            }

            if (state.geodeUpperLimit() <= maxGeodeCount) {
                return
            }

            for (robotKind in RobotKind.values()) {
                if (state.canBuildRobot(robotKind, blueprint)) {
                    backtrack(state.afterBuildingRobot(robotKind, blueprint))
                }
            }
            backtrack(state.afterWaitingMinute())
        }

        backtrack(state = State())
        return maxGeodeCount
    }

    val maxGeodeCounts = blueprints.map {
        val maxGeodeCount = maxGeodeCount(it)
        println(maxGeodeCount)
        maxGeodeCount
    }

    val result = maxGeodeCounts.withIndex().sumOf { it.value * (it.index + 1) }

    println("result: $result")
}