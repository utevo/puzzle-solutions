import {
  Brand,
  Data,
  ReadonlyArray as RA,
  Struct as St,
  String as S,
  pipe,
  Option,
  MutableHashSet,
  Order,
  Chunk,
} from "effect";
type RA<T> = ReadonlyArray<T>;

const examplePath = "./05/example.txt";
const inputPath = "./05/input.txt";

type Val = number & Brand.Brand<"Val">;
const Val = Brand.nominal<Val>();

interface Map_ extends Data.Case {
  readonly _tag: "Map_";
  readonly destination: number;
  readonly source: number;
  readonly range: number;
}
const Map_ = Data.tagged<Map_>("Map_");

type Mapping = RA<Map_>;

interface Problem extends Data.Case {
  readonly _tag: "Problem";
  readonly seeds: RA<Val>;
  readonly mappings: RA<Mapping>;
}
const Problem = Data.tagged<Problem>("Problem");

namespace Parsing {
  export function parseProblem(
    line: RA.NonEmptyReadonlyArray<string>
  ): Problem {
    const seedsLine = RA.headNonEmpty(line);
    const seeds = parseSeeds(seedsLine);

    const mappingsLines = RA.drop(line, 1);

    let mappings = RA.empty<Mapping>();
    let rest = mappingsLines;
    do {
      rest = RA.drop(rest, 1);
      let rawMapping;
      [rawMapping, rest] = RA.splitWhere(rest, (it) => S.trim(it) === "");

      mappings = RA.append(mappings, parseMapping(rawMapping));
    } while (RA.length(rest) > 0);

    return Problem({ seeds, mappings });
  }

  function parseSeeds(line: string): RA<Val> {
    return pipe(
      line,
      S.split(" "),
      RA.drop(1),
      RA.map((it) => Val(parseInt(it)))
    );
  }

  function parseMapping(lines: RA<string>): Mapping {
    return pipe(lines, RA.drop(1), RA.map(parseMap_));
  }

  function parseMap_(line: string): Map_ {
    const numbers = pipe(
      line,
      S.split(" "),
      RA.map((it) => parseInt(it))
    );

    return Map_({
      destination: numbers[0],
      source: numbers[1],
      range: numbers[2],
    });
  }
}

namespace A {
  export function calc(problem: Problem): number {
    const humidities = pipe(
      problem.seeds,
      RA.map((seed, idx) => A.convert(seed, problem.mappings))
    );

    return pipe(
      humidities,
      RA.map((it) => it.valueOf()),
      Option.liftPredicate(RA.isNonEmptyArray),
      Option.getOrThrow,
      RA.min(Order.number)
    );
  }

  export function convert(seed: Val, mappings: RA<Mapping>): Val {
    return pipe(
      mappings,
      RA.reduce(seed, (val, mapping) => {
        return convertOneStep(val, mapping);
      })
    );
  }

  export function convertOneStep(val: Val, mapping: Mapping): Val {
    for (const map of mapping) {
      if (val >= map.source && val <= map.source + map.range) {
        return (val + map.destination - map.source) as Val;
      }
    }
    return val;
  }
}

await main();
async function main() {
  const lines = await readLines(inputPath);
  if (!RA.isNonEmptyReadonlyArray(lines)) throw Error();

  const problem = Parsing.parseProblem(lines);

  console.log("a", A.calc(problem));
  // console.log("a", A.a(problem));
}

async function readLines(path: string): Promise<string[]> {
  const file = Bun.file(path);

  const text = await file.text();
  return pipe(text.split("\n"), (it) => {
    const last = RA.last(it).pipe(Option.getOrThrow);
    if (last === "") {
      return RA.dropRight(it, 1);
    }
    return it;
  });
}
