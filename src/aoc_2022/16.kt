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
    val leftMinutes: Int = 30,
    val openedRooms: Set<Room> = setOf(),
    val finalPressure: Int = 0,
) {

    fun shouldTryGoingAndOpeningValue(toRoom: Room): Boolean {
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
            openedRooms = openedRooms.plus(toRoom),
            finalPressure = finalPressure + gainedPressure,
            leftMinutes = leftMinutes - neededTime
        )
    }
}

fun main() {
    val lines = File("src/aoc_2022/inputs/16.txt").readLines()
    val roomList = lines.map { it.toRoom() }
    val rooms = Rooms(roomList)

    var maxPressure = 0

    fun backtrack(state: State) {
        if (maxPressure < state.finalPressure) {
            maxPressure = state.finalPressure
        }
        val roomsToChock = rooms.usefulRooms - state.openedRooms
        for (room in roomsToChock) {
            if (state.shouldTryGoingAndOpeningValue(room)) {
                backtrack(state.afterGoingAndOpeningValue(toRoom = room))
            }
        }
    }

    backtrack(State(rooms = rooms, currRoom = rooms.roomByName.getValue(START_ROOM_NAME)))

    println(maxPressure)
}