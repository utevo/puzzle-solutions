import Foundation

let examplePath = "example.txt"
let exampleBPath = "example_b.txt"
let inputPath = "input.txt"

func readFile(_ filePath: String) throws -> String {
  return try String(contentsOfFile: filePath)
}

func getLines(_ filePath: String) throws -> [String] {
  let slices = try readFile(filePath).split(separator: "\n")
  return slices.map { String($0) }
}

extension Character {
  func toNumber() -> Int? {
    Int(String(self))
  }
}

struct CalibrationValue {
  let value: Int
}

func parseNumbersA(_ str: String) -> [Int] {
  str.compactMap { $0.toNumber() }
}

func makeCalibrationValue(fromNumbers numbers: [Int]) -> CalibrationValue {
  CalibrationValue(value: numbers.first! * 10 + numbers.last!)
}

func a() -> Int {
  let lines = try! getLines(inputPath)
  let numbersPerLine = lines.map(parseNumbersA)
  let calibrationValues = numbersPerLine.map { makeCalibrationValue(fromNumbers: $0) }

  return calibrationValues.reduce(0) {
    $0 + $1.value
  }
}

// B

enum NumberAsString: String, CaseIterable {
  case one = "one"
  case two = "two"
  case three = "three"
  case four = "four"
  case five = "five"
  case six = "six"
  case seven = "seven"
  case eight = "eight"
  case nine = "nine"

  func toNumber() -> Int {
    return switch self {
    case .one: 1
    case .two: 2
    case .three: 3
    case .four: 4
    case .five: 5
    case .six: 6
    case .seven: 7
    case .eight: 8
    case .nine: 9
    }
  }
}

func parseNumbersB(_ str: String) -> [Int] {
  var substring = Substring(str)
  var result: [Int] = []
  while !substring.isEmpty {
    if let number = parseNumberB(substring) {
      result.append(number)
    }

    substring = substring.dropFirst()
  }

  return result
}

func parseNumberB(_ substring: Substring) -> Int? {
  if let number = substring.first!.toNumber() {
    return number
  }
  if let numberAsString = NumberAsString.allCases.first(where: {
    substring.starts(with: $0.rawValue)
  }) {
    return numberAsString.toNumber()
  }
  return nil
}

func b() -> Int {
  let lines = try! getLines(inputPath)
  let numbersPerLine = lines.map(parseNumbersB)
  let calibrationValues = numbersPerLine.map { makeCalibrationValue(fromNumbers: $0) }

  return calibrationValues.reduce(0) {
    $0 + $1.value
  }
}

print("a:", a())
print("b:", b())
