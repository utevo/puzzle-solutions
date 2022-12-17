package aoc_2022.d16

import java.io.File
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap
import kotlin.collections.HashSet

typealias RoomName = String

data class Room(val name: RoomName, val flowRate: Int, val neighbors: List<RoomName>)

fun String.toRoom(): Room {
    fun String.toNeighbors(): List<RoomName> {
        return split(",").map { it.trim() }
    }

    val (rawRoomName, rawFlowRate, rawNeighbors) = this.split(";")
    return Room(rawRoomName.trim(), rawFlowRate.trim().toInt(), rawNeighbors.toNeighbors())
}


const val START_ROOM_NAME: RoomName = "AA"

class Rooms(roomList: List<Room>) {
    val roomByName by lazy {
        roomList.associateBy { it.name }
    }
    val usefulRooms by lazy { setOf(roomByName.getValue(START_ROOM_NAME)).plus(roomList.filter { it.flowRate != 0 }) }
    private val distancesData: Map<RoomName, Map<RoomName, Int>>

    init {
        fun distancesFrom(roomName: RoomName): Map<RoomName, Int> {
            val distanceFrom = HashMap<RoomName, Int>().withDefault { Int.MAX_VALUE }

            val explored = HashSet<RoomName>()
            val queue = ArrayDeque<RoomName>()
            distanceFrom[roomName] = 0
            explored.add(roomName)
            queue.add(roomName)

            while (!queue.isEmpty()) {
                val currRoomName = queue.removeFirst()
                for (neighborName in roomByName.getValue(currRoomName).neighbors) {
                    if (neighborName !in explored) {
                        explored.add(neighborName)
                        distanceFrom[neighborName] = distanceFrom.getValue(currRoomName) + 1
                        queue.add(neighborName)
                    }
                }
            }
            return distanceFrom
        }

        distancesData = roomList.associate { Pair(it.name, distancesFrom(it.name)) }
    }

    fun distance(from: RoomName, to: RoomName): Int {
        return distancesData.getValue(from).getValue(to)
    }
}

data class State(
    val rooms: Rooms,
    val currRoom: Room,
    val leftMinutes: Int = 26,
    val finalPressure: Int = 0,
) {

    fun shouldTryGoingAndOpeningValue(toRoom: Room, openedRooms: Set<Room>): Boolean {
        val thisIsPossible = toRoom !in openedRooms
        val thisMakeSense = toRoom.flowRate != 0
        val weHaveTimeForThis = rooms.distance(currRoom.name, toRoom.name) + 2 <= leftMinutes

        return thisIsPossible && thisMakeSense && weHaveTimeForThis
    }

    fun afterGoingAndOpeningValue(toRoom: Room): State {
        val distance = rooms.distance(currRoom.name, toRoom.name)
        val neededTime = distance + 1
        val gainedPressure = (leftMinutes - neededTime) * toRoom.flowRate
        return this.copy(
            currRoom = toRoom,
            finalPressure = finalPressure + gainedPressure,
            leftMinutes = leftMinutes - neededTime
        )
    }
}

fun limitOfImprovement(yourState: State, elephantState: State, openedRooms: Set<Room>): Int {
    var limitOfImprovement = 0
    val notOpenedRoomsSorted =
        (yourState.rooms.usefulRooms - openedRooms).sortedBy { it.flowRate }.toMutableList()

    val yourNearestRoom = notOpenedRoomsSorted.minOf { yourState.rooms.distance(yourState.currRoom.name, it.name) }
    val elephantNearestRoom =
        notOpenedRoomsSorted.minOf { yourState.rooms.distance(elephantState.currRoom.name, it.name) }
    var yourLeftMinutes = yourState.leftMinutes - yourNearestRoom + 1
    var elephantLeftMinutes = elephantState.leftMinutes - elephantNearestRoom + 1

    while ((yourLeftMinutes >= 2 || elephantLeftMinutes >= 2) && notOpenedRoomsSorted.isNotEmpty()) {
        if (yourLeftMinutes > elephantLeftMinutes) {
            yourLeftMinutes -= 2
            limitOfImprovement += yourLeftMinutes * notOpenedRoomsSorted.removeLast().flowRate
        } else {
            elephantLeftMinutes -= 2
            limitOfImprovement += elephantLeftMinutes * notOpenedRoomsSorted.removeLast().flowRate
        }
    }
    return limitOfImprovement
}

fun main() {
    val lines = File("src/aoc_2022/inputs/16.txt").readLines()
    val roomList = lines.map { it.toRoom() }
    val rooms = Rooms(roomList)

    var maxPressure = 0
    fun backtrack(yourState: State, elephantState: State, openedRooms: MutableSet<Room>) {
        val currFinalPressure = yourState.finalPressure + elephantState.finalPressure
        if (maxPressure < currFinalPressure) {
            maxPressure = currFinalPressure
            println("---")
            println(currFinalPressure)
        } else {
            if (currFinalPressure + limitOfImprovement(yourState, elephantState, openedRooms) < maxPressure) {
                return
            }
        }

        for (room in (rooms.usefulRooms - openedRooms)) {
            if (yourState.shouldTryGoingAndOpeningValue(room, openedRooms)) {
                openedRooms.add(room)
                backtrack(yourState.afterGoingAndOpeningValue(toRoom = room), elephantState, openedRooms)
                openedRooms.remove(room)
            }

            if (elephantState.shouldTryGoingAndOpeningValue(room, openedRooms)) {
                openedRooms.add(room)
                backtrack(yourState, elephantState.afterGoingAndOpeningValue(toRoom = room), openedRooms)
                openedRooms.remove(room)
            }
        }
    }

    backtrack(
        yourState = State(
            rooms = rooms,
            currRoom = rooms.roomByName.getValue(START_ROOM_NAME),
        ),
        elephantState = State(
            rooms = rooms,
            currRoom = rooms.roomByName.getValue(START_ROOM_NAME),
        ),
        mutableSetOf()
    )

    println("maxPressure $maxPressure")
}