import Foundation

let examplePath = "example.txt"
let inputPath = "input.txt"

let bag: Bag = [.red: 12, .green: 13, .blue: 14]

main()
func main() {
  print("a", try! a(inputPath))
  print("b", try! b(inputPath))
}

func a(_ filePath: String) throws -> Int {
  let line_s = try read_lines(filePath)
  let game_s = try line_s.map { try GameParser.from(string: $0) }
  let possible_game_s = game_s.filter { game in
    game.is_possible(withBag: bag)
  }

  let result = possible_game_s.reduce(0) { sum, possible_game in sum + possible_game.id }
  return result
}

func b(_ filePath: String) throws -> Int {
  let line_s = try read_lines(filePath)
  let game_s = try line_s.map { try GameParser.from(string: $0) }
  let minimalBag_s = game_s.map { $0.minimalBag() }

  let result = minimalBag_s.reduce(0) { sum, minimalBag in sum + minimalBag.power() }
  return result
}

struct Game {
  let id: Id
  let hands: [Hand]

  typealias Id = Int
  typealias Hand = [Color: Int]

  func is_possible(withBag bag: Bag) -> Bool {
    return hands.allSatisfy { $0.is_possible(withBag: bag) }
  }

  func minimalBag() -> Bag {
    var result: Bag = [.red: 0, .green: 0, .blue: 0]

    for (color, number) in hands.flatMap({ $0 }) {
      result[color] = max(result[color]!, number)
    }

    return result
  }
}

extension Game.Hand {
  func is_possible(withBag bag: Bag) -> Bool {
    return self.allSatisfy { (color, quantity) in
      bag[color, default: 0] >= quantity
    }
  }
}

enum Color: CaseIterable {
  case red
  case green
  case blue
}

typealias Bag = [Color: Int]

extension Bag {
  func power() -> Int {
    return Color.allCases.reduce(1) { sum, color in
      sum * self[color, default: 0]
    }
  }
}

typealias ColorAndNumber = (Game.Hand.Key, Game.Hand.Value)

enum GameParser {
  static func from(string: String) throws -> Game {
    let result = string.split(separator: ":", maxSplits: 1)

    let raw_id = result[0]
    let raw_hand_s = result[1]

    let id = try parse_id(raw_id)
    let hands = try parse_hand_s(raw_hand_s)

    return Game(id: id, hands: hands)
  }

  private static func parse_id(_ raw_id: any StringProtocol) throws -> Game.Id {
    let splitted = raw_id.split(separator: " ")
    return Int(splitted[1])!
  }

  private static func parse_hand_s(_ raw_hand_s: any StringProtocol) throws -> [Game.Hand] {
    return try raw_hand_s.split(separator: ";").map(parse_hand)
  }

  private static func parse_hand(_ raw_hand: any StringProtocol) throws -> Game.Hand {
    let raw_colorAndNumber_s = raw_hand.split(separator: ",")
    let colorAndNumber_s = try raw_colorAndNumber_s.map(parse_colorAndNumber)

    var hand: Game.Hand = [:]
    for (color, number) in colorAndNumber_s {
      hand[color] = number
    }
    return hand
  }

  private static func parse_colorAndNumber(_ raw_colorAndNumber: any StringProtocol) throws
    -> ColorAndNumber
  {
    let splitted = raw_colorAndNumber.split(separator: " ")
    let raw_number = splitted[0]
    let raw_color = splitted[1]

    let color = try parse_color(raw_color)
    let number = Int(raw_number)!

    return (color, number)
  }

  private static func parse_color(_ raw_color: any StringProtocol) throws -> Color {
    return switch String(raw_color) {
    case "red": .red
    case "green": .green
    case "blue": .blue
    default: throw ParsingError(message: "unexpected color: \(raw_color)")
    }
  }
}

struct ParsingError: Error {
  let message: String
}

func read_lines(_ filePath: String) throws -> [String] {
  let slices = try String(contentsOfFile: filePath).split(separator: "\n")
  return slices.map { String($0) }
}
