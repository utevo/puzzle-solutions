import {
  Brand,
  Chunk,
  Data,
  HashMap,
  HashSet,
  Number,
  Option,
  Predicate,
  ReadonlyArray,
  String,
  pipe,
} from "effect";
import { cons } from "effect/List";

interface Card extends Data.Case {
  readonly _tag: "Card";
  readonly winning: ReadonlyArray<number>;
  readonly your: ReadonlyArray<number>;
}
const Card = Data.tagged<Card>("Card");

namespace parsing {
  export function parseCards(
    lines: ReadonlyArray<string>
  ): ReadonlyArray<Card> {
    return ReadonlyArray.map(lines, parseCard);
  }

  function parseCard(line: string): Card {
    const [rawFirst, rawSecond] = String.split(line, "|");

    const winning = pipe(
      rawFirst,
      String.trim,
      String.split(/\s+/),
      ReadonlyArray.drop(2),
      ReadonlyArray.map((it) => parseInt(it))
    );

    const your = pipe(
      rawSecond,
      String.trim,
      String.split(/\s+/),
      ReadonlyArray.map((it) => parseInt(it))
    );

    return Card({ winning, your });
  }
}

function commonNumber(card: Card): ReadonlyArray<number> {
  return HashSet.intersection(
    HashSet.fromIterable(card.your),
    HashSet.fromIterable(card.winning)
  ).pipe(ReadonlyArray.fromIterable);
}

namespace A {
  export function a(cards: ReadonlyArray<Card>): number {
    const commonNumbers = ReadonlyArray.map(cards, (it) => commonNumber(it));

    return pipe(commonNumbers, ReadonlyArray.map(makeValue), Number.sumAll);
  }

  function makeValue(commonNumbers: ReadonlyArray<number>): number {
    if (commonNumbers.length === 0) {
      return 0;
    }
    return 2 ** (commonNumbers.length - 1);
  }
}

type CardId = number & Brand.Brand<"CardId">;

namespace B {
  export function b(cards: ReadonlyArray<Card>): number {
    let cardCountById = HashMap.empty<CardId, number>();
    ReadonlyArray.range(1, cards.length).forEach((idx) => {
      cardCountById = cardCountById.pipe(HashMap.set(idx as CardId, 1));
    });

    for (const [rawIdx, card] of cards.entries()) {
      const idx = rawIdx + 1;
      const cardCount = cardCountById.pipe(HashMap.unsafeGet(idx));

      const value = pipe(card, commonNumber, ReadonlyArray.length);
      if (value === 0) {
        continue;
      }
      for (const idx_ of ReadonlyArray.range(idx + 1, idx + value)) {
        cardCountById = cardCountById.pipe(
          HashMap.modifyAt(idx_ as CardId, (it) => {
            return it.pipe(Option.map((it) => it + cardCount));
          })
        );
      }
    }

    return cardCountById.pipe(HashMap.values, Number.sumAll);
  }
}

const examplePath = "./04/example.txt";
const inputPath = "./04/input.txt";

await main();
async function main() {
  const lines = await readLines(inputPath);
  const cards = parsing.parseCards(lines);

  console.log("a", A.a(cards));
  console.log("b", B.b(cards));
}

async function readLines(path: string): Promise<string[]> {
  const file = Bun.file(path);

  const text = await file.text();
  return text.split("\n").filter((it) => it.length !== 0);
}
