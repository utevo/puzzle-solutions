package aoc_2022.d19

import java.io.File
import kotlin.math.max
import kotlin.math.min

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
    val leftMinutes: Int = 32
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

    private fun obsidianUpperLimit(): Int {
        val sureObsidianCount = resources.obsidian + leftMinutes * robots.obsidian
        val theoreticalObsidianGainCount = (leftMinutes - 1) * (leftMinutes) / 2

        return sureObsidianCount + theoreticalObsidianGainCount
    }

    fun geodeUpperLimit(blueprint: Blueprint): Int {
        val sureGeodeCount = resources.geode + leftMinutes * robots.geode

        val theoreticalMaxGainedGeodeRobot =
            min(leftMinutes - 1, obsidianUpperLimit().floorDiv(blueprint.geodeRobotCost.obsidian))

        val theoreticalGeodeGainCount =
            theoreticalMaxGainedGeodeRobot * (theoreticalMaxGainedGeodeRobot - 1) / 2 + (leftMinutes - theoreticalMaxGainedGeodeRobot) * theoreticalMaxGainedGeodeRobot
        return sureGeodeCount + theoreticalGeodeGainCount
    }
}

//fun State.isNotBetterAnyAnyway(other: State): Boolean {
//    return leftMinutes <= other.leftMinutes && robots.isNotBetterAnyAnyway(other.robots) && resources.isNotBetterAnyAnyway(
//        other.resources
//    )
//}
//
//fun RobotsState.isNotBetterAnyAnyway(other: RobotsState): Boolean {
//    return ore <= other.ore && clay <= other.clay && obsidian <= other.obsidian && geode <= other.geode
//}
//
//fun Resources.isNotBetterAnyAnyway(other: Resources): Boolean {
//    return ore <= other.ore && clay <= other.clay && obsidian <= other.obsidian && geode <= other.geode
//}

// need some time to calculate result
fun main() {
    val lines = File("src/aoc_2022/inputs/19.txt").readLines()
    val blueprints = lines.map { it.toBlueprint() }.take(3)

    fun maxGeodeCount(blueprint: Blueprint): Int {
        var maxGeodeCount = 0

        fun backtrack(state: State) {
            maxGeodeCount = max(maxGeodeCount, state.resources.geode)
            if (state.leftMinutes == 0) {
                return
            }

            if (state.geodeUpperLimit(blueprint) <= maxGeodeCount) {
                return
            }
            if (state.leftMinutes != 1) {
                for (robotKind in RobotKind.values()) {
                    if (state.canBuildRobot(robotKind, blueprint)) {
                        backtrack(state.afterBuildingRobot(robotKind, blueprint))
                    }
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

    val result = maxGeodeCounts.reduce { acc, it -> acc * it }

    println("result: $result")
}