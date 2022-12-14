package aoc_2022.d13

import java.io.File

sealed class PacketItem : Comparable<PacketItem> {
    override fun compareTo(other: PacketItem): Int {
        return when (this) {
            is PacketList -> when (other) {
                is PacketList -> {
                    compareListToList(this, other)
                }

                is PacketInt -> this.compareTo(other.toPacketList())
            }

            is PacketInt -> when (other) {
                is PacketList -> this.toPacketList().compareTo(other)
                is PacketInt -> this.value.compareTo(other.value)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is PacketItem) {
            return this.compareTo(other) == 0
        }
        return super.equals(other)
    }

    companion object {
        fun compareListToList(some: PacketList, other: PacketList): Int {
            for ((someEle, otherEle) in some.items zip other.items) {
                if (someEle == otherEle) continue
                return someEle.compareTo(otherEle)
            }
            return some.items.size - other.items.size
        }
    }
}

class PacketList(val items: List<PacketItem>) : PacketItem()
class PacketInt(val value: Int) : PacketItem() {
    fun toPacketList(): PacketList {
        return PacketList(listOf(this))
    }
}

fun List<PacketToken>.toPacketList(): PacketList {
    val items = mutableListOf<PacketItem>()
    val tokens = this.toMutableList()
    if (tokens.first() != OpeningBracketToken || tokens.last() != ClosingBracketToken) throw Exception("Impossible state")
    tokens.removeFirst()
    tokens.removeLast()

    var i = 0
    while (i < tokens.size) {
        when (val currToken = tokens[i]) {
            is IntToken -> {
                items.add(PacketInt(currToken.number))
                i++
            }

            OpeningBracketToken -> {
                var diffOpeningToClosing = 0
                val innerTokens = mutableListOf<PacketToken>()
                do {
                    val innerCurrToken = tokens[i]
                    innerTokens.add(innerCurrToken)
                    when (innerCurrToken) {
                        is OpeningBracketToken -> diffOpeningToClosing += 1
                        is ClosingBracketToken -> diffOpeningToClosing -= 1
                        else -> Unit
                    }
                    i++
                } while (diffOpeningToClosing > 0)
                items.add(innerTokens.toPacketList())
            }

            is ClosingBracketToken -> throw Exception("Impossible state")
        }
    }

    return PacketList(items)
}

sealed class PacketToken
object OpeningBracketToken : PacketToken()
object ClosingBracketToken : PacketToken()
data class IntToken(val number: Int) : PacketToken()

fun String.toPacketTokes(): List<PacketToken> {
    val packetTokens = mutableListOf<PacketToken>()

    var i = 0
    while (i < this.length) {

        when (this[i]) {
            '[' -> {
                packetTokens.add(OpeningBracketToken)
                i++
            }

            ']' -> {
                packetTokens.add(ClosingBracketToken)
                i++
            }

            in '0'..'9' -> {
                var numberAsString = ""
                while (i < this.length) {
                    if (this[i] !in '0'..'9') break
                    numberAsString += this[i]
                    i++
                }
                packetTokens.add(IntToken(numberAsString.toInt()))
            }

            else -> {
                i++
            }
        }

    }
    return packetTokens
}

fun main() {
    val lines = File("src/aoc_2022/inputs/13.txt").readLines()
    val rawPacketPairs = lines.chunked(3).map { Pair(it[0], it[1]) }
    val packetPairs = rawPacketPairs.map { (firstRawPacket, secondRawPacket) ->
        Pair(
            firstRawPacket.toPacketTokes().toPacketList(),
            secondRawPacket.toPacketTokes().toPacketList()
        )
    }

    val result = packetPairs.mapIndexedNotNull() { index, value ->
        if (value.first < value.second) Pair(
            index + 1,
            value
        ) else null
    }

    println(result.sumOf { it.first })

    val firstDivider = PacketList(listOf(PacketList(listOf(PacketInt(2)))))
    val secondDivider = PacketList(listOf(PacketList(listOf(PacketInt(6)))))

    val newItems = packetPairs.flatMap { it -> listOf(it.first, it.second) }.plus(firstDivider).plus(secondDivider)
    val newItemsSorted = newItems.sorted()

    val firstDividerIndex = newItemsSorted.indexOf(firstDivider) + 1
    val secondDividerIndex = newItemsSorted.indexOf(secondDivider) + 1

    println(firstDividerIndex * secondDividerIndex)
}
