type Interval = {
  from: number;
  to: number;
};

type IntervalPair = {
  first: Interval;
  second: Interval;
};

function intervalFromString(str: string): Interval {
  const [fromAsString, toAsString] = str.split("-");
  return {
    from: Number.parseInt(fromAsString),
    to: Number.parseInt(toAsString),
  };
}

function checkIfContain(interval: Interval, otherInterval: Interval): boolean {
  return (interval.from <= otherInterval.from) &&
    (interval.to >= otherInterval.to);
}

function checkIfOverlap(interval: Interval, otherInterval: Interval): boolean {
  const lengthOfMinContainingInterval =
    (Math.max(interval.to, otherInterval.to) -
      Math.min(interval.from, otherInterval.from)) + 1;
  const lengthOfInterval = interval.to - interval.from + 1;
  const lengthOfOtherInterval = otherInterval.to - otherInterval.from + 1;

  return lengthOfMinContainingInterval <
    (lengthOfInterval + lengthOfOtherInterval);
}

async function main() {
  const text = await Deno.readTextFile("./inputs/4.txt");
  const rawIntervalPairs = text.split("\n");
  rawIntervalPairs.pop();
  const raw2IntervalPairs = rawIntervalPairs.map((rawIntervalPair) =>
    rawIntervalPair.split(",")
  );
  const intervalPairs: IntervalPair[] = raw2IntervalPairs.map((
    [firstIntervalAsString, secondIntervalAsString],
  ) => ({
    first: intervalFromString(firstIntervalAsString),
    second: intervalFromString(secondIntervalAsString),
  }));

  const intervalPairsWhichOFCTO = intervalPairs.filter(({ first, second }) =>
    checkIfContain(first, second) || checkIfContain(second, first)
  );

  console.log({
    intervalPairsWhichOFCTOSlice: intervalPairsWhichOFCTO.slice(0, 10),
    length: intervalPairsWhichOFCTO.length,
  });

  const intervalPairsWhichOverlap = intervalPairs.filter(({ first, second }) =>
    checkIfOverlap(first, second)
  );

  console.log({
    intervalPairsWhichOverlapSlice: intervalPairsWhichOverlap.slice(0, 10),
    length: intervalPairsWhichOverlap.length,
  });

  // console.log({
  //   intervalPairs: intervalPairs.splice(0, 5),
  //   intervalPairsLast: intervalPairs.at(-1),
  // });
}

await main();
