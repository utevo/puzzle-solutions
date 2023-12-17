import { Chunk, Data, HashSet, Number, Predicate, Tuple, pipe } from "effect";

function isNumber(char: string): boolean {
  if (char.length !== 1) throw new Error("Invalid input");

  return char[0] >= "0" && char[0] <= "9";
}
const examplePath = "./03/example.txt";
const example2Path = "./03/example2.txt";
const inputPath = "./03/input.txt";

interface Point2D extends Data.Case {
  readonly _tag: "Point2D";
  readonly x: number;
  readonly y: number;
}
const Point2D = Data.tagged<Point2D>("Point2D");

interface Engine extends Data.Case {
  readonly _tag: "Engine";
  readonly numbers: HashSet.HashSet<EngineNumber>;
  readonly symbols: HashSet.HashSet<EngineSymbol>;
  readonly _raw: string[];
}
const Engine = Data.tagged<Engine>("Engine");

interface EngineNumber extends Data.Case {
  readonly _tag: "EngineNumber";
  readonly value: number;
  readonly startPosition: Point2D;
  readonly length: number;
}
const EngineNumber = Data.tagged<EngineNumber>("EngineNumber");

interface EngineSymbol extends Data.Case {
  readonly _tag: "EngineSymbol";
  readonly position: Point2D;
  readonly value: string;
}
const EngineSymbol = Data.tagged<EngineSymbol>("EngineSymbol");

namespace parsing {
  export function parseEngine(lines: string[]): Engine {
    const lineResult_s = lines.map((line, idx) =>
      parseLine({ line, lineIdx: idx })
    );
    let numbers = HashSet.empty<EngineNumber>();
    let symbols = HashSet.empty<EngineSymbol>();

    for (const lineResult of lineResult_s) {
      numbers = HashSet.union(numbers, lineResult.numbers);
      symbols = HashSet.union(symbols, lineResult.symbols);
    }

    return Engine({ numbers, symbols, _raw: lines });
  }

  interface ParseLineParams {
    line: string;
    lineIdx: number;
  }
  interface LineResult {
    numbers: HashSet.HashSet<EngineNumber>;
    symbols: HashSet.HashSet<EngineSymbol>;
  }
  function parseLine({ line, lineIdx }: ParseLineParams): LineResult {
    let engineNumber_s = HashSet.empty<EngineNumber>();
    let engineSymbol_s = HashSet.empty<EngineSymbol>();

    let idx = 0;
    const curr = () => line[idx];
    const next = () => line[idx + 1];
    function isEnd() {
      return idx >= line.length;
    }
    function incIf(cond: (char: string) => boolean): boolean {
      if (idx + 1 >= line.length) {
        return false;
      }
      if (cond(next())) {
        idx += 1;
        return true;
      }
      return false;
    }

    while (idx < line.length) {
      if (isNumber(curr())) {
        let rawNumber = "";
        do {
          rawNumber += curr();
        } while (incIf(isNumber));

        const newEngineNumber = EngineNumber({
          value: parseInt(rawNumber),
          length: rawNumber.length,
          startPosition: Point2D({ x: idx - rawNumber.length + 1, y: lineIdx }),
        });
        engineNumber_s = HashSet.add(engineNumber_s, newEngineNumber);
      } else if (curr() !== ".") {
        engineSymbol_s = HashSet.add(
          engineSymbol_s,
          EngineSymbol({
            position: Point2D({ x: idx, y: lineIdx }),
            value: curr(),
          })
        );
      }
      idx += 1;
    }

    return {
      numbers: engineNumber_s,
      symbols: engineSymbol_s,
    };
  }
}

namespace A {
  interface P {
    number: EngineNumber;
    point: Point2D;
  }
  export function isAdjacentPointOfEngineNumber({ number, point }: P): boolean {
    const xIsOk =
      point.x >= number.startPosition.x - 1 &&
      point.x <= number.startPosition.x + number.length;
    const yIsOk =
      point.y >= number.startPosition.y - 1 &&
      point.y <= number.startPosition.y + 1;

    return xIsOk && yIsOk;
  }
}

async function readLines(path: string): Promise<string[]> {
  const file = Bun.file(path);

  const text = await file.text();
  return text.split("\n");
}

namespace Debug {
  interface Props {
    number: EngineNumber;
    engine: Engine;
  }
  export function numberWithContext({ number, engine }: Props): string {
    let result = "";
    for (const y of Chunk.range(
      number.startPosition.y - 1,
      number.startPosition.y + 1
    )) {
      for (const x of Chunk.range(
        number.startPosition.x - 1,
        number.startPosition.x + number.length
      )) {
        if (engine._raw[y] === undefined || engine._raw[y][x] === undefined) {
          result += " ";
        } else {
          result += engine._raw[y][x];
        }
      }
      result += "\n";
    }

    return result;
  }
}

await main();
async function main() {
  const lines = await readLines(inputPath);
  const engine = parsing.parseEngine(lines);

  console.log("a", a(engine));
  console.log("b", b(engine));
}

function a(engine: Engine): number {
  const goodNumbers = engine.numbers.pipe(
    HashSet.filter((number) =>
      HashSet.some(engine.symbols, (symbol) =>
        A.isAdjacentPointOfEngineNumber({ number, point: symbol.position })
      )
    )
  );

  return goodNumbers.pipe(
    HashSet.values,
    Chunk.fromIterable,
    Chunk.map((it) => it.value),
    Number.sumAll
  );
}

function b(engine: Engine): number {
  const goodSymbols = engine.symbols.pipe(
    HashSet.filter((it) => it.value === "*")
  );

  const pairs = goodSymbols.pipe(
    HashSet.values,
    Chunk.fromIterable,
    Chunk.map((goodSymbol) => {
      const adjacentNumbers = engine.numbers.pipe(
        HashSet.filter((number) =>
          A.isAdjacentPointOfEngineNumber({
            number,
            point: goodSymbol.position,
          })
        ),
        HashSet.values,
        Chunk.fromIterable
      );
      if (adjacentNumbers.pipe(Chunk.size) === 2) {
        return Tuple.make(
          Chunk.unsafeGet(adjacentNumbers, 0),
          Chunk.unsafeGet(adjacentNumbers, 1)
        );
      }
      return null;
    }),
    Chunk.filter(Predicate.isNotNullable)
  );

  const result = pairs.pipe(
    Chunk.map(([a, b]) => a.value * b.value),
    Number.sumAll
  );

  return result;
}
